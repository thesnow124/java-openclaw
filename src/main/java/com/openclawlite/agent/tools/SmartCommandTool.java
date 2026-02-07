package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;
import com.openclawlite.config.AppProperties;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 智能命令工具 - 支持问题检测和自动解决
 */
@Component
public class SmartCommandTool implements ToolHandler {

    private final AppProperties properties;

    public SmartCommandTool(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public String name() {
        return "smart_exec";
    }

    @Override
    public String description() {
        return "智能命令执行工具：自动检测常见问题（如缺少依赖）并尝试解决。支持 pip、npm、mvn 等包管理器。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "smart_exec",
              "command": "pip install openpyxl",
              "auto-fix": true
            }
            """;
    }

    @Override
    public String execute(ToolCall call, ToolContext context) {
        if (!properties.isEnableCommandTools()) {
            return "❌ 命令工具未启用（enable-command-tools=false）";
        }

        String command = call.getContent();
        if (command == null || command.isBlank()) {
            return "❌ 未提供命令";
        }

        boolean autoFix = call.getArguments() != null &&
                         Boolean.TRUE.equals(call.getArguments().get("auto-fix"));

        try {
            ExecutionResult result = executeCommand(command, context);

            // 检测常见问题并尝试自动修复
            if (autoFix && result.exitCode != 0) {
                String fix = diagnoseAndFix(command, result.output);
                if (fix != null) {
                    // 执行修复命令
                    ExecutionResult fixResult = executeCommand(fix, context);
                    if (fixResult.exitCode == 0) {
                        // 修复成功，重试原命令
                        result = executeCommand(command, context);
                    }
                }
            }

            return formatResult(command, result);

        } catch (Exception e) {
            return "❌ 执行失败: " + e.getMessage();
        }
    }

    /**
     * 诊断问题并返回修复命令
     */
    private String diagnoseAndFix(String originalCommand, String output) {
        // 检测 Python ImportError
        if (output.contains("ModuleNotFoundError") || output.contains("No module named")) {
            String module = extractMissingModule(output);
            if (module != null) {
                return "pip install " + module;
            }
        }

        // 检测 Java ClassNotFoundException
        if (output.contains("ClassNotFoundException") || output.contains("NoClassDefFoundError")) {
            String className = extractClassName(output);
            if (className != null) {
                // 尝试使用 Maven 添加依赖
                return "mvn dependency:get -Dartifact=" + className + " -DremoteRepositories=https://repo1.maven.org/maven2/";
            }
        }

        // 检测 command not found
        if (output.contains("command not found") || output.contains("not installed")) {
            String cmd = extractMissingCommand(originalCommand, output);
            if (cmd != null) {
                // 根据系统推荐安装
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    return "brew install " + cmd;
                } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    return "sudo apt-get install " + cmd + " -y";
                }
            }
        }

        return null;
    }

    /**
     * 执行命令
     */
    private ExecutionResult executeCommand(String command, ToolContext context) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("bash", "-lc", command);
        builder.directory(context.getWorkspace().toFile());
        builder.redirectErrorStream(true);
        Process process = builder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        return new ExecutionResult(exitCode, output.toString());
    }

    /**
     * 格式化执行结果
     */
    private String formatResult(String command, ExecutionResult result) {
        StringBuilder sb = new StringBuilder();

        if (result.exitCode == 0) {
            sb.append("✅ 命令执行成功\n");
            sb.append("📝 命令: ").append(command).append("\n");
            sb.append("📤 输出:\n");
            sb.append(result.output);
        } else {
            sb.append("❌ 命令执行失败 (退出码: ").append(result.exitCode).append(")\n");
            sb.append("📝 命令: ").append(command).append("\n");
            sb.append("💡 建议: 检查命令语法或安装所需依赖\n");
            sb.append("📤 输出:\n");
            sb.append(result.output);
        }

        return sb.toString();
    }

    /**
     * 从错误信息中提取缺失的 Python 模块名
     */
    private String extractMissingModule(String output) {
        // "No module named 'openpyxl'"
        if (output.contains("No module named")) {
            int start = output.indexOf("'") + 1;
            int end = output.indexOf("'", start);
            if (start > 0 && end > start) {
                return output.substring(start, end);
            }
        }
        return null;
    }

    /**
     * 从错误信息中提取缺失的 Java 类名
     */
    private String extractClassName(String output) {
        // "ClassNotFoundException: org.apache.poi.ss.usermodel.Workbook"
        if (output.contains("ClassNotFoundException")) {
            int start = output.indexOf("ClassNotFoundException:") + "ClassNotFoundException:".length();
            String rest = output.substring(start).trim();
            return rest.split("\\s")[0];
        }
        return null;
    }

    /**
     * 从命令或输出中提取缺失的命令名
     */
    private String extractMissingCommand(String command, String output) {
        // 尝试从命令中提取
        String[] parts = command.split("\\s+");
        if (parts.length > 0) {
            return parts[0];
        }
        return null;
    }

    private static class ExecutionResult {
        final int exitCode;
        final String output;

        ExecutionResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
