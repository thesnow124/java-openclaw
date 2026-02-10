package com.openclawlite.openclaw.domain.agent;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智能问题解决器 - 主动检测并修复常见问题
 *
 * 核心能力：
 * 1. 问题诊断 - 检测各类常见问题
 * 2. 解决方案库 - 针对问题的自动化修复
 * 3. 自动执行 - 安全地执行修复操作
 * 4. 结果验证 - 确认修复是否成功
 * 5. 经验积累 - 记录问题和解决方案
 */
@Component
public class ProblemSolver {

    private final SolutionKnowledgeBase knowledgeBase;

    public ProblemSolver(SolutionKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    /**
     * 执行任务并自动解决问题
     *
     * @param task 要执行的任务
     * @return 执行结果
     */
    public ExecutionResult executeWithAutoFix(Task task) {
        int maxRetries = 3;
        int attempt = 0;
        ExecutionResult lastResult = null;

        while (attempt < maxRetries) {
            attempt++;
            System.out.println("🔧 尝试执行任务 (第 " + attempt + " 次)...");

            // 执行任务
            ExecutionResult result = executeTask(task);

            // 如果成功，直接返回
            if (result.isSuccess()) {
                System.out.println("✅ 任务执行成功！");
                return result;
            }

            // 如果失败，尝试诊断和修复
            Problem problem = diagnoseProblem(result);
            if (problem == null || !problem.isFixable()) {
                System.out.println("❌ 问题无法自动修复");
                return result;
            }

            System.out.println("🔍 检测到问题: " + problem.getDescription());

            // 尝试修复
            FixResult fixResult = attemptFix(problem);
            if (!fixResult.isSuccess()) {
                System.out.println("❌ 修复失败: " + fixResult.getMessage());
                return result;
            }

            System.out.println("✅ 问题已修复: " + fixResult.getMessage());

            // 等待修复生效
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            lastResult = result;
        }

        System.out.println("❌ 达到最大重试次数");
        return lastResult != null ? lastResult : new ExecutionResult(false, -1, "达到最大重试次数", task);
    }

    /**
     * 执行任务
     */
    private ExecutionResult executeTask(Task task) {
        try {
            String command = task.getCommand();
            String workingDir = task.getWorkingDir();

            ProcessBuilder builder = new ProcessBuilder();
            builder.command("bash", "-lc", command);

            if (workingDir != null) {
                builder.directory(new File(workingDir));
            }

            builder.redirectErrorStream(true);
            Process process = builder.start();

            // 如果有输入，写入标准输入
            if (task.getInput() != null) {
                process.getOutputStream().write(task.getInput().getBytes());
                process.getOutputStream().flush();
            }
            process.getOutputStream().close();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            boolean success = (exitCode == 0);

            return new ExecutionResult(success, exitCode, output.toString(), task);

        } catch (Exception e) {
            return new ExecutionResult(false, -1, e.getMessage(), task);
        }
    }

    /**
     * 诊断问题
     */
    private Problem diagnoseProblem(ExecutionResult result) {
        String output = result.getOutput();
        String command = result.getTask().getCommand();

        // 1. Python ImportError
        if (output.contains("ModuleNotFoundError") || output.contains("No module named")) {
            return Problem.builder()
                .type(ProblemType.PYTHON_MODULE_MISSING)
                .description("缺少 Python 模块")
                .extractedData(extractModuleName(output))
                .fixCommand(buildPipInstallCommand(output))
                .build();
        }

        // 2. Java ClassNotFoundException
        if (output.contains("ClassNotFoundException") || output.contains("NoClassDefFoundError")) {
            return Problem.builder()
                .type(ProblemType.JAVA_CLASS_MISSING)
                .description("缺少 Java 类或依赖")
                .extractedData(extractClassName(output))
                .fixCommand(buildMavenDependencyCommand(output))
                .build();
        }

        // 3. Command not found
        if (output.contains("command not found") || output.contains("not installed")) {
            return Problem.builder()
                .type(ProblemType.COMMAND_NOT_FOUND)
                .description("缺少命令行工具")
                .extractedData(extractMissingCommand(command, output))
                .fixCommand(buildInstallCommand(output))
                .build();
        }

        // 4. Permission denied
        if (output.contains("Permission denied")) {
            return Problem.builder()
                .type(ProblemType.PERMISSION_DENIED)
                .description("权限不足")
                .extractedData(extractFilePermission(output))
                .fixCommand(buildChmodCommand(output))
                .build();
        }

        // 5. File not found (需要创建)
        if (output.contains("No such file or directory") && !command.startsWith("pip") && !command.startsWith("npm")) {
            return Problem.builder()
                .type(ProblemType.FILE_NOT_FOUND)
                .description("文件或目录不存在")
                .extractedData(extractMissingPath(output))
                .fixCommand(buildCreateFileCommand(output))
                .build();
        }

        // 6. Port already in use
        if (output.contains("Address already in use") || output.contains("port is already in use")) {
            return Problem.builder()
                .type(ProblemType.PORT_IN_USE)
                .description("端口被占用")
                .extractedData(extractPortNumber(output))
                .fixCommand(buildKillPortCommand(output))
                .build();
        }

        return null; // 无法识别的问题
    }

    /**
     * 尝试修复问题
     */
    private FixResult attemptFix(Problem problem) {
        try {
            String fixCommand = problem.getFixCommand();
            if (fixCommand == null || fixCommand.isEmpty()) {
                return FixResult.failure("没有可用的修复方案");
            }

            System.out.println("🔧 执行修复命令: " + fixCommand);

            ProcessBuilder builder = new ProcessBuilder();
            builder.command("bash", "-lc", fixCommand);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return FixResult.success("修复成功: " + output.toString().trim());
            } else {
                return FixResult.failure("修复失败 (退出码: " + exitCode + "): " + output.toString());
            }

        } catch (Exception e) {
            return FixResult.failure("修复异常: " + e.getMessage());
        }
    }

