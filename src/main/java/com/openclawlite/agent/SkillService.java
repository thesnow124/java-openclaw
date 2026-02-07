package com.openclawlite.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclawlite.config.AppProperties;
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

@Component
// 扫描 skills 目录并构建技能快照。
public class SkillService {
    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    // 注入配置以解析 skills 路径。
    public SkillService(AppProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    // 若 skills 有更新则重新构建快照，否则复用旧快照。
    public SkillSnapshot ensureSnapshot(SessionState state) {
        Path skillsRoot = resolveSkillsRoot();
        long version = computeVersion(skillsRoot);
        SkillSnapshot current = state.getSkillSnapshot();
        if (current == null || current.getVersion() != version) {
            SkillSnapshot next = buildSnapshot(skillsRoot, version);
            state.setSkillSnapshot(next);
            return next;
        }
        return current;
    }

    // 构建技能列表与系统提示词片段。
    public SkillSnapshot buildSnapshot(Path skillsRoot, long version) {
        List<Skill> skills = loadSkills(skillsRoot);
        String prompt = buildPrompt(skills);
        List<SkillRef> refs = skills.stream()
                .map(
                        skill ->
                                new SkillRef(
                                        skill.getName(),
                                        skill.getDescription(),
                                        skill.getEmoji(),
                                        skill.isUserInvocable(),
                                        skill.isDisableModelInvocation()))
                .collect(Collectors.toList());
        return new SkillSnapshot(prompt, refs, version);
    }

    // 解析 skills 根目录（支持相对与绝对路径）。
    private Path resolveSkillsRoot() {
        Path workspace = Path.of(properties.getWorkspaceDir()).toAbsolutePath().normalize();
        Path skillsDir = Path.of(properties.getSkillsDir());
        if (skillsDir.isAbsolute()) {
            return skillsDir.normalize();
        }
        return workspace.resolve(skillsDir).normalize();
    }

    // 以最新的 SKILL.md 修改时间作为版本号。
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
            return 0L;
        }
    }

    // 读取所有 SKILL.md 并构造成 Skill 对象列表。
    private List<Skill> loadSkills(Path skillsRoot) {
        if (!Files.exists(skillsRoot)) {
            return List.of();
        }
        List<Skill> skills = new ArrayList<>();
        try {
            List<Path> files = Files.walk(skillsRoot)
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase("SKILL.md"))
                    .sorted()
                    .collect(Collectors.toList());
            // 逐个解析技能文件（解析 frontmatter + 正文）。
            for (Path skillFile : files) {
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
                    metadata.skillKey,
                    metadata.commandDispatch,
                    metadata.commandTool,
                    metadata.commandArgMode
                );
                skills.add(skill);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load skills from " + skillsRoot, e);
        }
        return skills;
    }

    // 将所有技能内容拼接为系统提示词片段。
    private String buildPrompt(List<Skill> skills) {
        if (skills.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean appended = false;
        for (Skill skill : skills) {
            if (skill.isDisableModelInvocation()) {
                continue;
            }
            if (!appended) {
                builder.append("技能列表：\n");
                appended = true;
            }
            builder.append("## ").append(skill.getName()).append("\n");
            builder.append(skill.getContent()).append("\n\n");
        }
        return appended ? builder.toString().trim() : "";
    }

    // 解析 frontmatter 并返回正文内容。
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
            return new FrontmatterParseResult(frontmatter, content);
        }
        return new FrontmatterParseResult(frontmatter, body.toString());
    }

    // 解析单行 frontmatter 的 key/value。
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
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        frontmatter.put(key, value);
    }

    // 解析布尔值字符串。
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

    // 取正文中第一行非空内容作为简短描述。
    private String firstNonEmptyLine(String content) {
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return "";
    }

    // 解析复杂的 metadata.openclaw 对象。
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
            // JSON 解析失败，返回空 metadata
            return new SkillMetadata();
        }
    }

    // 解析安装规范。
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

    // 从 Map 中安全获取字符串值。
    private String getString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    // 从 Map 中安全获取布尔值。
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

    // 从 Map 中安全获取整数值。
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

    // 解析字符串列表（支持数组或逗号分隔的字符串）。
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

    // 判断字符串是否有有效内容。
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // 技能元数据容器。
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

    // frontmatter 解析结果容器。
    private static class FrontmatterParseResult {
        private final Map<String, String> frontmatter;
        private final String body;

        private FrontmatterParseResult(Map<String, String> frontmatter, String body) {
            this.frontmatter = frontmatter;
            this.body = body;
        }
    }
}
