package com.openclawlite.agent;

// 技能引用信息（用于会话快照）。
public class SkillRef {
  private String name;
  private String description;
  private String emoji;
  private boolean userInvocable;
  private boolean disableModelInvocation;

  // 默认构造器，供序列化使用。
  public SkillRef() {}

  // 构造一个技能引用（不含 emoji）。
  public SkillRef(String name, String description, boolean userInvocable, boolean disableModelInvocation) {
    this(name, description, null, userInvocable, disableModelInvocation);
  }

  // 构造一个技能引用（包含所有字段）。
  public SkillRef(String name, String description, String emoji, boolean userInvocable, boolean disableModelInvocation) {
    this.name = name;
    this.description = description;
    this.emoji = emoji;
    this.userInvocable = userInvocable;
    this.disableModelInvocation = disableModelInvocation;
  }

  // 获取技能名称。
  public String getName() {
    return name;
  }

  // 设置技能名称。
  public void setName(String name) {
    this.name = name;
  }

  // 获取技能简述。
  public String getDescription() {
    return description;
  }

  // 设置技能简述。
  public void setDescription(String description) {
    this.description = description;
  }

  // 获取技能表情符号。
  public String getEmoji() {
    return emoji;
  }

  // 设置技能表情符号。
  public void setEmoji(String emoji) {
    this.emoji = emoji;
  }

  // 是否允许用户显式调用。
  public boolean isUserInvocable() {
    return userInvocable;
  }

  // 设置是否允许用户显式调用。
  public void setUserInvocable(boolean userInvocable) {
    this.userInvocable = userInvocable;
  }

  // 是否禁止模型自动调用。
  public boolean isDisableModelInvocation() {
    return disableModelInvocation;
  }

  // 设置是否禁止模型自动调用。
  public void setDisableModelInvocation(boolean disableModelInvocation) {
    this.disableModelInvocation = disableModelInvocation;
  }
}
