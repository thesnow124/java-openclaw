package com.openclawlite.openclaw.domain.agent;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能对象
 *
 * <p>代表一个 AI 技能，包含技能的完整信息：</p>
 * <ul>
 *   <li>基础信息：名称、描述、内容</li>
 *   <li>调用策略：是否允许用户/模型调用</li>
 *   <li>扩展元数据：依赖、安装规范、环境要求等</li>
 * </ul>
 *
 * <p>技能定义文件（SKILL.md）包含两部分：</p>
 * <ol>
 *   <li>Frontmatter：YAML 格式的元数据</li>
 *   <li>正文：Markdown 格式的技能说明</li>
 * </ol>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
public class Skill {

    // ========== 基础字段 ==========

    /** 技能名称（通常使用目录名） */
    private final String name;

    /** 技能正文内容（Markdown 格式） */
    private final String content;

    /** 技能简短描述 */
    private final String description;

    /** 是否允许用户显式调用该技能 */
    private final boolean userInvocable;

    /** 是否禁止模型自动调用该技能 */
    private final boolean disableModelInvocation;

    // ========== 扩展元数据字段 ==========

    /** 技能表情符号（用于 UI 展示） */
    private final String emoji;

    /** 技能主页 URL */
    private final String homepage;

    /** 主要环境变量名 */
    private final String primaryEnv;

    /** 依赖要求元数据 */
    private final RequiresMetadata requires;

    /** 安装规范列表 */
    private final List<InstallSpec> install;

    /** 支持的操作系统列表 */
    private final List<String> os;

    /** 是否总是加载该技能（跳过依赖检查） */
    private final boolean always;

    /** 自定义技能键（用于特殊处理） */
    private final String skillKey;

    /** 命令分发模式（tool） */
    private final String commandDispatch;

    /** 命令工具名称 */
    private final String commandTool;

    /** 命令参数模式（raw/structured） */
    private final String commandArgMode;

    /**
     * 扩展构造器：包含所有字段
     *
     * @param name 技能名称
     * @param content 技能内容
     * @param description 技能描述
     * @param userInvocable 是否允许用户调用
     * @param disableModelInvocation 是否禁止模型调用
     * @param emoji 表情符号
     * @param homepage 主页 URL
     * @param primaryEnv 主要环境变量
     * @param requires 依赖要求
     * @param install 安装规范列表
     * @param os 支持的操作系统列表
     * @param always 是否总是加载
     * @param skillKey 技能键
     * @param commandDispatch 命令分发模式
     * @param commandTool 命令工具
     * @param commandArgMode 命令参数模式
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
     * 兼容旧版构造器：仅包含基础字段
     *
     * @param name 技能名称
     * @param content 技能内容
     * @param description 技能描述
     * @param userInvocable 是否允许用户调用
     * @param disableModelInvocation 是否禁止模型调用
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

    /**
     * 获取技能名称
     *
     * @return 技能名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取技能正文内容
     *
     * @return Markdown 格式的技能说明
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取技能简短描述
     *
     * @return 技能描述文本
     */
    public String getDescription() {
        return description;
    }

    /**
     * 是否允许用户显式调用该技能
     *
     * @return true 表示用户可以显式调用
     */
    public boolean isUserInvocable() {
        return userInvocable;
    }

    /**
     * 是否禁止模型自动调用该技能
     *
     * @return true 表示禁止模型自动调用
     */
    public boolean isDisableModelInvocation() {
        return disableModelInvocation;
    }

    // ========== 扩展元数据 Getter ==========

    /**
     * 获取技能表情符号
     *
     * @return Emoji 字符串
     */
    public String getEmoji() {
        return emoji;
    }

    /**
     * 获取技能主页 URL
     *
     * @return 主页链接
     */
    public String getHomepage() {
        return homepage;
    }

    /**
     * 获取主要环境变量名
     *
     * @return 环境变量名
     */
    public String getPrimaryEnv() {
        return primaryEnv;
    }

    /**
     * 获取依赖要求
     *
     * @return 依赖元数据对象
     */
    public RequiresMetadata getRequires() {
        return requires;
    }

    /**
     * 获取安装规范列表
     *
     * @return 安装规范列表
     */
    public List<InstallSpec> getInstall() {
        return install;
    }

    /**
     * 获取支持的操作系统列表
     *
     * @return 操作系统名称列表
     */
    public List<String> getOs() {
        return os;
    }

    /**
     * 是否总是加载该技能（跳过依赖检查）
     *
     * @return true 表示总是加载
     */
    public boolean isAlways() {
        return always;
    }

    /**
     * 获取自定义技能键
     *
     * @return 技能键字符串
     */
    public String getSkillKey() {
        return skillKey;
    }

    /**
     * 获取命令分发模式
     *
     * @return 分发模式（如 "tool"）
     */
    public String getCommandDispatch() {
        return commandDispatch;
    }

    /**
     * 获取命令工具名称
     *
     * @return 工具名称
     */
    public String getCommandTool() {
        return commandTool;
    }

    /**
     * 获取命令参数模式
     *
     * @return 参数模式（raw/structured）
     */
    public String getCommandArgMode() {
        return commandArgMode;
    }

    // ========== 内部类 ==========

    /**
     * 依赖要求元数据
     *
     * <p>定义技能运行所需的依赖项。</p>
     */
      public static class RequiresMetadata {
        /** 需要的可执行文件列表（全部必须存在） */
        private List<String> bins = new ArrayList<>();

        /** 需要的可执行文件列表（至少一个存在） */
        private List<String> anyBins = new ArrayList<>();

        /** 需要的环境变量列表 */
        private List<String> env = new ArrayList<>();

        /** 需要的配置项列表 */
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
     * 安装规范
     *
     * <p>定义技能的安装方式，支持多种包管理器和安装方法。</p>
     */
      public static class InstallSpec {
        /** 安装规范唯一标识 */
        private String id;

        /** 安装类型：brew, node, go, uv, download, apt 等 */
        private String kind;

        /** 安装项标签（用于 UI 展示） */
        private String label;

        /** 安装后提供的可执行文件列表 */
        private List<String> bins = new ArrayList<>();

        /** 支持的操作系统列表 */
        private List<String> os = new ArrayList<>();

        /** Homebrew formula 名称 */
        private String formula;

        /** 包名 */
        private String package_;

        /** 模块名（如 Python 模块） */
        private String module;

        /** 下载 URL */
        private String url;

        /** 压缩文件路径 */
        private String archive;

        /** 是否需要解压 */
        private boolean extract;

        /** 解压时去掉的目录层数 */
        private int stripComponents;

        /** 目标安装目录 */
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
