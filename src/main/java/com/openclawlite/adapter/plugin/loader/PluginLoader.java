package com.openclawlite.adapter.plugin.loader;

import com.openclawlite.adapter.plugin.core.Plugin;
import com.openclawlite.common.exception.PluginException;
import com.openclawlite.adapter.plugin.core.PluginMetadata;
import com.openclawlite.common.enums.PluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * 插件加载器
 *
 * <p>基于 Java ServiceLoader 机制，从指定目录发现和加载插件 JAR 文件。</p>
 *
 * <h3>插件加载机制：</h3>
 * <ol>
 *   <li>扫描插件目录下的所有 .jar 文件</li>
 *   <li>为每个 JAR 创建独立的类加载器</li>
 *   <li>通过 ServiceLoader 发现 Plugin 接口的实现类</li>
 *   <li>实例化插件并记录加载信息</li>
 * </ol>
 *
 * <h3>类加载隔离：</h3>
 * <p>使用自定义的 PluginClassLoader 实现插件间的类隔离，共享 API 包：</p>
 * <ul>
 *   <li>com.openclawlite.adapter.plugin.core</li>
 *   <li>com.openclawlite.adapter.channel.core</li>
 *   <li>com.openclawlite.adapter.protocol.dto</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 从目录加载所有插件
 * List&lt;Plugin&gt; plugins = loader.loadPluginsFromDirectory(Path.of("/path/to/plugins"));
 *
 * // 加载单个插件
 * Plugin plugin = loader.loadPlugin(Path.of("/path/to/plugin.jar"));
 *
 * // 卸载插件
 * loader.unloadPlugin("my-plugin");
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 * @see Plugin
 * @see PluginClassLoader
 * @see LoadedPlugin
 */
@Component
public class PluginLoader {

    private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

    /**
     * 插件间共享的包（API接口）
     *
     * <p>这些包的类由父类加载器加载，确保所有插件使用相同的API版本。</p>
     */
    private static final List<String> SHARED_PACKAGES = List.of(
        "com.openclawlite.adapter.plugin.core",
        "com.openclawlite.adapter.channel.core",
        "com.openclawlite.adapter.protocol.dto"
    );

    /** 已加载插件的映射表（插件ID -> 加载信息） */
    private final Map<String, LoadedPlugin> loadedPlugins = new HashMap<>();

    /**
     * 从目录加载所有插件
     *
     * <p>扫描指定目录下的所有 JAR 文件，尝试加载其中的插件。</p>
     *
     * @param pluginDir 包含插件 JAR 的目录
     * @return 成功加载的插件列表
     */
    public List<Plugin> loadPluginsFromDirectory(Path pluginDir) {
        log.info("开始从目录加载插件: {}", pluginDir);

        List<Plugin> plugins = new ArrayList<>();

        // 检查目录是否存在
        if (!pluginDir.toFile().exists()) {
            log.warn("插件目录不存在: {}", pluginDir);
            return plugins;
        }

        // 列出所有 JAR 文件
        File[] jarFiles = pluginDir.toFile().listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            log.info("插件目录中没有找到 JAR 文件: {}", pluginDir);
            return plugins;
        }

        log.info("发现 {} 个 JAR 文件", jarFiles.length);

        // 加载每个 JAR 文件
        for (File jarFile : jarFiles) {
            try {
                log.debug("尝试加载插件 JAR: {}", jarFile.getName());
                Plugin plugin = loadPlugin(jarFile.toPath());
                if (plugin != null) {
                    plugins.add(plugin);
                }
            } catch (Exception e) {
                log.error("加载插件失败: jar={}, error={}", jarFile.getName(), e.getMessage(), e);
            }
        }

