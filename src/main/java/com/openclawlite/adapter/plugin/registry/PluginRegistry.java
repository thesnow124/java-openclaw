package com.openclawlite.adapter.plugin.registry;

import com.openclawlite.adapter.plugin.core.Plugin;
import com.openclawlite.adapter.plugin.core.PluginMetadata;
import com.openclawlite.common.enums.PluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件注册表
 *
 * <p>管理所有已注册插件的注册、查询和状态更新。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>插件注册与注销</li>
 *   <li>插件查询（按ID、类型等）</li>
 *   <li>插件状态管理</li>
 *   <li>依赖关系检查</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 注册插件
 * Plugin plugin = new MyPlugin();
 * registry.register(plugin, "file:/path/to/plugin.jar");
 *
 * // 查询插件
 * Optional&lt;Plugin&gt; plugin = registry.getPlugin("my-plugin");
 *
 * // 按类型查询
 * List&lt;RegisteredPlugin&gt; channels = registry.getPluginsByType(PluginType.CHANNEL);
 *
 * // 检查依赖
 * DependencyCheckResult result = registry.checkDependencies("my-plugin");
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 * @see Plugin
 * @see PluginMetadata
 * @see RegisteredPlugin
 */
@Component
public class PluginRegistry {

    private static final Logger log = LoggerFactory.getLogger(PluginRegistry.class);

    /** 已注册插件的映射表（插件ID -> 注册信息） */
    private final Map<String, RegisteredPlugin> plugins = new ConcurrentHashMap<>();

    /**
     * 注册插件
     *
     * <p>将插件添加到注册表中，记录其元数据和来源。</p>
     *
     * @param plugin 要注册的插件实例
     * @param source 插件来源（如JAR文件路径）
     * @throws IllegalArgumentException 如果插件ID已存在
     */
    public void register(Plugin plugin, String source) {
        PluginMetadata metadata = plugin.getMetadata();

        // 检查是否已注册
        if (plugins.containsKey(metadata.id())) {
            log.warn("插件已存在，将被覆盖: pluginId={}", metadata.id());
        }

        // 创建注册信息
        RegisteredPlugin registered = new RegisteredPlugin(
            metadata.id(),
            metadata,
            plugin,
            source,
            PluginState.LOADED,
            System.currentTimeMillis()
        );

        plugins.put(metadata.id(), registered);

        log.info("注册插件成功: name={}, id={}, version={}, type={}, source={}",
            metadata.name(),
            metadata.id(),
            metadata.version(),
            metadata.type(),
            source);

        log.debug("插件描述: {}", metadata.description());
        if (!metadata.dependencies().isEmpty()) {
            log.debug("插件依赖: {}", metadata.dependencies());
        }
    }

    /**
     * 注销插件
     *
     * <p>从注册表中移除指定的插件。</p>
     *
     * @param pluginId 要注销的插件ID
     */
    public void unregister(String pluginId) {
        RegisteredPlugin removed = plugins.remove(pluginId);
        if (removed != null) {
            log.info("注销插件成功: pluginId={}, name={}",
                pluginId, removed.metadata().name());
        } else {
            log.warn("尝试注销不存在的插件: pluginId={}", pluginId);
        }
    }

    /**
     * 根据ID获取插件实例
     *
     * @param pluginId 插件ID
     * @return 插件实例的Optional包装
     */
    public Optional<Plugin> getPlugin(String pluginId) {
        RegisteredPlugin registered = plugins.get(pluginId);
        return Optional.ofNullable(registered != null ? registered.plugin() : null);
    }

    /**
     * 根据ID获取插件元数据
     *
     * @param pluginId 插件ID
     * @return 插件元数据的Optional包装
     */
    public Optional<PluginMetadata> getMetadata(String pluginId) {
        RegisteredPlugin registered = plugins.get(pluginId);
        return Optional.ofNullable(registered != null ? registered.metadata() : null);
    }

