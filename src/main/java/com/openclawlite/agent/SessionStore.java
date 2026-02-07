package com.openclawlite.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclawlite.config.AppProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
// 负责从磁盘读取/写入会话存档。
public class SessionStore {
    private final ObjectMapper objectMapper;
    private final AppProperties properties;

    // 注入 JSON 读写器和应用配置。
    public SessionStore(ObjectMapper objectMapper, AppProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    // 读取会话存档；若不存在则创建新会话。
    public SessionState load() {
        Path path = resolvePath();
        if (Files.exists(path)) {
            try {
                return objectMapper.readValue(path.toFile(), SessionState.class);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read session store: " + path, e);
            }
        }
        // 首次运行时初始化新会话。
        SessionState state = new SessionState();
        state.setSessionId(UUID.randomUUID().toString());
        state.setUpdatedAt(System.currentTimeMillis());
        return state;
    }

    // 将会话对象序列化为 JSON 文件。
    public void save(SessionState state) {
        Path path = resolvePath();
        try {
            Files.createDirectories(path.getParent());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), state);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write session store: " + path, e);
        }
    }

    // 计算最终的会话存档路径（支持相对路径）。
    private Path resolvePath() {
        Path workspace = Path.of(properties.getWorkspaceDir()).toAbsolutePath().normalize();
        Path sessionPath = Path.of(properties.getSessionPath());
        if (sessionPath.isAbsolute()) {
            return sessionPath;
        }
        return workspace.resolve(sessionPath).normalize();
    }
}
