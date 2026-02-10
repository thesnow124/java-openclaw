package com.openclawlite.openclaw.application.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Agent 工作空间管理器
 * <p>
 * 负责 Agent 工作空间目录的管理，包括创建、查询和验证。
 * 每个 Agent 都有独立的工作空间用于存储文件和配置。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>获取 Agent 工作空间路径</li>
 *   <li>创建新的工作空间目录</li>
 *   <li>检查工作空间是否存在</li>
 * </ul>
 *
 * <p>注意：</p>
 * 该类将在第 4 阶段完整实现工作空间管理功能。
 *
 * TODO: 完整实现将在第 4 阶段完成
 */
@Component
public class AgentWorkspace {

    private static final Logger log = LoggerFactory.getLogger(AgentWorkspace.class);

    /** Agent 工作空间基础目录 */
    private static final String AGENTS_BASE_DIR = "data/agents";

    /**
     * 获取 Agent 工作空间目录路径
     * <p>
     * 返回指定 Agent 的工作空间目录，格式为：{baseDir}/{agentId}
     * </p>
     *
     * @param agentId Agent ID
     * @return 工作空间目录的 Path 对象
     */
    public Path getWorkspace(String agentId) {
        return Paths.get(AGENTS_BASE_DIR, agentId);
    }

    /**
     * 为 Agent 创建新的工作空间
     * <p>
     * 创建工作空间目录（包括所有必要的父目录）。
     * 如果提供了自定义工作空间路径，则使用自定义路径；
     * 否则使用默认路径 {baseDir}/{agentId}。
     * </p>
     *
     * @param agentId Agent ID
     * @param customWorkspace 自定义工作空间路径（可选）
     * @return 创建的工作空间目录的 Path 对象
     * @throws RuntimeException 如果创建失败
     */
    public Path createWorkspace(String agentId, String customWorkspace) {
        log.debug("创建工作空间: agentId={}, customWorkspace={}", agentId, customWorkspace);

        // 如果提供了自定义工作空间路径，使用自定义路径
        if (customWorkspace != null && !customWorkspace.trim().isEmpty()) {
            Path customPath = Paths.get(customWorkspace);
            log.info("使用自定义工作空间: agentId={}, path={}", agentId, customPath);
            return customPath;
        }

        // 使用默认工作空间路径
        Path workspaceDir = getWorkspace(agentId);
        try {
            // 创建目录（包括所有必要的父目录）
            java.nio.file.Files.createDirectories(workspaceDir);
            log.info("成功创建工作空间目录: agentId={}, path={}", agentId, workspaceDir);
            return workspaceDir;
        } catch (Exception e) {
            log.error("创建工作空间目录失败: agentId={}, path={}", agentId, workspaceDir, e);
            throw new RuntimeException("Failed to create workspace", e);
        }
    }

    /**
     * 检查工作空间是否存在
     * <p>
     * 验证指定 Agent 的工作空间目录是否已创建。
     * </p>
     *
     * @param agentId Agent ID
     * @return 如果工作空间存在返回 true，否则返回 false
     */
    public boolean workspaceExists(String agentId) {
        Path workspaceDir = getWorkspace(agentId);
        boolean exists = java.nio.file.Files.exists(workspaceDir);
        log.debug("检查工作空间是否存在: agentId={}, exists={}", agentId, exists);
        return exists;
    }
}