        log.info("插件加载完成: 目录={}, 成功加载={}", pluginDir, plugins.size());
        return plugins;
    }

    /**
     * 从 JAR 文件加载插件
     *
     * <p>使用独立的类加载器加载 JAR 文件中的插件实现。</p>
     *
     * @param jarPath 插件 JAR 文件路径
     * @return 加载的插件实例，如果未找到实现则返回 null
     * @throws PluginException 加载失败时抛出
     */
    public Plugin loadPlugin(Path jarPath) throws PluginException {
        log.debug("开始加载插件: {}", jarPath);

        try {
            URL jarUrl = jarPath.toUri().toURL();

            // 创建插件类加载器，实现类隔离
            PluginClassLoader classLoader = new PluginClassLoader(
                jarPath.toString(),
                new URL[]{jarUrl},
                getClass().getClassLoader(),
                SHARED_PACKAGES
            );

            log.debug("创建插件类加载器: jar={}, classLoader={}", jarPath, classLoader);

            // 使用 ServiceLoader 发现 Plugin 接口的实现
            ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);

            Iterator<Plugin> iterator = serviceLoader.iterator();
            if (!iterator.hasNext()) {
                log.warn("未找到 Plugin 接口的实现: {}", jarPath);
                classLoader.close();
                return null;
            }

            // 获取第一个实现
            Plugin plugin = iterator.next();

            // 检查是否有多个实现
            if (iterator.hasNext()) {
                log.warn("发现多个 Plugin 实现: {}，将使用第一个", jarPath);
            }

            // 获取插件元数据
            PluginMetadata metadata = plugin.getMetadata();
            log.info("发现插件实现: name={}, id={}, version={}, type={}",
                metadata.name(),
                metadata.id(),
                metadata.version(),
                metadata.type());

            // 创建加载记录
            LoadedPlugin loadedPlugin = new LoadedPlugin(
                metadata.id(),
                plugin,
                classLoader,
                jarPath,
                PluginState.LOADED
            );

            loadedPlugins.put(metadata.id(), loadedPlugin);

            log.info("插件加载成功: name={}, id={}, version={}, jar={}",
                metadata.name(), metadata.id(), metadata.version(), jarPath.getFileName());

            return plugin;

        } catch (Exception e) {
            log.error("插件加载异常: jar={}, error={}", jarPath, e.getMessage(), e);
            throw new PluginException(jarPath.toString(),
                "Failed to load plugin from " + jarPath, e);
        }
    }

    /**
     * 卸载插件
     *
     * <p>停止插件并释放其占用的所有资源，包括关闭类加载器。</p>
     *
     * @param pluginId 要卸载的插件ID
     * @throws PluginException 卸载失败时抛出
     */
    public void unloadPlugin(String pluginId) throws PluginException {
        log.info("开始卸载插件: {}", pluginId);

        LoadedPlugin loaded = loadedPlugins.get(pluginId);
        if (loaded == null) {
            log.warn("尝试卸载不存在的插件: {}", pluginId);
            throw new PluginException.PluginNotFoundException(pluginId);
        }

        try {
            Plugin plugin = loaded.plugin();

            // 如果插件已启动，先停止它
            if (plugin.getState() == PluginState.STARTED) {
                log.debug("停止插件: {}", pluginId);
                plugin.stop();
            }

            // 关闭类加载器，释放资源
            loaded.classLoader().close();
            loadedPlugins.remove(pluginId);

            log.info("插件卸载成功: id={}, jar={}",
                pluginId, loaded.jarPath().getFileName());

        } catch (Exception e) {
            log.error("插件卸载失败: id={}, error={}", pluginId, e.getMessage(), e);
            throw new PluginException(pluginId, "Failed to unload plugin: " + pluginId, e);
        }
    }

    /**
     * 获取已加载的插件实例
     *
     * @param pluginId 插件ID
     * @return 插件实例的Optional包装
     */
    public Optional<Plugin> getPlugin(String pluginId) {
        LoadedPlugin loaded = loadedPlugins.get(pluginId);
        return Optional.ofNullable(loaded != null ? loaded.plugin() : null);
    }

    /**
     * 获取所有已加载的插件
     *
     * @return 插件实例集合
     */
    public Collection<Plugin> getAllPlugins() {
        return loadedPlugins.values().stream()
            .map(LoadedPlugin::plugin)
            .toList();
    }

    /**
     * 获取所有已加载插件的详细信息
     *
     * @return 不可修改的插件信息集合
     */
    public Collection<LoadedPlugin> getLoadedPluginInfo() {
        return Collections.unmodifiableCollection(loadedPlugins.values());
    }

    /**
     * 检查插件是否已加载
     *
     * @param pluginId 插件ID
     * @return 如果插件已加载返回true，否则返回false
     */
    public boolean isLoaded(String pluginId) {
        return loadedPlugins.containsKey(pluginId);
    }

    /**
     * 已加载插件信息记录
     *
     * <p>包含插件的加载信息和运行时状态。</p>
     *
     * @param pluginId 插件ID
     * @param plugin 插件实例
     * @param classLoader 插件的类加载器
     * @param jarPath JAR 文件路径
     * @param state 插件状态
     */
    public record LoadedPlugin(
        String pluginId,
        Plugin plugin,
        PluginClassLoader classLoader,
        Path jarPath,
        PluginState state
    ) {}
}
