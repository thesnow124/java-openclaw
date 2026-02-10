package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.openclaw.domain.agent.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 扩展的工具执行上下文
 * 支持全局文件系统访问（需要显式启用）
 */
public class GlobalToolContext extends ToolContext {

    private static final Logger log = LoggerFactory.getLogger(GlobalToolContext.class);
    private final boolean allowGlobalAccess;

    public GlobalToolContext(Path workspace, boolean allowGlobalAccess) {
        super(workspace);
        this.allowGlobalAccess = allowGlobalAccess;
    }

    public GlobalToolContext(Path workspace, SessionState session, boolean allowGlobalAccess) {
        super(workspace, session);
        this.allowGlobalAccess = allowGlobalAccess;
    }

    /**
     * 解析路径 - 支持全局访问模式
     *
     * @param rawPath 输入路径
     * @return 解析后的绝对路径
     * @throws IOException 如果路径不安全或全局访问未启用
     */
    @Override
    public Path resolveSafePath(String rawPath) throws IOException {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IOException("path is required");
        }

        Path input = Path.of(rawPath);
        Path resolved = input.isAbsolute() ? input : getWorkspace().resolve(input);
        Path normalized = resolved.normalize();

        // 全局访问模式：允许访问任意路径
        if (allowGlobalAccess) {
            log.debug("Global access enabled: resolving path {}", normalized);
            // 安全警告
            if (normalized.startsWith("/System") || normalized.startsWith("/bin") || normalized.startsWith("/sbin")) {
                log.warn("Attempting to access system directory: {}", normalized);
            }
            return normalized;
        }

        // 安全模式：仅允许访问工作区内
        if (!normalized.startsWith(getWorkspace())) {
            throw new IOException("path must stay inside workspace (global access not enabled)");
        }

        return normalized;
    }

    /**
     * 检查是否启用了全局访问
     */
    public boolean isGlobalAccessEnabled() {
        return allowGlobalAccess;
    }
}
