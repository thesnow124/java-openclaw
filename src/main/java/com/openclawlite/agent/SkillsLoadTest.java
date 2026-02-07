package com.openclawlite.agent;

import com.openclawlite.config.AppProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 简单的技能加载测试类。
 */
public class SkillsLoadTest {

    public static void main(String[] args) {
        System.out.println("=== OpenClaw Lite 技能加载测试 ===\n");

        // 创建配置
        AppProperties properties = new AppProperties();
        properties.setWorkspaceDir(".");
        properties.setSkillsDir("skills");

        // 创建 SkillService
        SkillService skillService = new SkillService(properties);

        // 创建一个模拟的 SessionState
        SessionState session = new SessionState();
        session.setSessionId("test-session");

        try {
            // 获取技能快照
            System.out.println("正在加载技能...");
            SkillSnapshot snapshot = skillService.ensureSnapshot(session);

            // 显示统计信息
            System.out.println("\n✅ 技能加载成功！");
            System.out.println("版本号: " + snapshot.getVersion());
            System.out.println("技能数量: " + snapshot.getSkillRefs().size());

            // 列出所有技能
            System.out.println("\n已加载的技能：");
            for (SkillRef ref : snapshot.getSkillRefs()) {
                System.out.println("  - " + ref.getName() +
                    (ref.getEmoji() != null ? " " + ref.getEmoji() : "") +
                    ": " + ref.getDescription());
            }

            // 显示提示词预览
            String prompt = snapshot.getPrompt();
            if (prompt != null && !prompt.isEmpty()) {
                System.out.println("\n提示词预览（前500字符）：");
                System.out.println(prompt.substring(0, Math.min(500, prompt.length())));
                if (prompt.length() > 500) {
                    System.out.println("...");
                }
            }

        } catch (Exception e) {
            System.err.println("\n❌ 技能加载失败！");
            e.printStackTrace();
        }
    }
}
