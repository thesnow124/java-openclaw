package com.openclawlite.agent.tools;

import com.openclawlite.agent.ProblemSolver;
import com.openclawlite.agent.ToolCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能执行工具 - 自动检测并修复问题
 *
 * 与普通的命令执行工具不同，这个工具会：
 * 1. 检测失败原因
 * 2. 尝试自动修复（如安装依赖、创建文件等）
 * 3. 修复后自动重试
 * 4. 最多重试3次
 */
@Component
public class SmartExecTool implements ToolHandler {

    private final ProblemSolver problemSolver;

    @Autowired
    public SmartExecTool(ProblemSolver problemSolver) {
        this.problemSolver = problemSolver;
    }

    @Override
    public String name() {
        return "smart_exec";
    }

    @Override
    public String description() {
        return "智能命令执行工具：自动检测并修复常见问题（缺少依赖、权限不足、文件不存在、端口占用等），自动重试直到成功。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "smart_exec",
              "command": "python script.py",
              "workingDir": "/path/to/dir",
              "input": "可选的输入内容"
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("command", Map.of(
            "type", "string",
            "description","要执行的命令"
        ));
        properties.put("workingDir", Map.of(
            "type", "string",
            "description", "工作目录（可选）"
        ));
        properties.put("input", Map.of(
            "type", "string",
            "description", "标准输入内容（可选）"
        ));

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of("command")
        );
    }

    @Override
    public String execute(ToolCall call, ToolContext context) {
        ToolResult result = executeTyped(call, context);
        return result.getText();
    }

    @Override
    public ToolResult executeTyped(ToolCall call, ToolContext context) {
        try {
            String command = call.getArguments().get("command").toString();
            if (command == null || command.trim().isEmpty()) {
                return ToolResult.error("缺少必需参数：command");
            }

            String workingDir = (String) call.getArguments().get("workingDir");
            String input = (String) call.getArguments().get("input");

            // 创建任务
            ProblemSolver.Task task = new ProblemSolver.Task(command);
            if (workingDir != null && !workingDir.trim().isEmpty()) {
                task = new ProblemSolver.Task(command, workingDir, input);
            } else if (input != null && !input.trim().isEmpty()) {
                task = new ProblemSolver.Task(command, null, input);
            }

            // 设置工作目录（如果有）
            if (workingDir != null && context != null) {
                try {
                    java.nio.file.Path resolvedPath = context.resolveSafePath(workingDir);
                    task = new ProblemSolver.Task(command, resolvedPath.toString(), input);
                } catch (Exception e) {
                    // 如果路径解析失败，使用原始路径
                }
            }

            // 执行任务并自动修复
            ProblemSolver.ExecutionResult result = problemSolver.executeWithAutoFix(task);

            // 格式化结果
            return formatResult(result);

        } catch (Exception e) {
            return ToolResult.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 格式化执行结果
     */
    private ToolResult formatResult(ProblemSolver.ExecutionResult result) {
        StringBuilder sb = new StringBuilder();

        if (result.isSuccess()) {
            sb.append("✅ 命令执行成功\n");
            sb.append("📝 命令: ").append(result.getTask().getCommand()).append("\n");
            if (result.getOutput() != null && !result.getOutput().trim().isEmpty()) {
                sb.append("📤 输出:\n").append(result.getOutput());
            }
            return ToolResult.success(sb.toString(), Map.of(
                "success", true,
                "exitCode", result.getExitCode(),
                "output", result.getOutput()
            ));
        } else {
            sb.append("❌ 命令执行失败\n");
            sb.append("📝 命令: ").append(result.getTask().getCommand()).append("\n");
            sb.append("💡 可能的原因: 缺少依赖、权限不足、文件不存在等\n");
            if (result.getOutput() != null && !result.getOutput().trim().isEmpty()) {
                sb.append("📤 输出:\n").append(result.getOutput());
            }
            return ToolResult.success(sb.toString(), Map.of(
                "success", false,
                "exitCode", result.getExitCode(),
                "output", result.getOutput()
            ));
        }
    }
}
