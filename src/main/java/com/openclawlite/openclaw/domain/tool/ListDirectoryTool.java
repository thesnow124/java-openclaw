package com.openclawlite.openclaw.domain.tool;

import com.openclawlite.openclaw.domain.agent.ToolCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 列出目录内容工具
 *
 * <p>列出指定目录下的文件和子目录，支持详细信息显示。</p>
 *
 * <h3>功能特点：</h3>
 * <ul>
 *   <li>支持相对路径和绝对路径</li>
 *   <li>自动进行路径安全检查</li>
 *   <li>分别显示目录和文件</li>
 *   <li>可选显示详细信息（文件大小、修改时间等）</li>
 *   <li>友好的中文界面和 emoji 图标</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>
 * // 基本用法
 * {
 *   "tool": "list_directory",
 *   "path": "src/main/java"
 * }
 *
 * // 显示详细信息
 * {
 *   "tool": "list_directory",
 *   "path": ".",
 *   "arguments": {
 *     "detail": true
 *   }
 * }
 * </pre>
 *
 * <h3>响应示例：</h3>
 * <pre>
 * list_directory: ok
 * 📁 目录: /workspace
 *
 * 包含 3 个项目：
 *
 * 📂 目录 (1):
 *   📁 src
 *
 * 📄 文件 (2):
 *   📄 pom.xml - 2.5 KB, 修改于 2024-01-01T12:00:00Z
 *   📄 README.md - 1.2 KB, 修改于 2024-01-01T12:00:00Z
 * </pre>
 *
 * @author OpenClaw Lite Team
 * @version 1.0
 * @see ToolHandler
 * @see ToolContext
 */
@Component
public class ListDirectoryTool implements ToolHandler {

    private static final Logger log = LoggerFactory.getLogger(ListDirectoryTool.class);

    @Override
    public String name() {
        return "list_directory";
    }

    @Override
    public String description() {
        return "列出指定目录下的文件和子目录。可以查看目录内容、文件大小、修改时间等信息。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "list_directory",
              "path": "目录路径（相对或绝对路径）",
              "arguments": {
                "detail": "是否显示详细信息（可选，默认false）"
              }
            }
            """;
    }

    /**
     * 执行列出目录操作
     *
     * <p>列出指定目录的内容，可选择是否显示详细信息。</p>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>解析目录路径并进行安全检查</li>
     *   <li>验证路径存在且为目录</li>
     *   <li>读取目录内容</li>
     *   <li>分类目录和文件</li>
     *   <li>格式化并返回结果</li>
     * </ol>
     *
     * @param call 工具调用对象
     * @param context 工具执行上下文
     * @return 格式化的目录列表
     */
    @Override
    public String execute(ToolCall call, ToolContext context) {
        String pathStr = call.getPath();
        log.debug("执行列出目录: path={}", pathStr);

        try {
            // 如果未指定路径，使用当前目录
            if (pathStr == null || pathStr.isBlank()) {
                pathStr = ".";
                log.debug("未指定路径，使用当前目录");
            }

            // 解析并安全化路径
            Path path = context.resolveSafePath(pathStr);
            log.debug("解析后的路径: {}", path);

            // 检查路径是否存在
            if (!Files.exists(path)) {
                log.warn("路径不存在: {}", path);
                return "list_directory: 错误 - 路径不存在: " + pathStr;
            }

            // 检查是否为目录
            if (!Files.isDirectory(path)) {
                log.warn("不是目录: {}", path);
                return "list_directory: 错误 - 不是目录: " + pathStr;
            }

            // 从 arguments 中获取 detail 参数
            boolean showDetail = false;
            var args = call.getArguments();
            if (args != null && args.containsKey("detail")) {
                Object detailObj = args.get("detail");
                if (detailObj instanceof Boolean) {
                    showDetail = (Boolean) detailObj;
                } else if (detailObj instanceof String) {
                    showDetail = Boolean.parseBoolean((String) detailObj);
                }
                log.debug("显示详细信息: {}", showDetail);
            }

            StringBuilder result = new StringBuilder();
            result.append("list_directory: ok\n");
            result.append("📁 目录: ").append(path).append("\n\n");

            // 读取目录内容
            try (Stream<Path> stream = Files.list(path)) {
                List<Path> paths = stream.toList();

                log.debug("找到 {} 个项目", paths.size());

                if (paths.isEmpty()) {
                    result.append("(空目录)");
                    log.info("目录为空: {}", path);
                } else {
                    result.append("包含 ").append(paths.size()).append(" 个项目：\n\n");

                    // 分类：目录和文件
                    List<Path> dirs = new ArrayList<>();
                    List<Path> files = new ArrayList<>();

                    for (Path p : paths) {
                        if (Files.isDirectory(p)) {
                            dirs.add(p);
                        } else {
                            files.add(p);
                        }
                    }

                    log.debug("分类结果: 目录={}, 文件={}", dirs.size(), files.size());

                    // 先显示目录
                    if (!dirs.isEmpty()) {
                        result.append("📂 目录 (").append(dirs.size()).append("):\n");
                        for (Path dir : dirs) {
                            result.append("  📁 ").append(dir.getFileName());
                            if (showDetail) {
                                String dirInfo = getDirInfo(dir);
                                result.append(" - ").append(dirInfo);
                            }
                            result.append("\n");
                        }
                        result.append("\n");
                    }

                    // 再显示文件
                    if (!files.isEmpty()) {
                        result.append("📄 文件 (").append(files.size()).append("):\n");
                        for (Path file : files) {
                            result.append("  📄 ").append(file.getFileName());
                            if (showDetail) {
                                String fileInfo = getFileInfo(file);
                                result.append(" - ").append(fileInfo);
                            }
                            result.append("\n");
                        }
                    }

                    log.info("成功列出目录: path={}, items={}", path, paths.size());
                }
            }

            return result.toString();

        } catch (IOException e) {
            log.error("列出目录失败: path={}, error={}", pathStr, e.getMessage(), e);
            return "list_directory: 错误 - " + e.getMessage();
        }
    }

    /**
     * 获取文件详细信息
     *
     * @param file 文件路径
     * @return 格式化的文件信息（大小和修改时间）
     * @throws IOException 读取文件信息失败
     */
    private String getFileInfo(Path file) throws IOException {
        long size = Files.size(file);
        String sizeStr = formatSize(size);
        return sizeStr + ", 修改于 " + Files.getLastModifiedTime(file);
    }

    /**
     * 获取目录详细信息
     *
     * @param dir 目录路径
     * @return 格式化的目录信息（包含的项目数）
     */
    private String getDirInfo(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            long count = stream.count();
            return count + " 个项目";
        } catch (IOException e) {
            log.warn("无法读取目录信息: dir={}, error={}", dir, e.getMessage());
            return "无法读取";
        }
    }

    /**
     * 格式化文件大小
     *
     * @param bytes 字节数
     * @return 格式化后的大小字符串
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
