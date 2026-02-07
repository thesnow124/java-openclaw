package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
// 读取工作区内的文件内容。
public class ReadFileTool implements ToolHandler {
  @Override
  public String name() {
    return "read_file";
  }

  @Override
  public String description() {
    return "读取工作区内的文本文件内容";
  }

  @Override
  public String usage() {
    return "{\"tool\":\"read_file\",\"path\":\"relative/path\"}";
  }

  @Override
  public String execute(ToolCall call, ToolContext context) {
    try {
      Path path = context.resolveSafePath(call.getPath());
      if (!Files.exists(path)) {
        return "read_file: 文件不存在";
      }
      String content = Files.readString(path, StandardCharsets.UTF_8);
      return "read_file: ok\n" + content;
    } catch (Exception e) {
      return "read_file: 错误 " + e.getMessage();
    }
  }
}
