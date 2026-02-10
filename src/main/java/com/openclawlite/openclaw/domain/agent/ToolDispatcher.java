package com.openclawlite.openclaw.domain.agent;

import com.openclawlite.openclaw.domain.tool.GlobalToolContext;
import com.openclawlite.openclaw.domain.tool.ToolContext;
import com.openclawlite.openclaw.domain.tool.ToolHandler;
import com.openclawlite.openclaw.domain.tool.ToolRegistry;
import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * 工具执行分发器
 *
 * <p>负责将工具调用请求分发到对应的处理器执行，核心职责：</p>
 * <ul>
 *   <li>维护工具注册表（工具名称到处理器的映射）</li>
 *   <li>提供安全的工具执行上下文（文件系统访问限制）</li>
 *   <li>根据配置控制全局文件访问权限</li>
 * </ul>
 *
 * <p>安全机制：</p>
 * <ul>
 *   <li>默认情况下，工具只能访问配置的工作区目录</li>
 *   <li>可通过配置启用全局文件访问（需谨慎）</li>
 *   <li>未知工具调用会被拒绝</li>
 * </ul>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
@Component
public class ToolDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ToolDispatcher.class);

    /** 工具注册表 */
    private final ToolRegistry registry;

    /** 工具执行上下文（包含工作区路径和访问权限） */
    private final ToolContext context;

    /** 是否启用全局文件系统访问 */
    private final boolean globalAccessEnabled;

    /**
     * 构造工具分发器
     *
     * @param properties 应用配置（包含工作区和权限设置）
     * @param registry 工具注册表
     */
    public ToolDispatcher(AppProperties properties, ToolRegistry registry) {
        this.registry = registry;

        // 解析并规范化工作区路径
        Path workspace = Path.of(properties.getWorkspaceDir()).toAbsolutePath().normalize();
        this.globalAccessEnabled = properties.isAllowGlobalAccess();

        // 根据配置创建适当的上下文
        if (globalAccessEnabled) {
            log.warn("⚠️  全局文件系统访问已启用！Agent 将能够访问系统中的任何文件。");
            this.context = new GlobalToolContext(workspace, globalAccessEnabled);
        } else {
            this.context = new ToolContext(workspace);
            log.info("工具执行限制在工作区范围内: {}", workspace);
        }

        log.info("工具上下文已初始化: workspace={}, globalAccess={}", workspace, globalAccessEnabled);
    }

    /**
     * 执行工具调用
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>验证工具调用参数</li>
     *   <li>从注册表查找对应的处理器</li>
   *   <li>在安全上下文中执行工具</li>
     *   <li>返回执行结果（或错误信息）</li>
     * </ol>
     *
     * @param call 工具调用请求
     * @return 工具执行结果的字符串表示
     */
    public String execute(ToolCall call) {
        // 参数验证
        if (call == null || call.getTool() == null) {
            log.error("工具调用无效: call={}", call);
            return "tool_error: 缺少工具名称";
        }

        // 标准化工具名称
        String tool = call.getTool().trim().toLowerCase();
        log.info("执行工具: {}", tool);

        // 查找工具处理器
        ToolHandler handler = registry.find(tool);
        if (handler == null) {
            log.warn("未知工具: {}", tool);
            return "tool_error: 未知工具 '" + tool + "'";
        }

        // 执行工具
        try {
            log.debug("开始执行工具: {}, 参数: {}", tool, call.getArgs());
            long startTime = System.currentTimeMillis();

            String result = handler.execute(call, context);

            long duration = System.currentTimeMillis() - startTime;
            log.debug("工具执行完成: {}, 耗时: {}ms, 结果长度: {} 字符",
                     tool, duration, result != null ? result.length() : 0);

            return result;

        } catch (Exception e) {
            log.error("工具执行异常: {}", tool, e);
            return "tool_error: " + e.getMessage();
        }
    }

    /**
     * 检查是否启用了全局文件访问
     *
     * @return true 表示启用全局访问，false 表示限制在工作区
     */
    public boolean isGlobalAccessEnabled() {
        return globalAccessEnabled;
    }
}
