package com.openclawlite.agent.tools;

import com.openclawlite.agent.SessionState;

import java.io.IOException;
import java.nio.file.Path;

// 工具执行上下文，提供安全路径解析能力和会话访问。
public class ToolContext {
  private final Path workspace;
  private final SessionState session;

  // 创建工具上下文，指定工作区根目录。
  public ToolContext(Path workspace) {
    this.workspace = workspace;
    this.session = null;
  }

  // 创建工具上下文，指定工作区根目录和会话。
  public ToolContext(Path workspace, SessionState session) {
    this.workspace = workspace;
    this.session = session;
  }

  // 获取工作区根目录。
  public Path getWorkspace() {
    return workspace;
  }

  // 获取当前会话。
  public SessionState getSession() {
    return session;
  }

  // 将输入路径解析为工作区内的安全绝对路径。
  public Path resolveSafePath(String rawPath) throws IOException {
    if (rawPath == null || rawPath.isBlank()) {
      throw new IOException("path is required");
    }
    Path input = Path.of(rawPath);
    Path resolved = input.isAbsolute() ? input : workspace.resolve(input);
    Path normalized = resolved.normalize();
    if (!normalized.startsWith(workspace)) {
      throw new IOException("path must stay inside workspace");
    }
    return normalized;
  }
}
