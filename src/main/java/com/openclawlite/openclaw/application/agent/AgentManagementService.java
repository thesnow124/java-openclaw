package com.openclawlite.openclaw.application.agent;

import com.openclawlite.adapter.protocol.dto.AgentFileEntry;
import com.openclawlite.adapter.protocol.dto.AgentListItem;
import com.openclawlite.openclaw.infrastructure.persistence.agent.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Agent 管理服务
 * <p>
 * 高层次服务，结合身份管理和数据库持久化功能。
 * 负责 Agent 的完整生命周期管理，包括创建、更新、删除和查询操作。
 * </p>
 *
 * <p>主要职责：</p>
 * <ul>
 *   <li>Agent 配置的数据库持久化</li>
 *   <li>Agent 身份信息管理（IDENTITY.md）</li>
 *   <li>Agent 工作空间文件管理</li>
 *   <li>Agent 列表和详情查询</li>
 * </ul>
 */
@Service
public class AgentManagementService {

    private static final Logger log = LoggerFactory.getLogger(AgentManagementService.class);

    /** Agent 配置数据库仓库 */
    private final AgentRepository repository;

    /** Agent 工作空间管理器 */
    private final AgentWorkspace workspace;

    /** Agent 文件管理器 */
    private final AgentFilesManager filesManager;

    /**
     * 构造函数
     *
     * @param repository Agent 配置数据库仓库
     * @param workspace Agent 工作空间管理器
     * @param filesManager Agent 文件管理器
     */
    public AgentManagementService(
            AgentRepository repository,
            AgentWorkspace workspace,
            AgentFilesManager filesManager) {
        this.repository = repository;
        this.workspace = workspace;
        this.filesManager = filesManager;
    }

    /**
     * 列出所有 Agent
     * <p>
     * 从数据库获取所有 Agent 配置，并转换为列表项格式返回。
     * </p>
     *
     * @return Agent 列表项列表，按创建时间倒序排列
     */
    public List<AgentListItem> listAgents() {
        log.debug("开始查询所有 Agent 列表");
        List<AgentListItem> agents = repository.findAll().stream()
            .map(this::toListItem)
            .collect(Collectors.toList());
        log.debug("查询到 {} 个 Agent", agents.size());
        return agents;
    }

    /**
     * 根据 ID 获取 Agent 配置
     * <p>
     * 从数据库查询指定 ID 的 Agent 完整配置信息。
     * </p>
     *
     * @param agentId Agent ID
     * @return Agent 配置的 Optional 对象，如果不存在则返回 Optional.empty()
     */
    public Optional<AgentConfig> getAgent(String agentId) {
        log.debug("查询 Agent 配置: agentId={}", agentId);
        return repository.findById(agentId);
    }

    /**
     * 获取 Agent 身份信息
     * <p>
     * 从 Agent 工作空间的 IDENTITY.md 文件读取身份信息。
     * 如果文件不存在或读取失败，返回默认身份。
     * </p>
     *
     * @param agentId Agent ID
     * @return Agent 身份信息对象
     */
    public AgentIdentity getIdentity(String agentId) {
        log.debug("获取 Agent 身份信息: agentId={}", agentId);

        // 获取工作空间目录中的身份文件
        Path workspaceDir = workspace.getWorkspace(agentId);
        Path identityFile = workspaceDir.resolve("IDENTITY.md");

        // 如果身份文件不存在，返回默认身份
        if (!Files.exists(identityFile)) {
            log.debug("身份文件不存在，使用默认身份: agentId={}", agentId);
            return new AgentIdentity("Agent", "", "", "light");
        }

        try {
            // 读取并解析身份文件
            String content = Files.readString(identityFile);
            AgentIdentity identity = parseIdentity(content);
            log.debug("成功读取身份信息: agentId={}, name={}", agentId, identity.name());
            return identity;
        } catch (Exception e) {
            log.warn("读取 Agent 身份文件失败，使用默认身份: agentId={}", agentId, e);
            return new AgentIdentity("Agent", "", "", "light");
        }
    }

    /**
     * 创建新的 Agent
     * <p>
     * 执行以下步骤：
     * <ol>
     *   <li>从名称生成标准化的 Agent ID</li>
     *   <li>检查 Agent 是否已存在</li>
     *   <li>创建工作空间目录</li>
     *   <li>创建身份文件（IDENTITY.md）</li>
     *   <li>保存数据库记录</li>
     * </ol>
     * </p>
     *
     * @param name Agent 名称
     * @param emoji Agent 表情符号
     * @param avatar Agent 头像 URL
     * @param model 使用的模型名称
     * @param workspacePath 自定义工作空间路径（可选）
     * @return 创建的 Agent 配置对象
     * @throws IllegalArgumentException 如果 Agent 已存在
     * @throws RuntimeException 如果数据库保存失败
     */
    @Transactional
    public AgentConfig createAgent(String name, String emoji, String avatar, String model, String workspacePath) {
        log.info("开始创建 Agent: name={}, model={}", name, model);

        // 从名称生成标准化的 Agent ID
        String agentId = normalizeAgentId(name);
        log.debug("生成 Agent ID: {}", agentId);

        // 检查 Agent 是否已存在
        if (repository.exists(agentId)) {
            log.error("Agent 已存在: agentId={}", agentId);
            throw new IllegalArgumentException("Agent already exists: " + agentId);
        }

        // 创建工作空间目录
        Path workspaceDir = workspace.createWorkspace(agentId, workspacePath);
        log.debug("工作空间目录已创建: {}", workspaceDir);

        // 创建身份文件
        createIdentity(agentId, name, emoji, avatar);

        // 创建数据库记录
        AgentConfig config = AgentConfig.create(agentId, name, workspaceDir.toString(), model, avatar);
        if (!repository.save(config)) {
            log.error("数据库保存失败: agentId={}", agentId);
            throw new RuntimeException("Failed to save agent to database");
        }

        log.info("成功创建 Agent: agentId={}, name={}", agentId, name);
        return config;
    }

