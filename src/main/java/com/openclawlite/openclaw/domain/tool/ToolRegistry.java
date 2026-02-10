package com.openclawlite.openclaw.domain.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具注册表
 *
 * <p>管理所有可用的工具处理器，包括内置工具和插件工具。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>收集和注册内置工具（通过 Spring 依赖注入）</li>
 *   <li>加载和注册插件工具（通过 ToolPluginLoader）</li>
 *   <li>按名称查找工具</li>
 *   <li>列出所有可用工具</li>
 * </ul>
 *
 * <h3>工具名称规则：</h3>
 * <ul>
 *   <li>工具名称不区分大小写</li>
 *   <li>自动去除前后空格</li>
 *   <li>后注册的工具会覆盖先注册的同名工具</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 查找工具
 * ToolHandler handler = registry.find("read_file");
 *
 * // 列出所有工具
 * List&lt;ToolHandler&gt; tools = registry.list();
 *
 * // 执行工具
 * ToolResult result = handler.executeTyped(call, context);
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 * @see ToolHandler
 * @see ToolPluginLoader
 */
@Component
public class ToolRegistry {

  private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

  /** 工具处理器映射表（工具名称 -> 处理器实例） */
  private final Map<String, ToolHandler> handlers = new LinkedHashMap<>();

  /**
   * 构造函数 - 收集并注册所有工具
   *
   * <p>通过 Spring 依赖注入收集所有内置工具，并从插件加载器加载插件工具。</p>
   *
   * @param builtinHandlers Spring 容器中的所有内置工具
   * @param pluginLoader 插件工具加载器
   */
  public ToolRegistry(List<ToolHandler> builtinHandlers, ToolPluginLoader pluginLoader) {
    log.info("初始化工具注册表");

    // 注册内置工具
    registerAll(builtinHandlers);
    log.info("已注册 {} 个内置工具", builtinHandlers != null ? builtinHandlers.size() : 0);

    // 加载并注册插件工具
    List<ToolHandler> pluginTools = pluginLoader.load();
    registerAll(pluginTools);
    log.info("已注册 {} 个插件工具", pluginTools.size());

    log.info("工具注册表初始化完成: 总计 {} 个工具", handlers.size());
  }

  /**
   * 获取所有已注册的工具
   *
   * <p>返回按注册顺序排列的工具列表。</p>
   *
   * @return 工具处理器列表
   */
  public List<ToolHandler> list() {
    return new ArrayList<>(handlers.values());
  }

  /**
   * 根据名称查找工具处理器
   *
   * <p>查找时不区分大小写，并自动去除名称前后的空格。</p>
   *
   * @param name 工具名称
   * @return 找到的工具处理器，如果不存在则返回 null
   */
  public ToolHandler find(String name) {
    if (name == null) {
      log.debug("尝试查找工具，但名称为 null");
      return null;
    }

    String key = name.trim().toLowerCase();
    ToolHandler handler = handlers.get(key);

    if (handler == null) {
      log.debug("未找到工具: {}", name);
    } else {
      log.debug("找到工具: {} -> {}", name, handler.getClass().getSimpleName());
    }

    return handler;
  }

  /**
   * 批量注册工具
   *
   * <p>将工具列表中的所有工具注册到注册表中。</p>
   * <p>如果工具名称为 null 或已存在，则跳过该工具。</p>
   *
   * @param list 工具处理器列表
   */
  private void registerAll(List<ToolHandler> list) {
    if (list == null) {
      log.debug("工具列表为 null，跳过注册");
      return;
    }

    int registered = 0;
    int skipped = 0;

    for (ToolHandler handler : list) {
      if (handler == null || handler.name() == null) {
        skipped++;
        continue;
      }

      String key = handler.name().trim().toLowerCase();

      if (!handlers.containsKey(key)) {
        handlers.put(key, handler);
        registered++;
        log.debug("注册工具: {} ({})", key, handler.getClass().getSimpleName());
      } else {
        skipped++;
        log.debug("工具已存在，跳过: {}", key);
      }
    }

    if (registered > 0) {
      log.info("批量注册工具完成: 新增={}, 跳过={}", registered, skipped);
    }
  }
}
