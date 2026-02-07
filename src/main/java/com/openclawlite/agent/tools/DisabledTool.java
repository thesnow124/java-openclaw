package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;

// 禁用或不支持的工具占位实现。
public class DisabledTool implements ToolHandler {
  private final ToolPluginDefinition definition;
  private final String reason;

  public DisabledTool(ToolPluginDefinition definition, String reason) {
    this.definition = definition;
    this.reason = reason;
  }

  @Override
  public String name() {
    return definition.getName();
  }

  @Override
  public String description() {
    return definition.getDescription() == null ? "插件工具（不可用）" : definition.getDescription();
  }

  @Override
  public String usage() {
    if (definition.getUsage() != null && !definition.getUsage().isBlank()) {
      return definition.getUsage();
    }
    return "{\"tool\":\"" + name() + "\"}";
  }

  @Override
  public String execute(ToolCall call, ToolContext context) {
    return "tool_error: " + reason;
  }
}