    /**
     * 更新现有 Agent
     * <p>
     * 更新 Agent 的名称、表情符号、头像和模型配置。
     * 只更新提供的非空字段，保留其他字段的原值。
     * </p>
     *
     * @param agentId Agent ID
     * @param name 新名称（可选）
     * @param emoji 新表情符号（可选）
     * @param avatar 新头像 URL（可选）
     * @param model 新模型名称（可选）
     * @return 更新后的 Agent 配置对象
     * @throws IllegalArgumentException 如果 Agent 不存在
     * @throws RuntimeException 如果数据库更新失败
     */
    @Transactional
    public AgentConfig updateAgent(String agentId, String name, String emoji, String avatar, String model) {
        log.info("开始更新 Agent: agentId={}, name={}, model={}", agentId, name, model);

        // 获取现有配置
        AgentConfig existing = repository.findById(agentId)
            .orElseThrow(() -> {
                log.error("Agent 不存在: agentId={}", agentId);
                return new IllegalArgumentException("Agent not found: " + agentId);
            });

        // 更新身份文件
        AgentIdentity identity = getIdentity(agentId);
        AgentIdentity updatedIdentity = new AgentIdentity(
            name != null ? name : identity.name(),
            emoji != null ? emoji : identity.emoji(),
            avatar != null ? avatar : identity.avatar(),
            identity.theme()
        );
        updateIdentity(agentId, updatedIdentity);
        log.debug("身份文件已更新: agentId={}", agentId);

        // 更新数据库记录
        AgentConfig updatedConfig = new AgentConfig(
            agentId,
            name != null ? name : existing.name(),
            existing.workspace(),
            model != null ? model : existing.model(),
            avatar != null ? avatar : existing.avatar(),
            existing.createdAt(),
            System.currentTimeMillis()
        );

        if (!repository.update(updatedConfig)) {
            log.error("数据库更新失败: agentId={}", agentId);
            throw new RuntimeException("Failed to update agent in database");
        }

        log.info("成功更新 Agent: agentId={}", agentId);
        return updatedConfig;
    }

    /**
     * 删除 Agent
     * <p>
     * 从数据库删除 Agent 记录。
     * 注意：工作空间文件会被保留以供备份，不会被自动删除。
     * </p>
     *
     * @param agentId Agent ID
     * @return 如果删除成功返回 true，否则返回 false
     */
    @Transactional
    public boolean deleteAgent(String agentId) {
        log.info("开始删除 Agent: agentId={}", agentId);

        // 先获取 Agent 信息
        Optional<AgentConfig> config = repository.findById(agentId);

        // 从数据库删除
        boolean deleted = repository.delete(agentId);

        if (deleted && config.isPresent()) {
            // 处理工作空间文件（可选 - 可保留用于备份）
            try {
                Path workspaceDir = Path.of(config.get().workspace());
                if (java.nio.file.Files.exists(workspaceDir)) {
                    // TODO: 实现递归目录删除
                    log.info("工作空间文件已保留: {}", workspaceDir);
                }
            } catch (Exception e) {
                log.warn("清理 Agent 工作空间失败: agentId={}", agentId, e);
            }

            log.info("成功删除 Agent: agentId={}", agentId);
        } else {
            log.warn("Agent 删除失败或不存在: agentId={}", agentId);
        }

        return deleted;
    }

    /**
     * 列出 Agent 工作空间中的文件
     *
     * @param agentId Agent ID
     * @return 文件条目列表
     */
    public List<AgentFileEntry> listFiles(String agentId) {
        log.debug("列出 Agent 文件: agentId={}", agentId);
        Path workspaceDir = workspace.getWorkspace(agentId);
        return filesManager.listFiles(agentId, workspaceDir);
    }

    /**
     * 获取 Agent 工作空间中的文件内容
     *
     * @param agentId Agent ID
     * @param filename 文件名
     * @return 文件条目对象
     */
    public AgentFileEntry getFile(String agentId, String filename) {
        log.debug("读取 Agent 文件: agentId={}, filename={}", agentId, filename);
        Path workspaceDir = workspace.getWorkspace(agentId);
        return filesManager.readFile(agentId, workspaceDir, filename);
    }

