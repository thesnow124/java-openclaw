package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.openclaw.domain.agent.ToolCall;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
// 写入工作区内的文本文件内容。
public class WriteFileTool implements ToolHandler {
  @Override
  public String name() {
    return "write_file";
  }

  @Override
  public String description() {
    return "写入（覆盖）工作区内的文本文件";
  }

  @Override
  public String usage() {
    return "{\"tool\":\"write_file\",\"path\":\"relative/path\",\"content\":\"...\"}";
  }

  @Override
  public String execute(ToolCall call, ToolContext context) {
    try {
      Path path = context.resolveSafePath(call.getPath());
      Files.createDirectories(path.getParent());
      String safeContent = call.getContent() == null ? "" : call.getContent();
      Files.writeString(path, safeContent, StandardCharsets.UTF_8);
      return "write_file: ok";
    } catch (Exception e) {
      return "write_file: 错误 " + e.getMessage();
    }
  }
}
