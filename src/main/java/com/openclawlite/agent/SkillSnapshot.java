package com.openclawlite.agent;

import java.util.ArrayList;
import java.util.List;

// skills 的快照，用于检测更新与拼接系统提示词。
public class SkillSnapshot {
  private String prompt;
  private List<SkillRef> skills = new ArrayList<>();
  private long version;

  // 默认构造器用于反序列化。
  public SkillSnapshot() {}

  // 构造完整的技能快照。
  public SkillSnapshot(String prompt, List<SkillRef> skills, long version) {
    this.prompt = prompt;
    this.skills = skills;
    this.version = version;
  }

  // 获取拼接后的技能提示词内容。
  public String getPrompt() {
    return prompt;
  }

  // 设置拼接后的技能提示词内容。
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  // 获取技能引用列表。
  public List<SkillRef> getSkills() {
    return skills;
  }

  // 获取技能引用列表（别名）。
  public List<SkillRef> getSkillRefs() {
    return skills;
  }

  // 设置技能引用列表。
  public void setSkills(List<SkillRef> skills) {
    this.skills = skills;
  }

  // 获取技能快照版本号。
  public long getVersion() {
    return version;
  }

  // 设置技能快照版本号。
  public void setVersion(long version) {
    this.version = version;
  }
}