    /**
     * 设置 Agent 工作空间中的文件内容
     * <p>
     * 如果文件不存在则创建，如果存在则覆盖。
     * </p>
     *
     * @param agentId Agent ID
     * @param filename 文件名
     * @param content 文件内容
     * @return 文件条目对象
     */
    public AgentFileEntry setFile(String agentId, String filename, String content) {
        log.debug("写入 Agent 文件: agentId={}, filename={}", agentId, filename);
        Path workspaceDir = workspace.getWorkspace(agentId);
        return filesManager.writeFile(agentId, workspaceDir, filename, content);
    }

    /**
     * 获取 Agent 总数
     *
     * @return Agent 总数
     */
    public int getAgentCount() {
        int count = (int) repository.count();
        log.debug("Agent 总数: {}", count);
        return count;
    }

    // ============ 辅助方法 ============

    /**
     * 创建 Agent 身份文件
     * <p>
     * 在 Agent 工作空间创建 IDENTITY.md 文件，记录 Agent 的身份信息。
     * </p>
     *
     * @param agentId Agent ID
     * @param name Agent 名称
     * @param emoji Agent 表情符号
     * @param avatar Agent 头像 URL
     */
    private void createIdentity(String agentId, String name, String emoji, String avatar) {
        Path workspaceDir = workspace.getWorkspace(agentId);
        Path identityFile = workspaceDir.resolve("IDENTITY.md");

        // 格式化身份文件内容
        String content = String.format("""
            # Agent Identity

            - Name: %s
            - Emoji: %s
            - Avatar: %s
            - Theme: light
            """, name, emoji != null ? emoji : "", avatar != null ? avatar : "");

        try {
            Files.writeString(identityFile, content);
            log.debug("成功创建身份文件: agentId={}", agentId);
        } catch (Exception e) {
            log.error("创建身份文件失败: agentId={}", agentId, e);
        }
    }

    /**
     * 更新 Agent 身份文件
     * <p>
     * 更新 Agent 工作空间中的 IDENTITY.md 文件。
     * </p>
     *
     * @param agentId Agent ID
     * @param identity 新的身份信息
     */
    private void updateIdentity(String agentId, AgentIdentity identity) {
        Path workspaceDir = workspace.getWorkspace(agentId);
        Path identityFile = workspaceDir.resolve("IDENTITY.md");

        // 格式化身份文件内容
        String content = String.format("""
            # Agent Identity

            - Name: %s
            - Emoji: %s
            - Avatar: %s
            - Theme: %s
            """, identity.name(), identity.emoji(), identity.avatar(), identity.theme());

        try {
            Files.writeString(identityFile, content);
            log.debug("成功更新身份文件: agentId={}", agentId);
        } catch (Exception e) {
            log.error("更新身份文件失败: agentId={}", agentId, e);
        }
    }

    /**
     * 解析身份文件内容
     * <p>
     * 从 IDENTITY.md 文件内容中解析出 Agent 身份信息。
     * </p>
     *
     * @param content 身份文件内容
     * @return 解析后的身份信息对象
     */
    private AgentIdentity parseIdentity(String content) {
        String name = "Agent";
        String emoji = "";
        String avatar = "";
        String theme = "light";

        // 逐行解析身份信息
        for (String line : content.split("\n")) {
            if (line.startsWith("- Name:")) {
                name = line.substring(7).trim();
            } else if (line.startsWith("- Emoji:")) {
                emoji = line.substring(8).trim();
            } else if (line.startsWith("- Avatar:")) {
                avatar = line.substring(9).trim();
            } else if (line.startsWith("- Theme:")) {
                theme = line.substring(8).trim();
            }
        }

        return new AgentIdentity(name, emoji, avatar, theme);
    }

    /**
     * 标准化 Agent ID
     * <p>
     * 将输入的名称转换为符合规范的 Agent ID：
     * <ul>
     *   <li>转换为小写</li>
     *   <li>只保留字母、数字、下划线和连字符</li>
     *   <li>移除多余的分隔符</li>
     * </ul>
     * </p>
     *
     * @param input 输入字符串
     * @return 标准化后的 Agent ID
     */
    private String normalizeAgentId(String input) {
        if (input == null) {
            return "unknown";
        }
        return input.toLowerCase()
            .replaceAll("[^a-z0-9_-]", "-")  // 替换非法字符为连字符
            .replaceAll("-+", "-")           // 合并多个连字符
            .replaceAll("^-|-$", "");         // 移除首尾连字符
    }

    /**
     * 将 Agent 配置转换为列表项
     * <p>
     * 结合数据库配置和身份文件信息，生成用于列表显示的数据。
     * </p>
     *
     * @param config Agent 配置对象
     * @return Agent 列表项对象
     */
    private AgentListItem toListItem(AgentConfig config) {
        AgentIdentity identity = getIdentity(config.agentId());
        return new AgentListItem(
            config.agentId(),
            config.name(),
            identity.emoji(),
            config.avatar() != null ? config.avatar() : identity.avatar(),
            config.model(),
            Instant.ofEpochMilli(config.createdAt()),
            Instant.ofEpochMilli(config.updatedAt())
        );
    }
}