    /**
     * 获取所有已注册的插件
     *
     * @return 不可修改的插件集合
     */
    public Collection<RegisteredPlugin> getAllPlugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    /**
     * 根据类型获取插件列表
     *
     * @param type 插件类型（CHANNEL、TOOL等）
     * @return 指定类型的插件列表
     */
    public List<RegisteredPlugin> getPluginsByType(com.openclawlite.common.enums.PluginType type) {
        return plugins.values().stream()
            .filter(p -> p.metadata().type() == type)
            .toList();
    }

    /**
     * 更新插件状态
     *
     * <p>更新指定插件的运行状态。</p>
     *
     * @param pluginId 插件ID
     * @param state 新的插件状态
     */
    public void updateState(String pluginId, PluginState state) {
        RegisteredPlugin registered = plugins.get(pluginId);
        if (registered != null) {
            RegisteredPlugin updated = new RegisteredPlugin(
                registered.pluginId(),
                registered.metadata(),
                registered.plugin(),
                registered.source(),
                state,
                registered.registeredAt()
            );
            plugins.put(pluginId, updated);

            log.debug("更新插件状态: pluginId={}, state={}", pluginId, state);
        } else {
            log.warn("尝试更新不存在的插件状态: pluginId={}, state={}",
                pluginId, state);
        }
    }

    /**
     * 检查插件是否存在
     *
     * @param pluginId 插件ID
     * @return 如果插件存在返回true，否则返回false
     */
    public boolean hasPlugin(String pluginId) {
        return plugins.containsKey(pluginId);
    }

    /**
     * 获取已注册插件的数量
     *
     * @return 插件总数
     */
    public int getPluginCount() {
        return plugins.size();
    }

    /**
     * 检查插件依赖关系
     *
     * <p>验证插件的所有依赖是否都已注册。</p>
     *
     * @param pluginId 要检查的插件ID
     * @return 依赖检查结果，包含满足和缺失的依赖列表
     */
    public DependencyCheckResult checkDependencies(String pluginId) {
        RegisteredPlugin registered = plugins.get(pluginId);
        if (registered == null) {
            log.warn("检查依赖时插件不存在: pluginId={}", pluginId);
            return new DependencyCheckResult(false, List.of("Plugin not found"), List.of());
        }

        List<String> satisfied = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        // 检查每个依赖项
        for (String depId : registered.metadata().dependencies()) {
            if (plugins.containsKey(depId)) {
                satisfied.add(depId);
                log.debug("依赖已满足: plugin={}, dependency={}", pluginId, depId);
            } else {
                missing.add(depId);
                log.debug("依赖缺失: plugin={}, dependency={}", pluginId, depId);
            }
        }

        boolean allSatisfied = missing.isEmpty();

        if (allSatisfied) {
            log.info("插件依赖检查通过: pluginId={}, dependencies={}",
                pluginId, satisfied);
        } else {
            log.warn("插件依赖检查失败: pluginId={}, missing={}",
                pluginId, missing);
        }

        return new DependencyCheckResult(allSatisfied, satisfied, missing);
    }

    /**
     * 依赖检查结果记录
     *
     * @param satisfied 所有依赖是否都满足
     * @param satisfiedDependencies 已满足的依赖列表
     * @param missingDependencies 缺失的依赖列表
     */
    public record DependencyCheckResult(
        boolean satisfied,
        List<String> satisfiedDependencies,
        List<String> missingDependencies
    ) {}

    /**
     * 已注册插件记录
     *
     * <p>包含插件的完整注册信息和运行时状态。</p>
     *
     * @param pluginId 插件ID
     * @param metadata 插件元数据
     * @param plugin 插件实例
     * @param source 插件来源（如JAR文件路径）
     * @param state 插件当前状态
     * @param registeredAt 注册时间戳
     */
    public record RegisteredPlugin(
        String pluginId,
        PluginMetadata metadata,
        Plugin plugin,
        String source,
        PluginState state,
        long registeredAt
    ) {}
}
