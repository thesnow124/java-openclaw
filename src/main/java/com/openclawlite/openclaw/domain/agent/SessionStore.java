package com.openclawlite.openclaw.domain.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 会话持久化存储
 *
 * <p>负责会话状态的磁盘持久化，包括：</p>
 * <ul>
 *   <li>加载历史会话（从 JSON 文件）</li>
 *   <li>保存当前会话（序列化为 JSON）</li>
 *   <li>创建新会话（首次运行时）</li>
 * </ul>
 *
 * <p>存储路径配置：</p>
 * <ul>
 *   <li>支持绝对路径和相对路径</li>
 *   <li>相对路径基于工作区目录</li>
 *   <li>自动创建必要的目录结构</li>
 * </ul>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
@Component
public class SessionStore {

    private static final Logger log = LoggerFactory.getLogger(SessionStore.class);

    /** JSON 对象映射器（用于序列化/反序列化） */
    private final ObjectMapper objectMapper;

    /** 应用配置属性 */
    private final AppProperties properties;

    /**
     * 构造会话存储
     *
     * @param objectMapper JSON 映射器
     * @param properties 应用配置
     */
    public SessionStore(ObjectMapper objectMapper, AppProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        log.debug("会话存储已初始化");
    }

    /**
     * 加载会话状态
     *
     * <p>如果会话文件存在，则从磁盘加载；否则创建新的空会话。</p>
     *
     * @return 会话状态对象
     * @throws IllegalStateException 如果读取失败
     */
    public SessionState load() {
        Path path = resolvePath();
        log.debug("加载会话，路径: {}", path);

        if (Files.exists(path)) {
            try {
                log.info("从磁盘加载会话: {}", path);
                SessionState state = objectMapper.readValue(path.toFile(), SessionState.class);
                log.debug("会话加载成功: sessionId={}, 消息数={}",
                         state.getSessionId(), state.getMessages().size());
                return state;
            } catch (IOException e) {
                log.error("读取会话存档失败: {}", path, e);
                throw new IllegalStateException("Failed to read session store: " + path, e);
            }
        }

        // 首次运行时初始化新会话
        log.info("会话文件不存在，创建新会话");
        SessionState state = new SessionState();
        state.setSessionId(UUID.randomUUID().toString());
        state.setCreatedAt(System.currentTimeMillis());
        state.setUpdatedAt(System.currentTimeMillis());
        log.debug("新会话已创建: sessionId={}", state.getSessionId());
        return state;
    }

    /**
     * 保存会话状态
     *
     * <p>将会话对象序列化为 JSON 文件，自动创建必要的目录。</p>
     *
     * @param state 会话状态对象
     * @throws IllegalStateException 如果写入失败
     */
    public void save(SessionState state) {
        Path path = resolvePath();
        log.debug("保存会话，路径: {}", path);

        try {
            // 确保父目录存在
            Files.createDirectories(path.getParent());
            log.trace("已创建会话目录: {}", path.getParent());

            // 序列化为 JSON（带格式化）
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), state);
            log.debug("会话已保存: sessionId={}, 消息数={}",
                     state.getSessionId(), state.getMessages().size());

        } catch (IOException e) {
            log.error("写入会话存档失败: {}", path, e);
            throw new IllegalStateException("Failed to write session store: " + path, e);
        }
    }

    /**
     * 解析会话存档的完整路径
     *
     * <p>支持相对路径和绝对路径：</p>
     * <ul>
     *   <li>绝对路径：直接使用</li>
     *   <li>相对路径：基于工作区目录解析</li>
     * </ul>
     *
     * @return 规范化后的绝对路径
     */
    private Path resolvePath() {
        Path workspace = Path.of(properties.getWorkspaceDir()).toAbsolutePath().normalize();
        Path sessionPath = Path.of(properties.getSessionPath());

        if (sessionPath.isAbsolute()) {
            return sessionPath.normalize();
        }

        // 相对路径基于工作区解析
        return workspace.resolve(sessionPath).normalize();
    }
}
