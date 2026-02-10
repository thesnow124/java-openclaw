package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.openclaw.domain.agent.ToolCall;

import java.util.Map;

/**
 * 工具处理器接口
 *
 * <p>定义工具的元数据、参数模式和执行方法。</p>
 *
 * <p>所有可被 AI 智能体调用的工具都必须实现此接口。</p>
 *
 * <h3>工具类型：</h3>
 * <ul>
 *   <li>内置工具：系统预置的工具（如 read_file、list_directory）</li>
 *   <li>命令工具：执行外部命令的工具（如 bash、python）</li>
 *   <li>插件工具：通过插件系统动态加载的工具</li>
 * </ul>
 *
 * <h3>工具实现示例：</h3>
 * <pre>
 * public class MyTool implements ToolHandler {
 *
 *     {@literal @}Override
 *     public String name() {
 *         return "my_tool";
 *     }
 *
 *     {@literal @}Override
 *     public String description() {
 *         return "我的自定义工具";
 *     }
 *
 *     {@literal @}Override
 *     public String usage() {
 *         return "{\"tool\":\"my_tool\",\"param\":\"value\"}";
 *     }
 *
 *     {@literal @}Override
 *     public String execute(ToolCall call, ToolContext context) {
 *         // 执行工具逻辑
 *         return "执行结果";
 *     }
 * }
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 * @see ToolCall
 * @see ToolContext
 * @see ToolResult
 */
public interface ToolHandler {

  /**
   * 获取工具名称
   *
   * <p>工具名称用于标识和调用工具，必须是唯一的。</p>
   * <p>建议使用小写字母和下划线，如：read_file、list_directory</p>
   *
   * @return 工具名称
   */
  String name();

  /**
   * 获取工具描述
   *
   * <p>简要描述工具的功能，用于向 AI 展示工具用途。</p>
   * <p>这个描述会被包含在提示词中，帮助 AI 理解何时使用该工具。</p>
   *
   * @return 工具描述文本
   */
  String description();

  /**
   * 获取工具使用示例
   *
   * <p>提供工具调用的 JSON 示例，帮助 AI 理解如何调用工具。</p>
   *
   * @return JSON 格式的使用示例
   */
  String usage();

  /**
   * 获取工具参数的 JSON Schema
   *
   * <p>定义工具接受的参数结构，用于参数验证和提示词生成。</p>
   * <p>默认返回空对象，表示不需要参数。</p>
   *
   * <p>示例：</p>
   * <pre>
   * {
   *   "type": "object",
   *   "properties": {
   *     "path": {
   *       "type": "string",
   *       "description": "文件路径"
   *     }
   *   },
   *   "required": ["path"]
   * }
   * </pre>
   *
   * @return JSON Schema 格式的参数定义
   */
  default Map<String, Object> getParameterSchema() {
    return Map.of(
        "type", "object",
        "properties", Map.of()
    );
  }

  /**
   * 执行工具（旧版方法）
   *
   * <p>执行工具逻辑并返回文本结果。</p>
   * <p>这个方法用于向后兼容，新工具建议使用 executeTyped。</p>
   *
   * @param call 工具调用对象，包含工具名称、参数等信息
   * @param context 工具执行上下文，提供工作区、配置等资源
   * @return 执行结果的文本表示
   */
  String execute(ToolCall call, ToolContext context);

  /**
   * 执行工具并返回结构化结果（新版方法）
   *
   * <p>执行工具逻辑并返回包含状态和元数据的结构化结果。</p>
   * <p>默认实现调用 execute() 方法，子类可以重写以提供更丰富的结果信息。</p>
   *
   * @param call 工具调用对象
   * @param context 工具执行上下文
   * @return 结构化的工具执行结果
   */
  default ToolResult executeTyped(ToolCall call, ToolContext context) {
    String text = execute(call, context);
    return ToolResult.success(text, null);
  }
}
