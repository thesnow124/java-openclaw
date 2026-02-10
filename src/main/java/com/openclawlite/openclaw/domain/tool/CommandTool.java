package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.openclaw.domain.agent.ToolCall;
import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 命令型插件工具
 *
 * <p>执行预定义的外部命令并返回输出结果。</p>
 *
 * <h3>功能特点：</h3>
 * <ul>
 *   <li>执行配置文件中定义的 shell 命令</li>
 *   <li>支持通过 stdin 向命令传递输入</li>
 *   <li>捕获命令的标准输出和错误输出</li>
 *   <li>返回命令的退出码和输出内容</li>
 * </ul>
 *
 * <h3>安全考虑：</h3>
 * <ul>
 *   <li>默认情况下禁用，需要在配置中显式启用</li>
 *   <li>命令在工作区目录下执行</li>
 *   <li>建议限制可执行的命令类型</li>
 * </ul>
 *
 * <h3>配置示例：</h3>
 * <pre>
 * openclaw:
 *   tools:
 *     enable-command-tools: true
 *     command-tools:
 *       - name: "bash"
 *         command: "bash"
 *         description: "执行 bash 命令"
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 * @see ToolHandler
 * @see ToolPluginDefinition
 */
public class CommandTool implements ToolHandler {

  private static final Logger log = LoggerFactory.getLogger(CommandTool.class);

  /** 工具定义，包含命令名称、描述和实际命令 */
  private final ToolPluginDefinition definition;

  /** 应用配置，用于检查是否启用命令工具 */
  private final AppProperties properties;

  /**
   * 构造函数
   *
   * @param definition 工具插件定义
   * @param properties 应用配置
   */
  public CommandTool(ToolPluginDefinition definition, AppProperties properties) {
    this.definition = definition;
    this.properties = properties;
    log.debug("创建命令工具: name={}, command={}",
        definition.getName(), definition.getCommand());
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

  /**
   * 执行命令工具
   *
   * <p>执行配置的外部命令，并返回其输出结果。</p>
   *
   * <p>执行流程：</p>
   * <ol>
   *   <li>检查命令工具是否启用</li>
   *   <li>验证命令配置</li>
   *   <li>在工作区目录下启动命令进程</li>
   *   <li>如果提供了输入内容，写入进程的 stdin</li>
   *   <li>读取进程的 stdout 和 stderr</li>
   *   <li>等待进程结束并获取退出码</li>
   *   <li>返回格式化的结果</li>
   * </ol>
   *
   * @param call 工具调用对象
   * @param context 工具执行上下文
   * @return 命令执行结果，包含退出码和输出
   */
  @Override
  public String execute(ToolCall call, ToolContext context) {
    log.debug("执行命令工具: name={}", name());

    // 检查命令工具是否启用
    if (!properties.isEnableCommandTools()) {
      log.warn("命令工具未启用: enable-command-tools=false");
      return "tool_error: 未启用命令型工具（enable-command-tools=false）";
    }

    // 获取命令
    String command = definition.getCommand();
    if (command == null || command.isBlank()) {
      log.error("命令工具未配置 command: name={}", name());
      return "tool_error: 插件工具未配置 command";
    }

    try {
      log.info("执行命令: command={}, workspace={}", command, context.getWorkspace());

      // 构建并启动进程
      ProcessBuilder builder = new ProcessBuilder("bash", "-lc", command);
      builder.directory(context.getWorkspace().toFile());  // 设置工作目录
      builder.redirectErrorStream(true);  // 合并 stdout 和 stderr
      Process process = builder.start();

      // 如果提供了输入内容，写入进程的 stdin
      if (call != null && call.getContent() != null && !call.getContent().isBlank()) {
        String input = call.getContent();
        log.debug("写入命令输入: length={}", input.length());
        process.getOutputStream().write(input.getBytes());
        process.getOutputStream().flush();
      }

      // 关闭 stdin
      process.getOutputStream().close();

      // 读取命令输出
      StringBuilder output = new StringBuilder();
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
        }
      }

      // 等待进程结束并获取退出码
      int exitCode = process.waitFor();

      log.info("命令执行完成: command={}, exitCode={}, outputLength={}",
          command, exitCode, output.length());

      return "command: exit=" + exitCode + "\n" + output.toString().trim();

    } catch (Exception e) {
      log.error("命令执行失败: command={}, error={}", command, e.getMessage(), e);
      return "command: error " + e.getMessage();
    }
  }
}
