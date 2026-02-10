package com.openclawlite.openclaw.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用配置属性
 * <p>
 * 控制台代理运行时的可配置路径与限制。
 * 通过 application.properties 或 application.yml 配置文件进行配置。
 * </p>
 *
 * <p>配置前缀：app</p>
 *
 * <p>主要配置项：</p>
 * <ul>
 *   <li>工作空间和文件路径配置</li>
 *   <li>会话和上下文管理配置</li>
 *   <li>工具和插件配置</li>
 *   <li>安全配置</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {
  /** 解析相对路径的工作区根目录 */
  private String workspaceDir = ".";

  /** 会话存档 JSON 文件路径 */
  private String sessionPath = "data/session.json";

  /** skills 目录（包含 SKILL.md） */
  private String skillsDir = "skills";

  /** 每个回合允许的工具调用上限 */
  private int maxToolSteps = 100;

  /** 模型上下文窗口上限（近似 token 数） */
  private int contextTokens = 8000;

  /** 触发"接近上限"提示的比例（0-1） */
  private double contextWarnRatio = 0.8;

  /** 压缩后目标 token 数（近似） */
  private int compactionTargetTokens = 2000;

  /** 压缩时保留最近的消息条数 */
  private int compactionKeepMessages = 6;

  /** 压缩输入的最大字符数（避免过大提示） */
  private int compactionInputMaxChars = 12000;

  /** 工具插件目录（包含 *.json 定义） */
  private String toolsDir = "tools";

  /** 是否允许执行命令型插件工具 */
  private boolean enableCommandTools = false;

  /** 是否允许全局文件系统访问（危险！仅用于可信环境） */
  private boolean allowGlobalAccess = false;

  /** 技能配置（环境变量覆盖等） */
  private SkillConfig skillConfig = new SkillConfig();

  // 获取工作区根目录。
  public String getWorkspaceDir() {
    return workspaceDir;
  }

  // 设置工作区根目录。
  public void setWorkspaceDir(String workspaceDir) {
    this.workspaceDir = workspaceDir;
  }

  // 获取会话存档路径。
  public String getSessionPath() {
    return sessionPath;
  }

  // 设置会话存档路径。
  public void setSessionPath(String sessionPath) {
    this.sessionPath = sessionPath;
  }

  // 获取 skills 目录。
  public String getSkillsDir() {
    return skillsDir;
  }

  // 设置 skills 目录。
  public void setSkillsDir(String skillsDir) {
    this.skillsDir = skillsDir;
  }

  // 获取工具循环最大步数。
  public int getMaxToolSteps() {
    return maxToolSteps;
  }

  // 设置工具循环最大步数。
  public void setMaxToolSteps(int maxToolSteps) {
    this.maxToolSteps = maxToolSteps;
  }

  // 获取上下文窗口上限（近似 token）。
  public int getContextTokens() {
    return contextTokens;
  }

  // 设置上下文窗口上限（近似 token）。
  public void setContextTokens(int contextTokens) {
    this.contextTokens = contextTokens;
  }

  // 获取触发提示的比例阈值。
  public double getContextWarnRatio() {
    return contextWarnRatio;
  }

  // 设置触发提示的比例阈值。
  public void setContextWarnRatio(double contextWarnRatio) {
    this.contextWarnRatio = contextWarnRatio;
  }

  // 获取压缩后目标 token 数。
  public int getCompactionTargetTokens() {
    return compactionTargetTokens;
  }

  // 设置压缩后目标 token 数。
  public void setCompactionTargetTokens(int compactionTargetTokens) {
    this.compactionTargetTokens = compactionTargetTokens;
  }

  // 获取压缩时保留的最近消息条数。
  public int getCompactionKeepMessages() {
    return compactionKeepMessages;
  }

  // 设置压缩时保留的最近消息条数。
  public void setCompactionKeepMessages(int compactionKeepMessages) {
    this.compactionKeepMessages = compactionKeepMessages;
  }

  // 获取压缩输入的最大字符数。
  public int getCompactionInputMaxChars() {
    return compactionInputMaxChars;
  }

  // 设置压缩输入的最大字符数。
  public void setCompactionInputMaxChars(int compactionInputMaxChars) {
    this.compactionInputMaxChars = compactionInputMaxChars;
  }

  // 获取工具插件目录。
  public String getToolsDir() {
    return toolsDir;
  }

  // 设置工具插件目录。
  public void setToolsDir(String toolsDir) {
    this.toolsDir = toolsDir;
  }

  // 是否允许执行命令型工具。
  public boolean isEnableCommandTools() {
    return enableCommandTools;
  }

  // 设置是否允许执行命令型工具。
  public void setEnableCommandTools(boolean enableCommandTools) {
    this.enableCommandTools = enableCommandTools;
  }

  // 是否允许全局文件系统访问。
  public boolean isAllowGlobalAccess() {
    return allowGlobalAccess;
  }

  // 设置是否允许全局文件系统访问。
  public void setAllowGlobalAccess(boolean allowGlobalAccess) {
    this.allowGlobalAccess = allowGlobalAccess;
  }

  // 获取技能配置。
  public SkillConfig getSkillConfig() {
    return skillConfig;
  }

  // 设置技能配置。
  public void setSkillConfig(SkillConfig skillConfig) {
    this.skillConfig = skillConfig != null ? skillConfig : new SkillConfig();
  }

  /**
   * 技能配置：支持环境变量覆盖等。
   */
  public static class SkillConfig {
    // 环境变量覆盖映射（技能名 -> 环境变量名 -> 值）
    private Map<String, Map<String, String>> skills = new HashMap<>();
    // 全局环境变量覆盖
    private Map<String, String> env = new HashMap<>();
    // 启用的技能列表（空表示全部启用）
    private List<String> enabled = new ArrayList<>();
    // 禁用的技能列表
    private List<String> disabled = new ArrayList<>();

    public Map<String, Map<String, String>> getSkills() {
      return skills;
    }

    public void setSkills(Map<String, Map<String, String>> skills) {
      this.skills = skills != null ? skills : new HashMap<>();
    }

    public Map<String, String> getEnv() {
      return env;
    }

    public void setEnv(Map<String, String> env) {
      this.env = env != null ? env : new HashMap<>();
    }

    public List<String> getEnabled() {
      return enabled;
    }

    public void setEnabled(List<String> enabled) {
      this.enabled = enabled != null ? enabled : new ArrayList<>();
    }

    public List<String> getDisabled() {
      return disabled;
    }

    public void setDisabled(List<String> disabled) {
      this.disabled = disabled != null ? disabled : new ArrayList<>();
    }
  }
}
