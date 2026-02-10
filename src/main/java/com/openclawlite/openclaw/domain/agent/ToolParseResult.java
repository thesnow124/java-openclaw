package com.openclawlite.openclaw.domain.agent;

// 表示一次模型输出解析后的结果（工具调用或最终文本）。
public class ToolParseResult {
  private final ToolCall toolCall;
  private final String finalText;

  // 内部构造器：只允许通过静态工厂方法创建。
  private ToolParseResult(ToolCall toolCall, String finalText) {
    this.toolCall = toolCall;
    this.finalText = finalText;
  }

  // 创建一个工具调用结果。
  public static ToolParseResult tool(ToolCall call) {
    return new ToolParseResult(call, null);
  }

  // 创建一个最终文本结果。
  public static ToolParseResult finalText(String text) {
    return new ToolParseResult(null, text);
  }

  // 获取解析出的工具调用。
  public ToolCall getToolCall() {
    return toolCall;
  }

  // 获取解析出的最终文本。
  public String getFinalText() {
    return finalText;
  }
}
