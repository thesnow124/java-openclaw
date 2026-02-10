package com.openclawlite.openclaw.domain.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
// 读取 tools 目录下的插件定义并生成对应工具。
public class ToolPluginLoader {
  private final ObjectMapper objectMapper;
  private final AppProperties properties;

  // 注入 JSON 解析器与配置。
  public ToolPluginLoader(ObjectMapper objectMapper, AppProperties properties) {
    this.objectMapper = objectMapper;
    this.properties = properties;
  }

  // 从插件目录加载工具列表。
  public List<ToolHandler> load() {
    Path toolsDir = resolveToolsDir();
    if (toolsDir == null || !Files.exists(toolsDir)) {
      return List.of();
    }
    List<ToolHandler> handlers = new ArrayList<>();
    try {
      List<Path> files =
          Files.walk(toolsDir)
              .filter(path -> path.getFileName().toString().endsWith(".json"))
              .sorted()
              .toList();
      for (Path file : files) {
        ToolPluginDefinition def = objectMapper.readValue(file.toFile(), ToolPluginDefinition.class);
        ToolHandler handler = buildHandler(def);
        if (handler != null) {
          handlers.add(handler);
        }
      }
    } catch (IOException e) {
      return List.of();
    }
    return handlers;
  }

  private Path resolveToolsDir() {
    Path workspace = Path.of(properties.getWorkspaceDir()).toAbsolutePath().normalize();
    Path toolsDir = Path.of(properties.getToolsDir());
    if (toolsDir.isAbsolute()) {
      return toolsDir.normalize();
    }
    return workspace.resolve(toolsDir).normalize();
  }

  private ToolHandler buildHandler(ToolPluginDefinition def) {
    if (def == null || def.getName() == null || def.getName().isBlank()) {
      return null;
    }
    String type = def.getType() == null ? "" : def.getType().trim().toLowerCase();
    if ("command".equals(type)) {
      return new CommandTool(def, properties);
    }
    return new DisabledTool(def, "不支持的插件工具类型：" + type);
  }
}
