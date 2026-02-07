package com.openclawlite.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
// 解析模型输出中的 JSON，识别工具调用或最终回答。
public class ToolParser {
  private final ObjectMapper objectMapper;

  // 注入 JSON 解析器。
  public ToolParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  // 尝试从模型原始输出中提取工具调用或最终文本。
  public ToolParseResult parse(String raw) {
    if (raw == null || raw.isBlank()) {
      return ToolParseResult.finalText("");
    }
    String json = extractJson(raw);
    if (json == null) {
      // 无 JSON 时直接当作最终回答。
      return ToolParseResult.finalText(raw.trim());
    }
    try {
      JsonNode node = objectMapper.readTree(json);
        if (node.has("tool")) {
          String tool = text(node, "tool");
          String path = text(node, "path");
          String content = text(node, "content");
          if (tool != null && !tool.isBlank()) {
            java.util.Map<String, Object> args =
                objectMapper.convertValue(node, new TypeReference<java.util.Map<String, Object>>() {});
            if (args != null) {
              args.remove("tool");
            }
            // 识别到工具调用结构。
            return ToolParseResult.tool(new ToolCall(tool.trim(), path, content, args));
          }
        }
      if (node.has("final")) {
        return ToolParseResult.finalText(text(node, "final"));
      }
      // JSON 不匹配预期结构时回退为原始文本。
      return ToolParseResult.finalText(raw.trim());
    } catch (Exception e) {
      // JSON 解析失败时回退为原始文本。
      return ToolParseResult.finalText(raw.trim());
    }
  }

  // 读取 JSON 节点中的字符串字段。
  private String text(JsonNode node, String field) {
    JsonNode value = node.get(field);
    if (value == null || value.isNull()) {
      return null;
    }
    return value.asText();
  }

  // 从文本中抓取最外层 JSON 片段。
  private String extractJson(String raw) {
    int start = raw.indexOf('{');
    int end = raw.lastIndexOf('}');
    if (start == -1 || end == -1 || end <= start) {
      return null;
    }
    return raw.substring(start, end + 1).trim();
  }
}
