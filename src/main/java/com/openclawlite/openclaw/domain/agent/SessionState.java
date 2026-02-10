package com.openclawlite.openclaw.domain.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话状态对象
 *
 * <p>维护 AI Agent 的完整对话上下文，包括：</p>
 * <ul>
 *   <li>对话历史消息</li>
 *   <li>技能快照</li>
 *   <li>时间戳信息</li>
 *   <li>工具调用统计</li>
 *   <li>Token 使用统计</li>
 * </ul>
 *
 * <p>会话状态会在每次对话回合后持久化到磁盘，确保对话历史的连续性。</p>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
public class SessionState {

    /** 会话唯一标识符 */
    private String sessionId;

    /** 会话创建时间戳（毫秒） */
    private long createdAt;

    /** 会话最后更新时间戳（毫秒） */
    private long updatedAt;

    /** 使用的 AI 模型名称 */
    private String model;

    /** 技能快照（用于检测技能更新） */
    private SkillSnapshot skillSnapshot;

    /** 对话消息列表 */
    private List<MessageRecord> messages = new ArrayList<>();

    // ========== 工具调用统计 ==========

    /** 总工具调用次数 */
    private int totalToolCalls = 0;

    /** 成功的工具调用次数 */
    private int successfulToolCalls = 0;

    /** 失败的工具调用次数 */
    private int failedToolCalls = 0;

    // ========== Token 使用统计 ==========

    /** 总输入 Token 数 */
    private long totalInputTokens = 0;

    /** 总输出 Token 数 */
    private long totalOutputTokens = 0;

    /**
     * 获取会话 ID
     *
     * @return 会话唯一标识符
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 设置会话 ID
     *
     * @param sessionId 会话唯一标识符
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取会话创建时间
     *
     * @return 创建时间戳（毫秒）
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置会话创建时间
     *
     * @param createdAt 创建时间戳（毫秒）
     */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取会话最后更新时间
     *
     * @return 更新时间戳（毫秒）
     */
    public long getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置会话最后更新时间
     *
     * @param updatedAt 更新时间戳（毫秒）
     */
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取使用的 AI 模型名称
     *
     * @return 模型名称
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置使用的 AI 模型名称
     *
     * @param model 模型名称
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取技能快照
     *
     * @return 技能快照对象
     */
    public SkillSnapshot getSkillSnapshot() {
        return skillSnapshot;
    }

    /**
     * 设置技能快照
     *
     * @param skillSnapshot 技能快照对象
     */
    public void setSkillSnapshot(SkillSnapshot skillSnapshot) {
        this.skillSnapshot = skillSnapshot;
    }

    /**
     * 获取对话消息列表
     *
     * @return 消息记录列表
     */
    public List<MessageRecord> getMessages() {
        return messages;
    }

    /**
     * 设置对话消息列表
     *
     * @param messages 消息记录列表
     */
    public void setMessages(List<MessageRecord> messages) {
        this.messages = messages;
    }

    /**
     * 追加一条消息到会话记录
     *
     * @param role 消息角色（user/assistant/tool/system）
     * @param content 消息内容
     */
    public void addMessage(String role, String content) {
        this.messages.add(new MessageRecord(role, content));
    }

    // ========== 工具调用统计方法 ==========

    /**
     * 获取总工具调用次数
     *
     * @return 总调用次数
     */
    public int getTotalToolCalls() {
        return totalToolCalls;
    }

    /**
     * 设置总工具调用次数
     *
     * @param totalToolCalls 总调用次数
     */
    public void setTotalToolCalls(int totalToolCalls) {
        this.totalToolCalls = totalToolCalls;
    }

    /**
     * 获取成功的工具调用次数
     *
     * @return 成功次数
     */
    public int getSuccessfulToolCalls() {
        return successfulToolCalls;
    }

    /**
     * 设置成功的工具调用次数
     *
     * @param successfulToolCalls 成功次数
     */
    public void setSuccessfulToolCalls(int successfulToolCalls) {
        this.successfulToolCalls = successfulToolCalls;
    }

    /**
     * 获取失败的工具调用次数
     *
     * @return 失败次数
     */
    public int getFailedToolCalls() {
        return failedToolCalls;
    }

    /**
     * 设置失败的工具调用次数
     *
     * @param failedToolCalls 失败次数
     */
    public void setFailedToolCalls(int failedToolCalls) {
        this.failedToolCalls = failedToolCalls;
    }

    // ========== Token 统计方法 ==========

    /**
     * 获取总输入 Token 数
     *
     * @return 输入 Token 总数
     */
    public long getTotalInputTokens() {
        return totalInputTokens;
    }

    /**
     * 设置总输入 Token 数
     *
     * @param totalInputTokens 输入 Token 总数
     */
    public void setTotalInputTokens(long totalInputTokens) {
        this.totalInputTokens = totalInputTokens;
    }

    /**
     * 获取总输出 Token 数
     *
     * @return 输出 Token 总数
     */
    public long getTotalOutputTokens() {
        return totalOutputTokens;
    }

    /**
     * 设置总输出 Token 数
     *
     * @param totalOutputTokens 输出 Token 总数
     */
    public void setTotalOutputTokens(long totalOutputTokens) {
        this.totalOutputTokens = totalOutputTokens;
    }
}
