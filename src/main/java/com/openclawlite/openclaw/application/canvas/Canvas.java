package com.openclawlite.openclaw.application.canvas;

import java.util.ArrayList;
import java.util.List;

/**
 * Canvas 画布类
 * <p>
 * 表示一个可包含多种元素的画布对象。
 * Canvas 可以包含文本、形状、表格、代码块等多种类型的元素。
 * </p>
 *
 * <p>字段说明：</p>
 * <ul>
 *   <li>id: Canvas 唯一标识符</li>
 *   <li>type: Canvas 类型（table、code、report 等）</li>
 *   <li>title: Canvas 标题</li>
 *   <li>createdAt: 创建时间戳</li>
 *   <li>elements: Canvas 包含的元素列表</li>
 * </ul>
 */
public class Canvas {
    /** Canvas 唯一标识符 */
    private final String id;

    /** Canvas 类型 */
    private final String type;

    /** Canvas 标题 */
    private final String title;

    /** 创建时间戳（毫秒） */
    private final long createdAt;

    /** Canvas 包含的元素列表 */
    private final List<CanvasElement> elements = new ArrayList<>();

    /**
     * 构造函数
     *
     * @param id Canvas 唯一标识符
     * @param type Canvas 类型
     * @param title Canvas 标题
     * @param createdAt 创建时间戳
     */
    public Canvas(String id, String type, String title, long createdAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.createdAt = createdAt;
    }

    /** 获取 Canvas ID */
    public String id() { return id; }

    /** 获取 Canvas 类型 */
    public String type() { return type; }

    /** 获取 Canvas 标题 */
    public String title() { return title; }

    /** 获取创建时间戳 */
    public long createdAt() { return createdAt; }

    /** 获取元素列表 */
    public List<CanvasElement> elements() { return elements; }
}

/**
 * Canvas 元素接口
 * <p>
 * 所有 Canvas 元素的公共接口。
 * </p>
 */
interface CanvasElement {}

/**
 * 文本元素
 * <p>
 * 表示纯文本内容，可以包含 Markdown 格式。
 * </p>
 *
 * @param content 文本内容
 */
record CanvasText(
    String content
) implements CanvasElement {}

/**
 * 形状元素
 * <p>
 * 表示图形形状，包含类型和属性。
 * </p>
 *
 * @param type 形状类型（如 "rectangle", "circle", "line" 等）
 * @param properties 形状属性（如颜色、大小、位置等）
 */
record CanvasShape(
    String type,
    java.util.Map<String, Object> properties
) implements CanvasElement {}

/**
 * 表格元素
 * <p>
 * 表示表格数据，包含表头和多行数据。
 * </p>
 *
 * @param headers 表头列表
 * @param rows 数据行列表（每行是一个字符串列表）
 */
record CanvasTable(
    List<String> headers,
    List<List<String>> rows
) implements CanvasElement {}

/**
 * 代码块元素
 * <p>
 * 表示代码块，支持语法高亮。
 * </p>
 *
 * @param language 编程语言（用于语法高亮）
 * @param content 代码内容
 */
record CanvasCode(
    String language,
    String content
) implements CanvasElement {}
