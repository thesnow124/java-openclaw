package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.openclaw.domain.agent.ToolCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 读取文件工具
 *
 * <p>读取工作区内的文本文件内容。</p>
 *
 * <h3>功能特点：</h3>
 * <ul>
 *   <li>支持相对路径和绝对路径</li>
 *   <li>自动进行路径安全检查，防止目录遍历攻击</li>
 *   <li>使用 UTF-8 编码读取文件</li>
 *   <li>返回文件的完整内容</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * {
 *   "tool": "read_file",
 *   "path": "src/main/java/App.java"
 * }
 * </pre>
 *
 * <h3>响应示例：</h3>
 * <pre>
 * read_file: ok
 * package com.example;
 *
 * public class App {
 *     public static void main(String[] args) {
 *         System.out.println("Hello World!");
 *     }
 * }
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 * @see ToolHandler
 * @see ToolContext
 */
@Component
public class ReadFileTool implements ToolHandler {

  private static final Logger log = LoggerFactory.getLogger(ReadFileTool.class);

  @Override
  public String name() {
    return "read_file";
  }

  @Override
  public String description() {
    return "读取工作区内的文本文件内容。支持相对路径和绝对路径。";
  }

  @Override
  public String usage() {
    return "{\"tool\":\"read_file\",\"path\":\"文件路径（相对或绝对）\"}";
  }

  /**
   * 执行读取文件操作
   *
   * <p>读取指定路径的文件内容并返回。</p>
   *
   * <p>执行流程：</p>
   * <ol>
   *   <li>解析文件路径并进行安全检查</li>
   *   <li>检查文件是否存在</li>
   *   <li>使用 UTF-8 编码读取文件内容</li>
   *   <li>返回文件内容</li>
   * </ol>
   *
   * @param call 工具调用对象，必须包含 path 参数
   * @param context 工具执行上下文
   * @return 文件内容，或错误信息
   */
  @Override
  public String execute(ToolCall call, ToolContext context) {
    String pathStr = call.getPath();
    log.debug("执行读取文件: path={}", pathStr);

    try {
      // 解析并安全化路径
      Path path = context.resolveSafePath(pathStr);
      log.debug("解析后的路径: {}", path);

      // 检查文件是否存在
      if (!Files.exists(path)) {
        log.warn("文件不存在: {}", path);
        return "read_file: 文件不存在: " + pathStr;
      }

      // 检查是否为常规文件
      if (!Files.isRegularFile(path)) {
        log.warn("不是常规文件: {}", path);
        return "read_file: 不是常规文件: " + pathStr;
      }

      // 读取文件内容
      String content = Files.readString(path, StandardCharsets.UTF_8);

      log.info("成功读取文件: path={}, size={} bytes", path, content.length());
      log.debug("文件内容预览: {}", content.substring(0, Math.min(100, content.length())));

      return "read_file: ok\n" + content;

    } catch (Exception e) {
      log.error("读取文件失败: path={}, error={}", pathStr, e.getMessage(), e);
      return "read_file: 错误 " + e.getMessage();
    }
  }
}
