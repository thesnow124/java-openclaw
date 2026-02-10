package com.openclawlite.openclaw.application.agent;

import com.openclawlite.adapter.protocol.dto.AgentFileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent 文件管理器
 * <p>
 * 负责 Agent 工作空间中文件的读写操作。
 * 提供文件列表、读取和写入功能。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>列出工作空间中的所有文件</li>
 *   <li>读取文件内容</li>
 *   <li>写入文件内容</li>
 * </ul>
 *
 * <p>注意：</p>
 * 该类将在第 4 阶段完整实现文件管理功能。
 *
 * TODO: 完整实现将在第 4 阶段完成
 */
@Component
public class AgentFilesManager {

    private static final Logger log = LoggerFactory.getLogger(AgentFilesManager.class);

    /**
     * 列出 Agent 工作空间中的所有文件
     * <p>
     * 扫描工作空间目录，返回所有常规文件的列表。
     * </p>
     *
     * @param agentId Agent ID
     * @param workspaceDir 工作空间目录路径
     * @return 文件条目列表，如果工作空间不存在或出错则返回空列表
     */
    public List<AgentFileEntry> listFiles(String agentId, Path workspaceDir) {
        log.debug("列出 Agent 工作空间文件: agentId={}, workspaceDir={}", agentId, workspaceDir);

        try {
            List<AgentFileEntry> files = new ArrayList<>();

            // 检查工作空间是否存在
            if (!Files.exists(workspaceDir)) {
                log.warn("工作空间不存在: agentId={}, workspaceDir={}", agentId, workspaceDir);
                return files;
            }

            // 列出工作空间中的所有文件
            Files.list(workspaceDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    String name = file.getFileName().toString();
                    files.add(new AgentFileEntry(
                        name,           // 文件名
                        file.toString(), // 文件完整路径
                        false,          // 不是目录
                        null,           // 无文件大小信息
                        null,           // 无修改时间信息
                        null            // 无内容信息（列表时不读取内容）
                    ));
                });

            log.debug("找到 {} 个文件: agentId={}", files.size(), agentId);
            return files;
        } catch (Exception e) {
            log.error("列出文件失败: agentId={}, workspaceDir={}", agentId, workspaceDir, e);
            return List.of();
        }
    }

    /**
     * 从 Agent 工作空间读取文件
     * <p>
     * 读取指定文件的内容并返回。
     * </p>
     *
     * @param agentId Agent ID
     * @param workspaceDir 工作空间目录路径
     * @param filename 要读取的文件名
     * @return 包含文件内容的文件条目对象
     * @throws IllegalArgumentException 如果文件不存在
     * @throws RuntimeException 如果读取失败
     */
    public AgentFileEntry readFile(String agentId, Path workspaceDir, String filename) {
        log.debug("读取文件: agentId={}, filename={}", agentId, filename);

        try {
            // 解析文件完整路径
            Path filePath = workspaceDir.resolve(filename);

            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                log.error("文件不存在: agentId={}, filename={}", agentId, filename);
                throw new IllegalArgumentException("File not found: " + filename);
            }

            // 读取文件内容
            String content = Files.readString(filePath);
            log.debug("成功读取文件: agentId={}, filename={}, size={} bytes",
                     agentId, filename, content.length());

            return new AgentFileEntry(
                filename,          // 文件名
                filePath.toString(), // 文件完整路径
                false,             // 不是目录
                null,              // 无文件大小信息
                null,              // 无修改时间信息
                content            // 文件内容
            );
        } catch (Exception e) {
            log.error("读取文件失败: agentId={}, filename={}", agentId, filename, e);
            throw new RuntimeException("Failed to read file", e);
        }
    }

    /**
     * 向 Agent 工作空间写入文件
     * <p>
     * 将内容写入指定文件。如果文件存在则覆盖，如果不存在则创建。
     * 会自动创建必要的父目录。
     * </p>
     *
     * @param agentId Agent ID
     * @param workspaceDir 工作空间目录路径
     * @param filename 要写入的文件名
     * @param content 文件内容
     * @return 写入后的文件条目对象
     * @throws RuntimeException 如果写入失败
     */
    public AgentFileEntry writeFile(String agentId, Path workspaceDir, String filename, String content) {
        log.info("写入文件: agentId={}, filename={}, size={} bytes",
                agentId, filename, content != null ? content.length() : 0);

        try {
            // 解析文件完整路径
            Path filePath = workspaceDir.resolve(filename);

            // 如果需要，创建父目录
            Files.createDirectories(filePath.getParent());

            // 写入文件内容
            Files.writeString(filePath, content);

            log.info("成功写入文件: agentId={}, filename={}", agentId, filename);

            return new AgentFileEntry(
                filename,          // 文件名
                filePath.toString(), // 文件完整路径
                false,             // 不是目录
                null,              // 无文件大小信息
                null,              // 无修改时间信息
                content            // 文件内容
            );
        } catch (Exception e) {
            log.error("写入文件失败: agentId={}, filename={}", agentId, filename, e);
            throw new RuntimeException("Failed to write file", e);
        }
    }
}
