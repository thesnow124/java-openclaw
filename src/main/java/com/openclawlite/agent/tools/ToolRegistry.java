package com.openclawlite.agent.tools;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
// 管理工具注册表，聚合内置与插件工具。
public class ToolRegistry {
  private final Map<String, ToolHandler> handlers = new LinkedHashMap<>();

  // 收集内置工具并加载插件工具。
  public ToolRegistry(List<ToolHandler> builtinHandlers, ToolPluginLoader pluginLoader) {
    registerAll(builtinHandlers);
    registerAll(pluginLoader.load());
  }

  // 获取全部工具（按注册顺序）。
  public List<ToolHandler> list() {
    return new ArrayList<>(handlers.values());
  }

  // 根据名称查找工具处理器。
  public ToolHandler find(String name) {
    if (name == null) {
      return null;
    }
    return handlers.get(name.trim().toLowerCase());
  }

  private void registerAll(List<ToolHandler> list) {
    if (list == null) {
      return;
    }
    for (ToolHandler handler : list) {
      if (handler == null || handler.name() == null) {
        continue;
      }
      String key = handler.name().trim().toLowerCase();
      if (!handlers.containsKey(key)) {
        handlers.put(key, handler);
      }
    }
  }
}
