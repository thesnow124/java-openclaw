package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;

import java.util.Map;

// 工具处理器接口，用于注册与执行工具。
public interface ToolHandler {
  // 工具名称（用于 JSON 调用）。
  String name();

  // 工具简要描述（用于提示词）。
  String description();

  // 工具调用示例（JSON）。
  String usage();

  // 获取参数 JSON Schema（默认返回空 object）。
  default Map<String, Object> getParameterSchema() {
    return Map.of(
        "type", "object",
        "properties", Map.of()
    );
  }

  // 执行工具并返回结果文本（旧版方法，保持兼容）。
  String execute(ToolCall call, ToolContext context);

  // 执行工具并返回结构化结果（新版方法）。
  default ToolResult executeTyped(ToolCall call, ToolContext context) {
    String text = execute(call, context);
    return ToolResult.success(text, null);
  }
}
