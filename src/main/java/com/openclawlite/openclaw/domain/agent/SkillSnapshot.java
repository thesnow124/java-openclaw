package com.openclawlite.openclaw.domain.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能快照
 *
 * <p>技能快照是技能系统在某一时刻的完整视图，用于：</p>
 * <ul>
 *   <li>检测技能更新（通过版本号比较）</li>
 *   <li>生成系统提示词（可用技能清单）</li>
 *   <li>提供技能引用列表（用于 UI 展示和查询）</li>
 * </ul>
 *
 * <p>版本控制：</p>
 * <ul>
 *   <li>版本号基于 SKILL.md 文件的最后修改时间</li>
 *   <li>任何技能文件更新都会导致版本号变化</li>
 *   <li>会话中缓存快照，仅在版本变化时重建</li>
 * </ul>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
public class SkillSnapshot {

    /** 技能提示词片段（available_skills 清单，将插入到系统提示词中） */
    private String prompt;

    /** 技能引用列表（用于 UI 展示和查询） */
    private List<SkillRef> skills = new ArrayList<>();

    /** 快照版本号（基于技能文件的最后修改时间） */
    private long version;

    /**
     * 默认构造器（用于反序列化）
     */
    public SkillSnapshot() {}

    /**
     * 构造完整的技能快照
     *
     * @param prompt 拼接后的提示词内容
     * @param skills 技能引用列表
     * @param version 版本号
     */
    public SkillSnapshot(String prompt, List<SkillRef> skills, long version) {
        this.prompt = prompt;
        this.skills = skills;
        this.version = version;
    }

    /**
     * 获取拼接后的技能提示词内容
     *
     * @return 提示词字符串
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * 设置拼接后的技能提示词内容
     *
     * @param prompt 提示词字符串
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * 获取技能引用列表
     *
     * @return 技能引用列表
     */
    public List<SkillRef> getSkills() {
        return skills;
    }

    /**
     * 获取技能引用列表（别名方法）
     *
     * @return 技能引用列表
     */
    public List<SkillRef> getSkillRefs() {
        return skills;
    }

    /**
     * 设置技能引用列表
     *
     * @param skills 技能引用列表
     */
    public void setSkills(List<SkillRef> skills) {
        this.skills = skills;
    }

    /**
     * 获取技能快照版本号
     *
     * @return 版本号（时间戳毫秒）
     */
    public long getVersion() {
        return version;
    }

    /**
     * 设置技能快照版本号
     *
     * @param version 版本号
     */
    public void setVersion(long version) {
        this.version = version;
    }
}
