package com.openclawlite.openclaw.domain.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 技能管理服务
 *
 * <p>负责扫描、解析和管理技能文件（SKILL.md），核心功能：</p>
 * <ul>
 *   <li>扫描 skills 目录，加载所有技能定义</li>
 *   <li>解析技能文件的 frontmatter 元数据</li>
 *   <li>构建技能快照，用于系统提示词生成</li>
 *   <li>检测技能更新，自动刷新快照</li>
 * </ul>
 *
 * <p>技能文件格式：</p>
 * <pre>
 * ---
 * name: 技能名称
 * description: 技能描述
 * user-invocable: true
 * disable-model-invocation: false
 * ---
 * 技能正文内容...
 * </pre>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
@Component
public class SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillService.class);

    /** 应用配置属性 */
    private final AppProperties properties;

    /** JSON 对象映射器（用于解析元数据） */
    private final ObjectMapper objectMapper;

    /**
     * 构造技能服务
     *
     * @param properties 应用配置
     */
    public SkillService(AppProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        log.debug("技能服务已初始化");
    }

    /**
     * 确保技能快照最新
     *
     * <p>检查技能文件是否有更新，如有则重新构建快照，否则复用现有快照。</p>
     * <p>版本检测基于 SKILL.md 文件的最后修改时间。</p>
     *
     * @param state 当前会话状态
     * @return 最新的技能快照
     */
    public SkillSnapshot ensureSnapshot(SessionState state) {
        Path skillsRoot = resolveSkillsRoot();
        long version = computeVersion(skillsRoot);

        log.debug("检查技能快照版本: currentVersion={}, latestVersion={}",
                 state.getSkillSnapshot() != null ? state.getSkillSnapshot().getVersion() : "null",
                 version);

        SkillSnapshot current = state.getSkillSnapshot();
        if (current == null || current.getVersion() != version) {
            log.info("检测到技能更新，重新构建快照");
            SkillSnapshot next = buildSnapshot(skillsRoot, version);
            state.setSkillSnapshot(next);
            return next;
        }

        log.debug("使用现有技能快照");
        return current;
    }

    /**
     * 构建技能快照
     *
     * <p>加载所有技能并生成：</p>
     * <ul>
     *   <li>技能引用列表（用于 UI 展示）</li>
     *   <li>系统提示词片段（插入到提示词中）</li>
     *   <li>版本号（用于更新检测）</li>
     * </ul>
     *
     * @param skillsRoot 技能根目录
     * @param version 版本号
     * @return 技能快照对象
     */
    public SkillSnapshot buildSnapshot(Path skillsRoot, long version) {
        log.debug("构建技能快照: root={}, version={}", skillsRoot, version);

        // 加载所有技能
        List<Skill> skills = loadSkills(skillsRoot);
        log.info("已加载 {} 个技能", skills.size());

        // 构建提示词
        String prompt = buildPrompt(skills);
        log.debug("技能提示词长度: {} 字符", prompt.length());

        // 构建技能引用列表
        List<SkillRef> refs = skills.stream()
                .map(skill -> new SkillRef(
                        skill.getName(),
                        skill.getDescription(),
                        skill.getEmoji(),
                        resolveSkillLocation(skill),
                        skill.isUserInvocable(),
                        skill.isDisableModelInvocation()))
                .collect(Collectors.toList());

        return new SkillSnapshot(prompt, refs, version);
    }

    /**
     * 解析技能根目录路径
     *
     * <p>支持相对路径和绝对路径，相对路径基于工作区目录。</p>
     *
     * @return 规范化后的绝对路径
     */
    private Path resolveSkillsRoot() {
        Path workspace = Path.of(properties.getWorkspaceDir()).toAbsolutePath().normalize();
        Path skillsDir = Path.of(properties.getSkillsDir());

        if (skillsDir.isAbsolute()) {
            return skillsDir.normalize();
        }

        return workspace.resolve(skillsDir).normalize();
    }

    /**
     * 计算技能目录的版本号
     *
     * <p>使用最新的 SKILL.md 文件修改时间作为版本号。</p>
     *
     * @param skillsRoot 技能根目录
     * @return 版本号（时间戳毫秒）
     */
    private long computeVersion(Path skillsRoot) {
        if (!Files.exists(skillsRoot)) {
            return 0L;
        }

        try {
            return Files.walk(skillsRoot)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase("SKILL.md"))
                    .map(path -> path.toFile().lastModified())
                    .max(Comparator.naturalOrder())
                    .orElse(0L);
        } catch (IOException e) {
            log.error("计算技能版本失败", e);
            return 0L;
        }
    }

    /**
     * 加载所有技能
     *
     * <p>扫描技能目录，读取并解析所有 SKILL.md 文件。</p>
     *
     * @param skillsRoot 技能根目录
     * @return 技能对象列表
     */
    private List<Skill> loadSkills(Path skillsRoot) {
        if (!Files.exists(skillsRoot)) {
            log.warn("技能目录不存在: {}", skillsRoot);
            return List.of();
        }

        List<Skill> skills = new ArrayList<>();
        try {
            // 查找所有 SKILL.md 文件
            List<Path> files = Files.walk(skillsRoot)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase("SKILL.md"))
                    .sorted()
                    .collect(Collectors.toList());

            log.debug("找到 {} 个技能文件", files.size());

            // 逐个解析技能文件（解析 frontmatter + 正文）
            for (Path skillFile : files) {
                log.trace("解析技能文件: {}", skillFile);

                String content = Files.readString(skillFile);
                FrontmatterParseResult parsed = parseFrontmatterBlock(content);

                // 解析基础字段
                String frontmatterName = parsed.frontmatter.get("name");
                String name = hasText(frontmatterName)
                        ? frontmatterName.trim()
                        : skillFile.getParent().getFileName().toString();

                String frontmatterDescription = parsed.frontmatter.get("description");
                String cleaned = parsed.body == null ? "" : parsed.body.trim();
                String description = hasText(frontmatterDescription)
                        ? frontmatterDescription.trim()
                        : firstNonEmptyLine(cleaned);

                boolean userInvocable = parseBoolean(parsed.frontmatter.get("user-invocable"), true);
                boolean disableModelInvocation =
                        parseBoolean(parsed.frontmatter.get("disable-model-invocation"), false);

                // 解析扩展 metadata
                SkillMetadata metadata = parseSkillMetadata(parsed.frontmatter);
                String skillKey = hasText(metadata.skillKey)
                        ? metadata.skillKey.trim()
                        : skillFile.getParent().getFileName().toString();

                // 创建包含所有字段的 Skill 对象
                Skill skill = new Skill(
                    name,
                    cleaned,
                    description,
                    userInvocable,
                    disableModelInvocation,
                    metadata.emoji,
                    metadata.homepage,
                    metadata.primaryEnv,
                    metadata.requires,
                    metadata.install,
                    metadata.os,
                    metadata.always,
                    skillKey,
                    metadata.commandDispatch,
                    metadata.commandTool,
                    metadata.commandArgMode
                );

                skills.add(skill);
                log.trace("技能已加载: {}", name);
            }

        } catch (IOException e) {
            log.error("加载技能失败: {}", skillsRoot, e);
            throw new IllegalStateException("Failed to load skills from " + skillsRoot, e);
        }

        return skills;
    }

    /**
     * 构建技能提示词
     *
     * <p>构建可用技能清单，提示模型按需读取 SKILL.md。</p>
     *
     * @param skills 技能列表
     * @return 提示词字符串
     */
    private String buildPrompt(List<Skill> skills) {
        if (skills.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        int eligibleCount = 0;

        builder.append("<available_skills>\n");

        for (Skill skill : skills) {
            if (skill.isDisableModelInvocation()) {
                continue;
            }

            String name = hasText(skill.getName()) ? skill.getName().trim() : "unknown";
            String description = hasText(skill.getDescription())
                    ? skill.getDescription().trim()
                    : "无描述";
            String location = resolveSkillLocation(skill);

            builder.append("  <skill>\n");
            builder.append("    <name>").append(escapeXml(name)).append("</name>\n");
            builder.append("    <description>").append(escapeXml(description)).append("</description>\n");
            builder.append("    <location>").append(escapeXml(location)).append("</location>\n");
            builder.append("  </skill>\n");
            eligibleCount++;
        }

        builder.append("</available_skills>");
        return eligibleCount > 0 ? builder.toString().trim() : "";
    }

    private String resolveSkillLocation(Skill skill) {
        String key = hasText(skill.getSkillKey()) ? skill.getSkillKey().trim() : skill.getName();
        if (!hasText(key)) {
            return "skills/unknown/SKILL.md";
        }

        String normalized = key.replace("\\", "/");
        if (normalized.startsWith("skills/")) {
            normalized = normalized.substring("skills/".length());
        }
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/SKILL.md")) {
            return "skills/" + normalized;
        }
        return "skills/" + normalized + "/SKILL.md";
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * 解析 Frontmatter 块
     *
     * <p>提取文件开头的 YAML 格式元数据，分离出 frontmatter 和正文。</p>
     *
     * @param content 文件完整内容
     * @return 解析结果（frontmatter + body）
     */
    private FrontmatterParseResult parseFrontmatterBlock(String content) {
        if (content == null || !content.startsWith("---")) {
            return new FrontmatterParseResult(new HashMap<>(), content);
        }

        Map<String, String> frontmatter = new HashMap<>();
        StringBuilder body = new StringBuilder();
        boolean inFrontmatter = true;

        try (var reader = new java.io.BufferedReader(new StringReader(content))) {
            String line = reader.readLine(); // consume leading ---
            while ((line = reader.readLine()) != null) {
                if (inFrontmatter) {
                    if (line.trim().equals("---")) {
                        inFrontmatter = false;
                        continue;
                    }
                    parseFrontmatterLine(frontmatter, line);
                } else {
                    body.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            log.warn("解析 frontmatter 失败，使用原始内容", e);
            return new FrontmatterParseResult(frontmatter, content);
        }

        return new FrontmatterParseResult(frontmatter, body.toString());
    }

    /**
     * 解析单行 frontmatter 的 key/value
     *
     * @param frontmatter frontmatter 映射表
     * @param line 当前行文本
     */
    private void parseFrontmatterLine(Map<String, String> frontmatter, String line) {
        if (line == null) {
            return;
        }

        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        int idx = trimmed.indexOf(':');
        if (idx <= 0) {
            return;
        }

        String key = trimmed.substring(0, idx).trim().toLowerCase();
        String value = trimmed.substring(idx + 1).trim();

        // 移除引号
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }

        frontmatter.put(key, value);
    }

    /**
     * 解析布尔值字符串
     *
     * @param raw 原始字符串
     * @param fallback 默认值
     * @return 布尔值
     */
    private boolean parseBoolean(String raw, boolean fallback) {
        if (!hasText(raw)) {
            return fallback;
        }

        String value = raw.trim().toLowerCase();
        return value.equals("true")
                || value.equals("1")
                || value.equals("yes")
                || value.equals("y");
    }

    /**
     * 提取正文中第一行非空内容
     *
     * @param content 正文内容
     * @return 第一行非空文本
     */
    private String firstNonEmptyLine(String content) {
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return "";
    }

    /**
     * 解析技能元数据
     *
     * <p>解析 metadata 字段中的复杂对象，包含依赖、安装规范等信息。</p>
     *
     * @param frontmatter frontmatter 映射表
     * @return 技能元数据对象
     */
    private SkillMetadata parseSkillMetadata(Map<String, String> frontmatter) {
        String metadataRaw = frontmatter.get("metadata");
        if (!hasText(metadataRaw)) {
            return new SkillMetadata();
        }

        try {
            // 尝试解析 JSON
            Map<String, Object> metadataObj = objectMapper.readValue(
                metadataRaw,
                new TypeReference<Map<String, Object>>() {}
            );

            // 查找 openclaw 键（支持多个候选键名）
            String[] candidateKeys = {"openclaw", "openclaw-lite", "claw"};
            Map<String, Object> openClawData = null;

            for (String key : candidateKeys) {
                Object value = metadataObj.get(key);
                if (value instanceof Map) {
                    openClawData = (Map<String, Object>) value;
                    break;
                }
            }

            if (openClawData == null) {
                openClawData = metadataObj; // 直接使用根对象
            }

            SkillMetadata meta = new SkillMetadata();

            // 解析基础字段
            meta.emoji = getString(openClawData, "emoji");
            meta.homepage = getString(openClawData, "homepage");
            meta.primaryEnv = getString(openClawData, "primaryEnv");
            meta.skillKey = getString(openClawData, "skillKey");
            meta.always = getBoolean(openClawData, "always", false);
            meta.commandDispatch = getString(openClawData, "command-dispatch");
            meta.commandTool = getString(openClawData, "command-tool");
            meta.commandArgMode = getString(openClawData, "command-arg-mode");

            // 解析 requires 对象
            Object requiresObj = openClawData.get("requires");
            if (requiresObj instanceof Map) {
                Map<?, ?> requiresMap = (Map<?, ?>) requiresObj;
                meta.requires = new Skill.RequiresMetadata();
                meta.requires.setBins(parseStringList(requiresMap.get("bins")));
                meta.requires.setAnyBins(parseStringList(requiresMap.get("anyBins")));
                meta.requires.setEnv(parseStringList(requiresMap.get("env")));
                meta.requires.setConfig(parseStringList(requiresMap.get("config")));
            }

            // 解析 install 数组
            Object installObj = openClawData.get("install");
            if (installObj instanceof List) {
                List<?> installList = (List<?>) installObj;
                meta.install = new ArrayList<>();
                for (Object item : installList) {
                    if (item instanceof Map) {
                        meta.install.add(parseInstallSpec((Map<?, ?>) item));
                    }
                }
            }

            // 解析 os 数组
            meta.os = parseStringList(openClawData.get("os"));

            return meta;

        } catch (JsonProcessingException e) {
            log.warn("解析技能元数据失败，使用默认值: {}", e.getMessage());
            // JSON 解析失败，返回空 metadata
            return new SkillMetadata();
        }
    }

    /**
     * 解析安装规范
     *
     * @param raw 原始映射数据
     * @return 安装规范对象
     */
    private Skill.InstallSpec parseInstallSpec(Map<?, ?> raw) {
        Skill.InstallSpec spec = new Skill.InstallSpec();
        spec.setId(getString(raw, "id"));
        spec.setKind(getString(raw, "kind"));
        spec.setLabel(getString(raw, "label"));
        spec.setBins(parseStringList(raw.get("bins")));
        spec.setOs(parseStringList(raw.get("os")));
        spec.setFormula(getString(raw, "formula"));
        spec.setPackage(getString(raw, "package"));
        spec.setModule(getString(raw, "module"));
        spec.setUrl(getString(raw, "url"));
        spec.setArchive(getString(raw, "archive"));
        spec.setExtract(getBoolean(raw, "extract", false));
        spec.setStripComponents(getInt(raw, "stripComponents", 0));
        spec.setTargetDir(getString(raw, "targetDir"));
        return spec;
    }

    /**
     * 从 Map 中安全获取字符串值
     *
     * @param map 数据映射
     * @param key 键名
     * @return 字符串值，不存在则返回 null
     */
    private String getString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    /**
     * 从 Map 中安全获取布尔值
     *
     * @param map 数据映射
     * @param key 键名
     * @param fallback 默认值
     * @return 布尔值
     */
    private boolean getBoolean(Map<?, ?> map, String key, boolean fallback) {
        Object value = map.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = String.valueOf(value).trim().toLowerCase();
        return str.equals("true") || str.equals("1") || str.equals("yes");
    }

    /**
     * 从 Map 中安全获取整数值
     *
     * @param map 数据映射
     * @param key 键名
     * @param fallback 默认值
     * @return 整数值
     */
    private int getInt(Map<?, ?> map, String key, int fallback) {
        Object value = map.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /**
     * 解析字符串列表
     *
     * <p>支持两种格式：</p>
     * <ul>
     *   <li>数组：["item1", "item2"]</li>
     *   <li>逗号分隔字符串："item1,item2"</li>
     * </ul>
     *
     * @param value 原始值
     * @return 字符串列表
     */
    private List<String> parseStringList(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }

        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (item != null) {
                    String str = String.valueOf(item).trim();
                    if (!str.isEmpty()) {
                        result.add(str);
                    }
                }
            }
            return result;
        }

        if (value instanceof String) {
            String[] parts = ((String) value).split(",");
            List<String> result = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        }

        return new ArrayList<>();
    }

    /**
     * 判断字符串是否有有效内容
     *
     * @param value 字符串值
     * @return true 表示非空且非空白
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // ========== 内部类 ==========

    /**
     * 技能元数据容器（临时解析用）
     */
    private static class SkillMetadata {
        String emoji;
        String homepage;
        String primaryEnv;
        Skill.RequiresMetadata requires;
        List<Skill.InstallSpec> install;
        List<String> os;
        boolean always;
        String skillKey;
        String commandDispatch;
        String commandTool;
        String commandArgMode;
    }

    /**
     * Frontmatter 解析结果容器
     */
    private static class FrontmatterParseResult {
        private final Map<String, String> frontmatter;
        private final String body;

        private FrontmatterParseResult(Map<String, String> frontmatter, String body) {
            this.frontmatter = frontmatter;
            this.body = body;
        }
    }
}
