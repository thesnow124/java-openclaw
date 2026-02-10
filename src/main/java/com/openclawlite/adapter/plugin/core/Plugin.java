package com.openclawlite.adapter.plugin.core;

import com.openclawlite.common.enums.PluginState;
import com.openclawlite.common.exception.PluginException;

/**
 * 插件核心接口
 *
 * <p>所有插件必须实现此接口。插件系统基于此接口管理插件的生命周期。</p>
 *
 * <h3>插件生命周期：</h3>
 * <ol>
 *   <li>加载 (LOADED): 插件被加载器发现并实例化</li>
 *   <li>初始化 (INITIALIZED): 调用 {@link #initialize(PluginContext)} 方法</li>
 *   <li>启动 (STARTED): 调用 {@link #start()} 方法，插件开始提供服务</li>
 *   <li>停止 (STOPPED): 调用 {@link #stop()} 方法，插件停止服务</li>
 * </ol>
 *
 * <h3>插件实现示例：</h3>
 * <pre>
 * public class MyPlugin implements Plugin {
 *
 *     private PluginContext context;
 *
 *     {@literal @}Override
 *     public PluginMetadata getMetadata() {
 *         return new PluginMetadata(
 *             "my-plugin",
 *             "My Plugin",
 *             "1.0.0",
 *             PluginType.CHANNEL,
 *             "My custom plugin",
 *             List.of()
 *         );
 *     }
 *
 *     {@literal @}Override
 *     public void initialize(PluginContext context) {
 *         this.context = context;
 *         // 初始化插件资源
 *     }
 *
 *     {@literal @}Override
 *     public void start() {
 *         // 启动插件服务
 *     }
 *
 *     {@literal @}Override
 *     public void stop() {
 *         // 清理插件资源
 *     }
 * }
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 * @see PluginMetadata
 * @see PluginContext
 * @see PluginState
 */
public interface Plugin {

    /**
     * 获取插件元数据
     *
     * <p>返回插件的描述信息，包括ID、名称、版本、类型等。</p>
     *
     * @return 插件元数据对象
     */
    PluginMetadata getMetadata();

    /**
     * 初始化插件
     *
     * <p>在插件加载后调用，用于初始化插件资源和配置。</p>
     * <p>此方法应该：</p>
     * <ul>
     *   <li>保存插件上下文引用</li>
     *   <li>初始化插件所需的资源</li>
     *   <li>验证插件配置</li>
     * </ul>
     *
     * @param context 插件上下文，提供系统资源和配置
     * @throws PluginException 初始化失败时抛出
     */
    void initialize(PluginContext context) throws PluginException;

    /**
     * 启动插件
     *
     * <p>在初始化完成后调用，启动插件的服务功能。</p>
     * <p>此方法应该：</p>
     * <ul>
     *   <li>启动后台线程或服务</li>
     *   <li>注册事件监听器</li>
     *   <li>建立外部连接</li>
     * </ul>
     *
     * @throws PluginException 启动失败时抛出
     */
    void start() throws PluginException;

    /**
     * 停止插件
     *
     * <p>在插件卸载前调用，停止插件的服务并清理资源。</p>
     * <p>此方法应该：</p>
     * <ul>
     *   <li>停止后台线程或服务</li>
     *   <li>注销事件监听器</li>
     *   <li>关闭外部连接</li>
     *   <li>释放占用的资源</li>
     * </ul>
     *
     * @throws PluginException 停止失败时抛出
     */
    void stop() throws PluginException;

    /**
     * 获取插件状态
     *
     * <p>返回插件当前的状态。默认返回 LOADED 状态。</p>
     * <p>子类可以重写此方法以提供更精确的状态信息。</p>
     *
     * @return 插件状态枚举
     */
    default PluginState getState() {
        return PluginState.LOADED;
    }

    /**
     * 检查插件是否启用
     *
     * <p>返回插件是否处于启用状态。默认返回 true。</p>
     * <p>子类可以重写此方法以实现动态启用/禁用逻辑。</p>
     *
     * @return 如果插件启用返回 true，否则返回 false
     */
    default boolean isEnabled() {
        return true;
    }
}
