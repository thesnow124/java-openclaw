package com.openclawlite.agent;

// 单条消息记录（角色 + 文本内容）。
public class MessageRecord {
  private String role;
  private String content;

  // 默认构造器用于序列化。
  public MessageRecord() {}

  // 构造一条消息记录。
  public MessageRecord(String role, String content) {
    this.role = role;
    this.content = content;
  }

  // 获取消息角色。
  public String getRole() {
    return role;
  }

  // 设置消息角色。
  public void setRole(String role) {
    this.role = role;
  }

  // 获取消息内容。
  public String getContent() {
    return content;
  }

  // 设置消息内容。
  public void setContent(String content) {
    this.content = content;
  }
}
