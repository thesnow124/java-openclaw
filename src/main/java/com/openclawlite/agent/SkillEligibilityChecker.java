package com.openclawlite.agent;

import com.openclawlite.config.AppProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 技能依赖检查器：验证技能是否满足加载条件。
 */
@Component
public class SkillEligibilityChecker {

    private final AppProperties properties;

    public SkillEligibilityChecker(AppProperties properties) {
        this.properties = properties;
    }

    /**
     * 综合检查技能是否满足所有依赖要求。
     *
     * @param skill 要检查的技能
     * @return true 如果技能可以加载，否则 false
     */
    public boolean isSkillEligible(Skill skill) {
        // always 标记的技能跳过所有检查
        if (skill.isAlways()) {
            return true;
        }

        // 操作系统兼容性检查
        if (!isOsCompatible(skill.getOs())) {
            return false;
        }

        Skill.RequiresMetadata requires = skill.getRequires();

        // 检查必需的二进制文件（AND 逻辑：所有都必须存在）
        for (String bin : requires.getBins()) {
            if (!hasBinary(bin)) {
                return false;
            }
        }

        // 检查可选的二进制文件（OR 逻辑：至少一个存在即可）
        if (!requires.getAnyBins().isEmpty()) {
            boolean anyFound = false;
            for (String bin : requires.getAnyBins()) {
                if (hasBinary(bin)) {
                    anyFound = true;
                    break;
                }
            }
            if (!anyFound) {
                return false;
            }
        }

        // 检查环境变量
        if (!hasRequiredEnv(requires.getEnv())) {
            return false;
        }

        // 检查配置项
        if (!hasRequiredConfig(requires.getConfig())) {
            return false;
        }

        return true;
    }

    /**
     * 检查操作系统兼容性。
     *
     * @param requiredOs 技能要求的操作系统列表
     * @return true 如果当前系统在兼容列表中，或列表为空
     */
    public boolean isOsCompatible(List<String> requiredOs) {
        if (requiredOs == null || requiredOs.isEmpty()) {
            return true; // 未指定操作系统要求，默认兼容
        }

        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        for (String os : requiredOs) {
            String normalized = os.toLowerCase();
            if (normalized.contains("darwin") || normalized.contains("mac") || normalized.contains("osx")) {
                if (osName.contains("mac") || osName.contains("darwin")) {
                    return true;
                }
            } else if (normalized.contains("linux")) {
                if (osName.contains("linux")) {
                    return true;
                }
            } else if (normalized.contains("windows") || normalized.contains("win")) {
                if (osName.contains("windows")) {
                    return true;
                }
            }
            // 通用匹配
            if (osName.contains(normalized)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查可执行文件是否在 PATH 中可用。
     *
     * @param bin 可执行文件名称
     * @return true 如果文件存在且可执行
     */
    public boolean hasBinary(String bin) {
        if (bin == null || bin.trim().isEmpty()) {
            return false;
        }

        // 首先尝试使用 ProcessBuilder 执行 which/where 命令
        try {
            ProcessBuilder pb;
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("windows")) {
                pb = new ProcessBuilder("where", bin);
            } else {
                pb = new ProcessBuilder("which", bin);
            }

            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            // 如果命令执行失败，尝试手动搜索 PATH
        }

        // 手动搜索 PATH 环境变量
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            return false;
        }

        String pathSeparator = System.getProperty("path.separator", ":");
        String[] pathDirs = pathEnv.split(pathSeparator);

        for (String dir : pathDirs) {
            if (dir == null || dir.trim().isEmpty()) {
                continue;
            }
            Path binaryPath = Paths.get(dir, bin);
            if (Files.exists(binaryPath) && Files.isExecutable(binaryPath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查所有必需的环境变量是否已设置。
     *
     * @param envVars 环境变量名称列表
     * @return true 如果所有变量都已设置
     */
    public boolean hasRequiredEnv(List<String> envVars) {
        if (envVars == null || envVars.isEmpty()) {
            return true;
        }

        for (String envVar : envVars) {
            if (envVar == null || envVar.trim().isEmpty()) {
                continue;
            }

            // 检查系统环境变量
            String value = System.getenv(envVar.trim());
            if (value != null && !value.trim().isEmpty()) {
                continue;
            }

            // 检查技能配置中的环境变量覆盖
            if (properties.getSkillConfig() != null) {
                Map<String, String> skillEnv = properties.getSkillConfig().getEnv();
                if (skillEnv != null && skillEnv.containsKey(envVar.trim())) {
                    String configValue = skillEnv.get(envVar.trim());
                    if (configValue != null && !configValue.trim().isEmpty()) {
                        continue;
                    }
                }
            }

            return false;
        }

        return true;
    }

    /**
     * 检查配置路径是否存在且为真值。
     *
     * @param configPaths 配置路径列表（点分隔的配置键）
     * @return true 如果所有配置路径都存在且为真
     */
    public boolean hasRequiredConfig(List<String> configPaths) {
        if (configPaths == null || configPaths.isEmpty()) {
            return true;
        }

        // 检查 channels.* 配置
        for (String configPath : configPaths) {
            if (configPath == null || configPath.trim().isEmpty()) {
                continue;
            }

            String normalized = configPath.trim();

            // 简化实现：只检查 channels 开头的配置
            if (normalized.startsWith("channels.")) {
                String channelKey = normalized.substring("channels.".length());
                // 这里可以扩展检查实际的通道配置
                // 目前简化为总是返回 true
                continue;
            }

            // 其他配置路径可以在此扩展
        }

        return true;
    }

    /**
     * 过滤技能列表，只返回满足依赖要求的技能。
     *
     * @param skills 技能列表
     * @return 满足依赖要求的技能列表
     */
    public List<Skill> filterEligibleSkills(List<Skill> skills) {
        List<Skill> eligible = new ArrayList<>();
        for (Skill skill : skills) {
            if (isSkillEligible(skill)) {
                eligible.add(skill);
            }
        }
        return eligible;
    }

    /**
     * 获取技能不可用的原因（用于调试和日志）。
     *
     * @param skill 技能对象
     * @return 不可用原因的描述，如果可用则返回 null
     */
    public String getIneligibilityReason(Skill skill) {
        if (skill.isAlways()) {
            return null;
        }

        if (!isOsCompatible(skill.getOs())) {
            String currentOs = System.getProperty("os.name");
            return "操作系统不兼容（当前：" + currentOs + "，要求：" + skill.getOs() + "）";
        }

        Skill.RequiresMetadata requires = skill.getRequires();

        for (String bin : requires.getBins()) {
            if (!hasBinary(bin)) {
                return "缺少必需的可执行文件：" + bin;
            }
        }

        if (!requires.getAnyBins().isEmpty()) {
            boolean anyFound = false;
            List<String> missingBins = new ArrayList<>();
            for (String bin : requires.getAnyBins()) {
                if (hasBinary(bin)) {
                    anyFound = true;
                    break;
                }
                missingBins.add(bin);
            }
            if (!anyFound) {
                return "缺少所有可选的可执行文件（至少需要一个）：" + String.join(", ", missingBins);
            }
        }

        if (!hasRequiredEnv(requires.getEnv())) {
            return "缺少必需的环境变量：" + requires.getEnv();
        }

        if (!hasRequiredConfig(requires.getConfig())) {
            return "缺少必需的配置项：" + requires.getConfig();
        }

        return null; // 技能满足所有依赖
    }
}
