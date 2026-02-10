package com.openclawlite.openclaw.application.agent;

/**
 * Agent 身份数据类
 * <p>
 * 表示 Agent 的身份信息，包括名称、表情符号、头像和主题。
 * 用于 Agent 的个性化和视觉展示。
 * </p>
 *
 * <p>字段说明：</p>
 * <ul>
 *   <li>name: Agent 显示名称</li>
 *   <li>emoji: Agent 表情符号图标</li>
 *   <li>avatar: Agent 头像图片 URL（可选）</li>
 *   <li>theme: UI 主题（light/dark）</li>
 * </ul>
 *
 * <p>注意：</p>
 * 该类将在第 4 阶段完整实现身份管理功能。
 *
 * TODO: 完整实现将在第 4 阶段完成
 */
public record AgentIdentity(
    /** Agent 显示名称 */
    String name,

    /** Agent 表情符号图标 */
    String emoji,

    /** Agent 头像图片 URL（可选） */
    String avatar,

    /** UI 主题（light/dark） */
    String theme
) {
    /**
     * 获取默认 Agent 身份
     * <p>
     * 返回一个预配置的默认 Agent 身份，用于初始化或身份信息缺失时。
     * </p>
     *
     * @return 默认的 Agent 身份对象
     */
    public static AgentIdentity defaultAgent() {
        return new AgentIdentity(
            "Claude Assistant",  // 默认名称
            "🤖",                 // 默认表情符号
            null,                 // 无默认头像
            "dark"                // 默认深色主题
        );
    }
}
