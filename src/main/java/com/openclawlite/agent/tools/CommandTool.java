package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;
import com.openclawlite.config.AppProperties;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// 命令型插件工具：执行预定义命令并返回输出。
public class CommandTool implements ToolHandler {
  private final ToolPluginDefinition definition;
  private final AppProperties properties;

  // 绑定插件定义与配置。
  public CommandTool(ToolPluginDefinition definition, AppProperties properties) {
    this.definition = definition;
    this.properties = properties;
  }

  @Override
  public String name() {
    return definition.getName();
  }

  @Override
  public String description() {
    return definition.getDescription() == null ? "命令型插件工具" : definition.getDescription();
  }

  @Override
  public String usage() {
    if (definition.getUsage() != null && !definition.getUsage().isBlank()) {
      return definition.getUsage();
    }
    return "{\"tool\":\"" + name() + "\",\"content\":\"可选输入\"}";
  }

  @Override
  public String execute(ToolCall call, ToolContext context) {
    if (!properties.isEnableCommandTools()) {
      return "tool_error: 未启用命令型工具（enable-command-tools=false）";
    }
    String command = definition.getCommand();
    if (command == null || command.isBlank()) {
      return "tool_error: 插件工具未配置 command";
    }
    try {
      ProcessBuilder builder = new ProcessBuilder("bash", "-lc", command);
      builder.directory(context.getWorkspace().toFile());
      builder.redirectErrorStream(true);
      Process process = builder.start();
      if (call != null && call.getContent() != null && !call.getContent().isBlank()) {
        process.getOutputStream().write(call.getContent().getBytes());
        process.getOutputStream().flush();
      }
      process.getOutputStream().close();
      StringBuilder output = new StringBuilder();
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
        }
      }
      int code = process.waitFor();
      return "command: exit=" + code + "\n" + output.toString().trim();
    } catch (Exception e) {
      return "command: error " + e.getMessage();
    }
  }
}
