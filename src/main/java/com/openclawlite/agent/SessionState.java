package com.openclawlite.agent;

import java.util.ArrayList;
import java.util.List;

// 会话状态，保存对话消息与技能快照。
public class SessionState {
  private String sessionId;
  private long createdAt;
  private long updatedAt;
  private String model;
  private SkillSnapshot skillSnapshot;
  private List<MessageRecord> messages = new ArrayList<>();

  // 工具调用统计
  private int totalToolCalls = 0;
  private int successfulToolCalls = 0;
  private int failedToolCalls = 0;

  // Token 使用统计
  private long totalInputTokens = 0;
  private long totalOutputTokens = 0;

  // 获取会话 ID。
  public String getSessionId() {
    return sessionId;
  }

  // 设置会话 ID。
  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  // 获取创建时间戳。
  public long getCreatedAt() {
    return createdAt;
  }

  // 设置创建时间戳。
  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  // 获取更新时间戳。
  public long getUpdatedAt() {
    return updatedAt;
  }

  // 设置更新时间戳。
  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }

  // 获取模型名称。
  public String getModel() {
    return model;
  }

  // 设置模型名称。
  public void setModel(String model) {
    this.model = model;
  }

  // 获取技能快照。
  public SkillSnapshot getSkillSnapshot() {
    return skillSnapshot;
  }

  // 设置技能快照。
  public void setSkillSnapshot(SkillSnapshot skillSnapshot) {
    this.skillSnapshot = skillSnapshot;
  }

  // 获取消息列表。
  public List<MessageRecord> getMessages() {
    return messages;
  }

  // 设置消息列表。
  public void setMessages(List<MessageRecord> messages) {
    this.messages = messages;
  }

  // 追加一条消息到会话记录。
  public void addMessage(String role, String content) {
    this.messages.add(new MessageRecord(role, content));
  }

  // 获取总工具调用次数。
  public int getTotalToolCalls() {
    return totalToolCalls;
  }

  // 设置总工具调用次数。
  public void setTotalToolCalls(int totalToolCalls) {
    this.totalToolCalls = totalToolCalls;
  }

  // 获取成功工具调用次数。
  public int getSuccessfulToolCalls() {
    return successfulToolCalls;
  }

  // 设置成功工具调用次数。
  public void setSuccessfulToolCalls(int successfulToolCalls) {
    this.successfulToolCalls = successfulToolCalls;
  }

  // 获取失败工具调用次数。
  public int getFailedToolCalls() {
    return failedToolCalls;
  }

  // 设置失败工具调用次数。
  public void setFailedToolCalls(int failedToolCalls) {
    this.failedToolCalls = failedToolCalls;
  }

  // 获取总输入 Token 数。
  public long getTotalInputTokens() {
    return totalInputTokens;
  }

  // 设置总输入 Token 数。
  public void setTotalInputTokens(long totalInputTokens) {
    this.totalInputTokens = totalInputTokens;
  }

  // 获取总输出 Token 数。
  public long getTotalOutputTokens() {
    return totalOutputTokens;
  }

  // 设置总输出 Token 数。
  public void setTotalOutputTokens(long totalOutputTokens) {
    this.totalOutputTokens = totalOutputTokens;
  }
}
