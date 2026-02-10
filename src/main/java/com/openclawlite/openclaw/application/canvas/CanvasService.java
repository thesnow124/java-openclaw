package com.openclawlite.openclaw.application.canvas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Canvas 服务
 * <p>
 * 负责创建可视化内容和结构化输出。
 * 提供灵活的画布（Canvas）功能，支持文本、表格、代码块等多种元素类型。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建和管理 Canvas 对象</li>
 *   <li>添加各种元素（文本、形状、表格、代码）</li>
 *   <li>将 Canvas 渲染为 Markdown 格式</li>
 *   <li>创建预定义的 Canvas 类型（数据表、代码块、报告等）</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>生成结构化的报告文档</li>
 *   <li>展示数据表格和统计信息</li>
 *   <li>格式化代码块展示</li>
 *   <li>创建可视化的分析结果</li>
 * </ul>
 */
@Service
public class CanvasService {

    private static final Logger log = LoggerFactory.getLogger(CanvasService.class);

    /**
     * 创建新的 Canvas
     * <p>
     * 创建一个指定类型和标题的空白 Canvas 对象。
     * </p>
     *
     * @param type Canvas 类型（如 "table", "code", "report" 等）
     * @param title Canvas 标题
     * @return 新创建的 Canvas 对象
     */
    public Canvas createCanvas(String type, String title) {
        log.debug("创建 Canvas: type={}, title={}", type, title);
        return new Canvas(
            UUID.randomUUID().toString(),  // 生成唯一 ID
            type,
            title,
            System.currentTimeMillis()     // 创建时间戳
        );
    }

    /**
     * 向 Canvas 添加文本元素
     *
     * @param canvas 目标 Canvas 对象
     * @param text 要添加的文本元素
     */
    public void addText(Canvas canvas, CanvasText text) {
        canvas.elements().add(text);
        log.debug("添加文本元素到 Canvas: canvasId={}", canvas.id());
    }

    /**
     * 向 Canvas 添加形状元素
     *
     * @param canvas 目标 Canvas 对象
     * @param shape 要添加的形状元素
     */
    public void addShape(Canvas canvas, CanvasShape shape) {
        canvas.elements().add(shape);
        log.debug("添加形状元素到 Canvas: canvasId={}", canvas.id());
    }

    /**
     * 向 Canvas 添加表格元素
     *
     * @param canvas 目标 Canvas 对象
     * @param table 要添加的表格元素
     */
    public void addTable(Canvas canvas, CanvasTable table) {
        canvas.elements().add(table);
        log.debug("添加表格元素到 Canvas: canvasId={}, rows={}", canvas.id(), table.rows().size());
    }

    /**
     * 向 Canvas 添加代码块元素
     *
     * @param canvas 目标 Canvas 对象
     * @param code 要添加的代码块元素
     */
    public void addCode(Canvas canvas, CanvasCode code) {
        canvas.elements().add(code);
        log.debug("添加代码块元素到 Canvas: canvasId={}, language={}", canvas.id(), code.language());
    }

    /**
     * 将 Canvas 渲染为 Markdown 格式
     * <p>
     * 遍历 Canvas 中的所有元素，将其转换为 Markdown 格式的文本。
     * 支持文本、代码块和表格元素。
     * </p>
     *
     * @param canvas 要渲染的 Canvas 对象
     * @return Markdown 格式的字符串
     */
    public String toMarkdown(Canvas canvas) {
        log.debug("渲染 Canvas 为 Markdown: canvasId={}, elements={}", canvas.id(), canvas.elements().size());

        StringBuilder sb = new StringBuilder();

        // 添加标题
        sb.append("# ").append(canvas.title()).append("\n\n");

        // 遍历所有元素并渲染
        for (CanvasElement element : canvas.elements()) {
            if (element instanceof CanvasText text) {
                // 渲染文本元素
                sb.append(text.content()).append("\n\n");
            } else if (element instanceof CanvasCode code) {
                // 渲染代码块元素
                sb.append("```").append(code.language()).append("\n");
                sb.append(code.content());
                sb.append("\n```\n\n");
            } else if (element instanceof CanvasTable table) {
                // 渲染表格元素
                renderTable(sb, table);
            }
        }

        log.debug("Markdown 渲染完成: canvasId={}, length={} chars", canvas.id(), sb.length());
        return sb.toString();
    }

    /**
     * 渲染表格为 Markdown 格式
     * <p>
     * 将表格数据转换为 Markdown 表格格式，包括表头、分隔符和数据行。
     * </p>
     *
     * @param sb 字符串构建器
     * @param table 要渲染的表格元素
     */
    private void renderTable(StringBuilder sb, CanvasTable table) {
        if (table.rows().isEmpty()) {
            log.debug("表格为空，跳过渲染");
            return;
        }

        // 渲染表头
        sb.append("| ");
        for (String header : table.headers()) {
            sb.append(header).append(" | ");
        }
        sb.append("|\n");

        // 渲染分隔符
        sb.append("| ");
        for (int i = 0; i < table.headers().size(); i++) {
            sb.append("--- | ");
        }
        sb.append("|\n");

        // 渲染数据行
        for (List<String> row : table.rows()) {
            sb.append("| ");
            for (String cell : row) {
                sb.append(cell != null ? cell : "").append(" | ");
            }
            sb.append("|\n");
        }

        sb.append("\n");
        log.debug("表格渲染完成: columns={}, rows={}", table.headers().size(), table.rows().size());
    }

    /**
     * 创建数据表格 Canvas
     * <p>
     * 快速创建一个包含表格数据的 Canvas。
     * </p>
     *
     * @param title Canvas 标题
     * @param headers 表头列表
     * @param rows 数据行列表
     * @return 包含表格的 Canvas 对象
     */
    public Canvas createDataTable(String title, List<String> headers, List<List<String>> rows) {
        log.info("创建数据表格 Canvas: title={}, headers={}, rows={}", title, headers.size(), rows.size());
        Canvas canvas = createCanvas("table", title);
        addTable(canvas, new CanvasTable(headers, rows));
        return canvas;
    }

    /**
     * 创建代码块 Canvas
     * <p>
     * 快速创建一个包含代码块的 Canvas。
     * </p>
     *
     * @param title Canvas 标题
     * @param language 编程语言（用于语法高亮）
     * @param code 代码内容
     * @return 包含代码块的 Canvas 对象
     */
    public Canvas createCodeCanvas(String title, String language, String code) {
        log.info("创建代码块 Canvas: title={}, language={}", title, language);
        Canvas canvas = createCanvas("code", title);
        addCode(canvas, new CanvasCode(language, code));
        return canvas;
    }

    /**
     * 创建报告 Canvas
     * <p>
     * 快速创建一个包含键值对数据的报告 Canvas。
     * </p>
     *
     * @param title Canvas 标题
     * @param data 键值对数据映射
     * @return 包含报告数据的 Canvas 对象
     */
    public Canvas createReport(String title, Map<String, Object> data) {
        log.info("创建报告 Canvas: title={}, entries={}", title, data.size());
        Canvas canvas = createCanvas("report", title);

        // 添加标题
        addText(canvas, new CanvasText("## " + title));

        // 添加键值对数据
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            addText(canvas, new CanvasText(
                "- **" + entry.getKey() + "**: " + entry.getValue()
            ));
        }

        return canvas;
    }
}
