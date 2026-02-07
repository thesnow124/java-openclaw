package com.openclawlite.agent.tools;

import com.openclawlite.agent.SkillRef;
import com.openclawlite.agent.SkillSnapshot;
import com.openclawlite.agent.ToolCall;
import com.openclawlite.agent.tools.ToolContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 技能状态查询工具 - 让 AI 能够了解当前可用的技能
 */
@Component
public class SkillsStatusTool implements ToolHandler {

    @Override
    public String name() {
        return "skills_status";
    }

    @Override
    public String description() {
        return "查询当前可用的技能列表及其状态。返回技能名称、描述和位置信息。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "skills_status"
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(),
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
            // 获取技能快照
            SkillSnapshot snapshot = context.getSession().getSkillSnapshot();

            StringBuilder response = new StringBuilder();
            response.append("📊 技能状态报告\n\n");

            if (snapshot == null || snapshot.getSkills() == null || snapshot.getSkills().isEmpty()) {
                response.append("⚠️  当前未加载任何技能。\n\n");
                response.append("💡 要添加技能：\n");
                response.append("1. 在项目根目录创建 skills/ 目录\n");
                response.append("2. 在 skills/ 目录下创建子目录（如 skills/my-skill/）\n");
                response.append("3. 创建 SKILL.md 文件并添加技能描述\n");
                response.append("4. 重启服务以加载技能\n");
            } else {
                List<SkillRef> skills = snapshot.getSkills();
                response.append("✅ 已加载 ").append(skills.size()).append(" 个技能：\n\n");

                for (SkillRef skill : skills) {
                    response.append("• **").append(skill.getName()).append("**");
                    if (skill.getEmoji() != null && !skill.getEmoji().isEmpty()) {
                        response.append(" ").append(skill.getEmoji());
                    }
                    response.append("\n");

                    if (skill.getDescription() != null && !skill.getDescription().isEmpty()) {
                        response.append("  ").append(skill.getDescription()).append("\n");
                    }

                    response.append("\n");
                }

                response.append("💡 提示：\n");
                response.append("- 技能文件位于 ").append(snapshot.getPrompt() != null ? "skills/" : "未知位置").append("\n");
                response.append("- 要修改技能，编辑对应的 SKILL.md 文件\n");
                response.append("- 修改后需要重启服务或重新加载\n");
            }

            return ToolResult.success(response.toString(), Map.of(
                "skill_count", snapshot != null && snapshot.getSkills() != null ? snapshot.getSkills().size() : 0,
                "skills", snapshot != null && snapshot.getSkills() != null ?
                    skillsToStringList(snapshot.getSkills()) : List.of()
            ));

        } catch (Exception e) {
            return ToolResult.error("查询技能状态失败: " + e.getMessage());
        }
    }

    /**
     * 将 SkillRef 列表转换为字符串列表
     */
    private List<String> skillsToStringList(List<SkillRef> skills) {
        List<String> result = new ArrayList<>();
        for (SkillRef skill : skills) {
            result.add(skill.getName());
        }
        return result;
    }
}
