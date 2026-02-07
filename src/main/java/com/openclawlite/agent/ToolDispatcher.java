package com.openclawlite.agent;

import com.openclawlite.agent.tools.ToolContext;
import com.openclawlite.agent.tools.ToolHandler;
import com.openclawlite.agent.tools.ToolRegistry;
import com.openclawlite.config.AppProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
// 根据工具调用分发执行逻辑（基于注册表）。
public class ToolDispatcher {
  private final ToolRegistry registry;
  private final ToolContext context;

  // 注入工具注册表与配置以限定安全工作区。
  public ToolDispatcher(AppProperties properties, ToolRegistry registry) {
    this.registry = registry;
    Path workspace = Path.of(properties.getWorkspaceDir()).toAbsolutePath().normalize();
    this.context = new ToolContext(workspace);
  }

  // 执行工具调用并返回字符串化结果。
  public String execute(ToolCall call) {
    if (call == null || call.getTool() == null) {
      return "tool_error: 缺少工具名称";
    }
    String tool = call.getTool().trim().toLowerCase();
    ToolHandler handler = registry.find(tool);
    if (handler == null) {
      return "tool_error: 未知工具 '" + tool + "'";
    }
    // 基于工具注册表执行。
    return handler.execute(call, context);
  }
}