    // ==================== 诊断方法 ====================

    private String extractModuleName(String output) {
        Pattern pattern = Pattern.compile("No module named '([\\w.-]+)'");
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        pattern = Pattern.compile("ModuleNotFoundError: ([\\w.-]+)");
        matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractClassName(String output) {
        Pattern pattern = Pattern.compile("ClassNotFoundException: ([\\w.]+)");
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        pattern = Pattern.compile("NoClassDefFoundError: ([\\w.]+)");
        matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractMissingCommand(String command, String output) {
        // 从命令中提取
        String[] parts = command.split("\\s+");
        if (parts.length > 0) {
            return parts[0];
        }
        // 从输出中提取
        Pattern pattern = Pattern.compile("([\\w-]+): command not found");
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractFilePermission(String output) {
        Pattern pattern = Pattern.compile("Permission denied \\(([^\"]+)\\)");
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractMissingPath(String output) {
        Pattern pattern = Pattern.compile("No such file or directory: `([^']+)'");
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractPortNumber(String output) {
        Pattern pattern = Pattern.compile("port (\\d+)");
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // ==================== 修复命令生成 ====================

    private String buildPipInstallCommand(String output) {
        String module = extractModuleName(output);
        if (module != null) {
            return "pip install " + module;
        }
        return null;
    }

    private String buildMavenDependencyCommand(String output) {
        // 这是一个简化版本，实际应用中需要更智能的依赖解析
        String className = extractClassName(output);
        if (className != null && className.contains(".")) {
            String groupId = extractGroupId(className);
            return "mvn dependency:get -DremoteRepositories=https://repo1.maven.org/maven2 " +
                   "-Dartifact=" + groupId + ":" + extractArtifactId(className);
        }
        return null;
    }

    private String extractGroupId(String className) {
        // 简化实现：将包名转换为 groupId
        String packageName = className.substring(0, className.lastIndexOf('.'));
        return packageName.replace('.', '.');
    }

    private String extractArtifactId(String className) {
        // 简化实现：提取类名作为 artifactId
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private String buildInstallCommand(String output) {
        String command = extractMissingCommand("", output);
        if (command != null) {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("mac")) {
                return "brew install " + command;
            } else if (os.contains("linux")) {
                // 检测是否有 apt-get 或 yum
                return "command -v apt-get >/dev/null 2>&1 && apt-get install -y " + command +
                       " || command -v yum >/dev/null 2>&1 && yum install -y " + command;
            }
            // TODO: 添加 Windows 支持
            return null;
        }
        return null;
    }

    private String buildChmodCommand(String output) {
        String path = extractFilePermission(output);
        if (path != null) {
            return "chmod +x " + path;
        }
        return null;
    }

    private String buildCreateFileCommand(String output) {
        String path = extractMissingPath(output);
        if (path != null) {
            return "mkdir -p " + path + " && touch " + path;
        }
        return null;
    }

    private String buildKillPortCommand(String output) {
        String port = extractPortNumber(output);
        if (port != null) {
            // Linux/macOS
            return "lsof -ti :" + port + " | xargs kill -9 2>/dev/null || true";
        }
        return null;
    }

    // ==================== 内部类 ====================

    /**
     * 问题类型
     */
    public enum ProblemType {
        PYTHON_MODULE_MISSING,    // 缺少 Python 模块
        JAVA_CLASS_MISSING,       // 缺少 Java 类
        COMMAND_NOT_FOUND,        // 缺少命令
        PERMISSION_DENIED,         // 权限不足
        FILE_NOT_FOUND,           // 文件不存在
        PORT_IN_USE,              // 端口占用
        UNKNOWN                   // 未知问题
    }

    /**
     * 问题定义
     */
    public static class Problem {
        private final ProblemType type;
        private final String description;
        private final String extractedData;
        private final String fixCommand;

        private Problem(ProblemType type, String description, String extractedData, String fixCommand) {
            this.type = type;
            this.description = description;
            this.extractedData = extractedData;
            this.fixCommand = fixCommand;
        }

        public static Builder builder() {
            return new Builder();
        }

        public ProblemType getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getExtractedData() {
            return extractedData;
        }

        public String getFixCommand() {
            return fixCommand;
        }

        public boolean isFixable() {
            return fixCommand != null && !fixCommand.isEmpty();
        }

        public static class Builder {
            private ProblemType type;
            private String description;
            private String extractedData;
            private String fixCommand;

            public Builder type(ProblemType type) {
                this.type = type;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder extractedData(String extractedData) {
                this.extractedData = extractedData;
                return this;
            }

            public Builder fixCommand(String fixCommand) {
                this.fixCommand = fixCommand;
                return this;
            }

            public Problem build() {
                return new Problem(type, description, extractedData, fixCommand);
            }
        }
    }

    /**
     * 修复结果
     */
    public static class FixResult {
        private final boolean success;
        private final String message;

        private FixResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static FixResult success(String message) {
            return new FixResult(true, message);
        }

        public static FixResult failure(String message) {
            return new FixResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 执行结果
     */
    public static class ExecutionResult {
        private final boolean success;
        private final int exitCode;
        private final String output;
        private final Task task;

        public ExecutionResult(boolean success, int exitCode, String output, Task task) {
            this.success = success;
            this.exitCode = exitCode;
            this.output = output;
            this.task = task;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getOutput() {
            return output;
        }

        public Task getTask() {
            return task;
        }
    }

    /**
     * 任务定义
     */
    public static class Task {
        private final String command;
        private final String workingDir;
        private final String input;

        public Task(String command) {
            this(command, null, null);
        }

        public Task(String command, String workingDir, String input) {
            this.command = command;
            this.workingDir = workingDir;
            this.input = input;
        }

        public String getCommand() {
            return command;
        }

        public String getWorkingDir() {
            return workingDir;
        }

        public String getInput() {
            return input;
        }
    }
}
