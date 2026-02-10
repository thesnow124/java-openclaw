package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.openclaw.domain.agent.ToolCall;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能问题诊断工具 - 帮助 AI 诊断用户问题并提供解决方案
 *
 * 当用户遇到能力不足的情况时，这个工具可以：
 * 1. 分析问题类型
 * 2. 检查是否有相关技能可用
 * 3. 提供解决方案建议
 * 4. 推荐替代方案
 */
@Component
public class ProblemDiagnosisTool implements ToolHandler {

    private final SkillsStatusTool skillsStatusTool;

    public ProblemDiagnosisTool(SkillsStatusTool skillsStatusTool) {
        this.skillsStatusTool = skillsStatusTool;
    }

    @Override
    public String name() {
        return "diagnose_problem";
    }

    @Override
    public String description() {
        return "诊断用户问题并提供智能解决方案建议。当用户请求某个功能但当前不可用时使用。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "diagnose_problem",
              "user_request": "用户请求的功能描述",
              "missing_capability": "缺失的能力描述"
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("user_request", Map.of(
            "type", "string",
            "description", "用户的原始请求"
        ));
        properties.put("missing_capability", Map.of(
            "type", "string",
            "description", "缺失的能力描述（如果已知）"
        ));

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of()
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
            String userRequest = (String) call.getArguments().get("user_request");
            String missingCapability = (String) call.getArguments().get("missing_capability");

            if (userRequest == null || userRequest.trim().isEmpty()) {
                return ToolResult.error("需要提供 user_request 参数");
            }

            // 分析问题并提供建议
            String analysis = analyzeProblem(userRequest, missingCapability);

            return ToolResult.success(analysis, Map.of(
                "user_request", userRequest,
                "suggestions_provided", true
            ));

        } catch (Exception e) {
            return ToolResult.error("问题诊断失败: " + e.getMessage());
        }
    }

    /**
     * 分析问题并提供建议
     */
    private String analyzeProblem(String userRequest, String missingCapability) {
        StringBuilder response = new StringBuilder();
        response.append("🔍 问题诊断与分析\n\n");

        // 分析用户请求的类型
        String requestType = classifyRequest(userRequest, missingCapability);
        response.append("📋 请求类型：").append(requestType).append("\n\n");

        // 提供针对性的解决方案
        List<String> solutions = generateSolutions(userRequest, missingCapability, requestType);

        if (solutions.isEmpty()) {
            response.append("❌ 暂时无法提供自动解决方案\n\n");
            response.append("建议：\n");
            response.append("- 检查 skills_status 查看是否有相关技能\n");
            response.append("- 尝试使用其他工具组合实现目标\n");
            response.append("- 联系技术支持获取帮助\n");
        } else {
            response.append("💡 解决方案建议：\n\n");
            for (int i = 0; i < solutions.size(); i++) {
                response.append((i + 1)).append(". ").append(solutions.get(i)).append("\n\n");
            }
        }

        // 添加通用建议
        response.append("---\n\n");
        response.append("📚 相关资源：\n");
        response.append("- 文档：https://docs.openclaw.ai\n");
        response.append("- 技能市场：https://clawhub.com\n");
        response.append("- GitHub：https://github.com/openclaw/java-openclaw-lite\n");

        return response.toString();
    }

    /**
     * 分类请求类型
     */
    private String classifyRequest(String userRequest, String missingCapability) {
        String lower = userRequest.toLowerCase();

        if (lower.contains("图片") || lower.contains("图像") || lower.contains("photo") || lower.contains("image")) {
            return "图像分析/处理";
        }
        if (lower.contains("pdf") || lower.contains("文档")) {
            return "文档处理";
        }
        if (lower.contains("网络") || lower.contains("搜索") || lower.contains("抓取")) {
            return "网络操作";
        }
        if (lower.contains("数据库") || lower.contains("sql") || lower.contains("存储")) {
            return "数据存储";
        }
        if (lower.contains("消息") || lower.contains("通知") || lower.contains("邮件")) {
            return "消息通知";
        }
        if (lower.contains("代码") || lower.contains("编程") || lower.contains("开发")) {
            return "开发工具";
        }

        if (missingCapability != null && !missingCapability.isEmpty()) {
            return "缺失能力: " + missingCapability;
        }

        return "通用请求";
    }

    /**
     * 生成解决方案列表
     */
    private List<String> solutions = new ArrayList<>();

    private List<String> generateSolutions(String userRequest, String missingCapability, String requestType) {
        solutions.clear();
        String lower = userRequest.toLowerCase();

        // 图像相关的解决方案
        if (requestType.contains("图像")) {
            solutions.add("配置视觉模型 API（推荐）\n" +
                "   - 设置 CLAUDE_API_KEY 或 OPENAI_API_KEY\n" +
                "   - 使用 analyze_image 工具分析图像");

            solutions.add("使用在线服务（临时方案）\n" +
                "   - Google Lens: lens.google.com\n" +
                "   - 百度识图: image.baidu.com\n" +
                "   - 上传图片并获取描述");
        }

        // 文档处理相关的解决方案
        if (requestType.contains("文档")) {
            solutions.add("使用现有的文档生成工具\n" +
                "   - generate_word: 生成 Word 文档\n" +
                "   - generate_excel: 生成 Excel 表格");

            solutions.add("安装 Apache POI 依赖\n" +
                "   - 已包含在 pom.xml 中\n" +
                "   - 支持生成 .docx 和 .xlsx 文件");
        }

        // 网络操作相关的解决方案
        if (requestType.contains("网络")) {
            solutions.add("使用 web_search 工具\n" +
                "   - 需要配置搜索引擎 API（如 Brave Search）\n" +
                "   - 可执行网络搜索和获取网页内容");

            solutions.add("使用 web_fetch 工具\n" +
                "   - 获取并解析网页内容\n" +
                "   - 自动转换为 Markdown 格式");
        }

        // 通用解决方案
        if (solutions.isEmpty()) {
            solutions.add("检查可用技能\n" +
                "   - 使用 skills_status 工具查询\n" +
                "   - 查看是否有相关技能可以满足需求");

            solutions.add("工具组合方案\n" +
                "   - 分析现有工具的能力\n" +
                "   - 尝试组合多个工具实现目标");

            solutions.add("扩展能力\n" +
                "   - 安装新的技能包\n" +
                "   - 配置所需的 API 密钥\n" +
                "   - 查阅文档了解如何扩展功能");
        }

        return solutions;
    }
}
