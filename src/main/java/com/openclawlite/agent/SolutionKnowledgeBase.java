package com.openclawlite.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解决方案知识库 - 记录问题和解决方案，积累经验
 *
 * 功能：
 * 1. 记录遇到的问题和解决方案
 * 2. 学习成功的修复模式
 * 3. 推荐解决方案
 * 4. 统计问题类型分布
 */
@Component
public class SolutionKnowledgeBase {

    private final ObjectMapper objectMapper;
    private final Map<String, Solution> solutions;
    private final String knowledgeBasePath;

    public SolutionKnowledgeBase(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.solutions = new ConcurrentHashMap<>();
        this.knowledgeBasePath = "data/solution_knowledge.json";
        loadKnowledgeBase();
    }

    /**
     * 记录解决方案
     */
    public void recordSolution(String problemSignature, String problemType, String solution, boolean successful) {
        Solution sol = new Solution(
            problemSignature,
            problemType,
            solution,
            successful,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        solutions.put(problemSignature, sol);
        saveKnowledgeBase();
    }

    /**
     * 查找解决方案
     */
    public String findSolution(String problemSignature) {
        Solution solution = solutions.get(problemSignature);
        if (solution != null && solution.isSuccessful()) {
            return solution.getSolution();
        }
        return null;
    }

    /**
     * 获取问题统计
     */
    public Map<String, Integer> getProblemStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        for (Solution solution : solutions.values()) {
            String type = solution.getProblemType();
            stats.put(type, stats.getOrDefault(type, 0) + 1);
        }
        return stats;
    }

    /**
     * 获取所有解决方案
     */
    public Map<String, Solution> getAllSolutions() {
        return new HashMap<>(solutions);
    }

    /**
     * 加载知识库
     */
    private void loadKnowledgeBase() {
        try {
            File file = new File(knowledgeBasePath);
            if (file.exists()) {
                String json = Files.readString(file.toPath());
                // 简化实现：实际应该用 JSON 解析
                System.out.println("📚 已加载知识库: " + solutions.size() + " 条解决方案");
            } else {
                System.out.println("📚 知识库不存在，创建新的...");
            }
        } catch (Exception e) {
            System.err.println("⚠️  加载知识库失败: " + e.getMessage());
        }
    }

    /**
     * 保存知识库
     */
    private void saveKnowledgeBase() {
        try {
            // 确保目录存在
            Files.createDirectories(Paths.get(knowledgeBasePath).getParent());
            // TODO: 实际应该序列化为 JSON
            System.out.println("💾 知识库已更新");
        } catch (Exception e) {
            System.err.println("⚠️ 保存知识库失败: " + e.getMessage());
        }
    }

    /**
     * 解决方案定义
     */
    public static class Solution {
        private final String problemSignature;
        private final String problemType;
        private final String solution;
        private final boolean successful;
        private final String timestamp;

        public Solution(String problemSignature, String problemType, String solution,
                       boolean successful, String timestamp) {
            this.problemSignature = problemSignature;
            this.problemType = problemType;
            this.solution = solution;
            this.successful = successful;
            this.timestamp = timestamp;
        }

        public String getProblemSignature() {
            return problemSignature;
        }

        public String getProblemType() {
            return problemType;
        }

        public String getSolution() {
            return solution;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    /**
     * 问题签名生成器
     */
    public static class ProblemSignature {
        /**
         * 从错误输出生成问题签名
         */
        public static String generate(String output, String command) {
            StringBuilder signature = new StringBuilder();

            // 提取关键错误信息
            if (output.contains("ModuleNotFoundError") || output.contains("No module named")) {
                signature.append("PYTHON_MODULE_MISSING:");
                // 提取模块名
                if (output.contains("'")) {
                    int start = output.indexOf("'") + 1;
                    int end = output.indexOf("'", start);
                    if (end > start) {
                        signature.append(output.substring(start, end));
                    }
                }
            } else if (output.contains("ClassNotFoundException")) {
                signature.append("JAVA_CLASS_MISSING:");
                // 提取类名
                int start = output.indexOf("ClassNotFoundException:") + "ClassNotFoundException:".length();
                String rest = output.substring(start).trim();
                String className = rest.split("\\s")[0];
                signature.append(className);
            } else if (output.contains("command not found")) {
                signature.append("COMMAND_NOT_FOUND:");
                // 提取命令名
                if (command != null) {
                    String[] parts = command.split("\\s+");
                    if (parts.length > 0) {
                        signature.append(parts[0]);
                    }
                }
            } else if (output.contains("Permission denied")) {
                signature.append("PERMISSION_DENIED");
            } else if (output.contains("No such file or directory")) {
                signature.append("FILE_NOT_FOUND:");
                // 提取路径
                if (output.contains("`")) {
                    int start = output.indexOf("`") + 1;
                    int end = output.indexOf("'", start);
                    if (end > start) {
                        signature.append(output.substring(start, end));
                    }
                }
            }

            return signature.toString();
        }
    }
}
