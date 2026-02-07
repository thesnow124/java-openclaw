package com.openclawlite.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能对象：包含名称、正文、简述、调用策略和扩展元数据。
 */
public class Skill {
  // 基础字段
  private final String name;
  private final String content;
  private final String description;
  private final boolean userInvocable;
  private final boolean disableModelInvocation;

  // 扩展元数据字段
  private final String emoji;
  private final String homepage;
  private final String primaryEnv;
  private final RequiresMetadata requires;
  private final List<InstallSpec> install;
  private final List<String> os;
  private final boolean always;
  private final String skillKey;
  private final String commandDispatch;
  private final String commandTool;
  private final String commandArgMode;

  /**
   * 扩展构造器：包含所有字段。
   */
  public Skill(
      String name,
      String content,
      String description,
      boolean userInvocable,
      boolean disableModelInvocation,
      String emoji,
      String homepage,
      String primaryEnv,
      RequiresMetadata requires,
      List<InstallSpec> install,
      List<String> os,
      boolean always,
      String skillKey,
      String commandDispatch,
      String commandTool,
      String commandArgMode) {
    this.name = name;
    this.content = content;
    this.description = description;
    this.userInvocable = userInvocable;
    this.disableModelInvocation = disableModelInvocation;
    this.emoji = emoji;
    this.homepage = homepage;
    this.primaryEnv = primaryEnv;
    this.requires = requires != null ? requires : new RequiresMetadata();
    this.install = install != null ? install : new ArrayList<>();
    this.os = os != null ? os : new ArrayList<>();
    this.always = always;
    this.skillKey = skillKey;
    this.commandDispatch = commandDispatch;
    this.commandTool = commandTool;
    this.commandArgMode = commandArgMode;
  }

  /**
   * 兼容旧版构造器：仅包含基础字段。
   */
  public Skill(
      String name,
      String content,
      String description,
      boolean userInvocable,
      boolean disableModelInvocation) {
    this(name, content, description, userInvocable, disableModelInvocation,
         null, null, null, null, null, null, false, null, null, null, null);
  }

  // ========== 基础字段 Getter ==========

  /** 获取技能名称（目录名）。 */
  public String getName() {
    return name;
  }

  /** 获取技能正文内容。 */
  public String getContent() {
    return content;
  }

  /** 获取技能简短描述。 */
  public String getDescription() {
    return description;
  }

  /** 是否允许用户显式调用该技能。 */
  public boolean isUserInvocable() {
    return userInvocable;
  }

  /** 是否禁止模型自动调用该技能。 */
  public boolean isDisableModelInvocation() {
    return disableModelInvocation;
  }

  // ========== 扩展元数据 Getter ==========

  /** 获取技能表情符号。 */
  public String getEmoji() {
    return emoji;
  }

  /** 获取技能主页 URL。 */
  public String getHomepage() {
    return homepage;
  }

  /** 获取主要环境变量名。 */
  public String getPrimaryEnv() {
    return primaryEnv;
  }

  /** 获取依赖要求。 */
  public RequiresMetadata getRequires() {
    return requires;
  }

  /** 获取安装规范列表。 */
  public List<InstallSpec> getInstall() {
    return install;
  }

  /** 获取支持的操作系统列表。 */
  public List<String> getOs() {
    return os;
  }

  /** 是否总是加载该技能（跳过依赖检查）。 */
  public boolean isAlways() {
    return always;
  }

  /** 获取自定义技能键。 */
  public String getSkillKey() {
    return skillKey;
  }

  /** 获取命令分发模式（tool）。 */
  public String getCommandDispatch() {
    return commandDispatch;
  }

  /** 获取命令工具名称。 */
  public String getCommandTool() {
    return commandTool;
  }

  /** 获取命令参数模式（raw/structured）。 */
  public String getCommandArgMode() {
    return commandArgMode;
  }

  // ========== 内部类 ==========

  /**
   * 依赖要求元数据。
   */
  public static class RequiresMetadata {
    private List<String> bins = new ArrayList<>();
    private List<String> anyBins = new ArrayList<>();
    private List<String> env = new ArrayList<>();
    private List<String> config = new ArrayList<>();

    public List<String> getBins() {
      return bins;
    }

    public void setBins(List<String> bins) {
      this.bins = bins != null ? bins : new ArrayList<>();
    }

    public List<String> getAnyBins() {
      return anyBins;
    }

    public void setAnyBins(List<String> anyBins) {
      this.anyBins = anyBins != null ? anyBins : new ArrayList<>();
    }

    public List<String> getEnv() {
      return env;
    }

    public void setEnv(List<String> env) {
      this.env = env != null ? env : new ArrayList<>();
    }

    public List<String> getConfig() {
      return config;
    }

    public void setConfig(List<String> config) {
      this.config = config != null ? config : new ArrayList<>();
    }
  }

  /**
   * 安装规范。
   */
  public static class InstallSpec {
    private String id;
    private String kind; // brew, node, go, uv, download, apt
    private String label;
    private List<String> bins = new ArrayList<>();
    private List<String> os = new ArrayList<>();
    private String formula;
    private String package_;
    private String module;
    private String url;
    private String archive;
    private boolean extract;
    private int stripComponents;
    private String targetDir;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getKind() {
      return kind;
    }

    public void setKind(String kind) {
      this.kind = kind;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public List<String> getBins() {
      return bins;
    }

    public void setBins(List<String> bins) {
      this.bins = bins != null ? bins : new ArrayList<>();
    }

    public List<String> getOs() {
      return os;
    }

    public void setOs(List<String> os) {
      this.os = os != null ? os : new ArrayList<>();
    }

    public String getFormula() {
      return formula;
    }

    public void setFormula(String formula) {
      this.formula = formula;
    }

    public String getPackage() {
      return package_;
    }

    public void setPackage(String package_) {
      this.package_ = package_;
    }

    public String getModule() {
      return module;
    }

    public void setModule(String module) {
      this.module = module;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getArchive() {
      return archive;
    }

    public void setArchive(String archive) {
      this.archive = archive;
    }

    public boolean isExtract() {
      return extract;
    }

    public void setExtract(boolean extract) {
      this.extract = extract;
    }

    public int getStripComponents() {
      return stripComponents;
    }

    public void setStripComponents(int stripComponents) {
      this.stripComponents = stripComponents;
    }

    public String getTargetDir() {
      return targetDir;
    }

    public void setTargetDir(String targetDir) {
      this.targetDir = targetDir;
    }
  }
}
