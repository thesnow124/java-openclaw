package com.openclawlite.openclaw.domain.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 工具调用解析器
 *
 * <p>负责解析 AI 模型的输出，识别两种情况：</p>
 * <ul>
 *   <li>工具调用请求：提取工具名称、参数和元数据</li>
 *   <li>最终回答：直接返回文本内容</li>
 * </ul>
 *
 * <p>解析策略：</p>
 * <ol>
 *   <li>尝试从输出中提取 JSON 片段</li>
 *   <li>解析 JSON 中的 tool 字段判断是否为工具调用</li>
 *   <li>如解析失败或无 tool 字段，则视为最终回答</li>
 * </ol>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
@Component
public class ToolParser {

    private static final Logger log = LoggerFactory.getLogger(ToolParser.class);

    /** JSON 对象映射器 */
    private final ObjectMapper objectMapper;

    /**
     * 构造工具解析器
     *
     * @param objectMapper JSON 解析器
     */
    public ToolParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        log.debug("工具解析器已初始化");
    }

    /**
     * 解析模型输出，提取工具调用或最终文本
     *
     * @param raw 模型的原始输出文本
     * @return 解析结果（工具调用或最终文本）
     */
    public ToolParseResult parse(String raw) {
        log.trace("开始解析模型输出，长度: {} 字符", raw != null ? raw.length() : 0);

        // 空值检查
        if (raw == null || raw.isBlank()) {
            log.debug("模型输出为空，返回空文本");
            return ToolParseResult.finalText("");
        }

        // 提取 JSON 片段
        String json = extractJson(raw);
        if (json == null) {
            log.debug("未找到 JSON 片段，视为最终文本");
            return ToolParseResult.finalText(raw.trim());
        }

        log.trace("提取的 JSON: {}", json);

        try {
            // 解析 JSON
            JsonNode node = objectMapper.readTree(json);

            // 检查是否为工具调用
            if (node.has("tool")) {
                String tool = text(node, "tool");
                String path = text(node, "path");
                String content = text(node, "content");

                if (tool != null && !tool.isBlank()) {
                    // 提取所有参数（移除 tool 字段）
                    java.util.Map<String, Object> args =
                        objectMapper.convertValue(node, new TypeReference<java.util.Map<String, Object>>() {});
                    if (args != null) {
                        args.remove("tool");
                    }

                    log.info("识别到工具调用: tool={}, argsCount={}", tool, args != null ? args.size() : 0);
                    log.trace("工具参数: {}", args);

                    return ToolParseResult.tool(new ToolCall(tool.trim(), path, content, args));
                }
            }

            // 检查是否有 final 字段（明确标记的最终回答）
            if (node.has("final")) {
                String finalText = text(node, "final");
                log.debug("识别到最终回答（final 字段）");
                return ToolParseResult.finalText(finalText);
            }

            // JSON 不匹配预期结构，回退为原始文本
            log.debug("JSON 结构不匹配，回退为原始文本");
            return ToolParseResult.finalText(raw.trim());

        } catch (Exception e) {
            // JSON 解析失败，回退为原始文本
            log.warn("JSON 解析失败，回退为原始文本: {}", e.getMessage());
            return ToolParseResult.finalText(raw.trim());
        }
    }

    /**
     * 读取 JSON 节点中的字符串字段
     *
     * @param node JSON 节点
     * @param field 字段名
     * @return 字段值（字符串），如字段不存在或为 null 则返回 null
     */
    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    /**
     * 从文本中提取最外层 JSON 片段
     *
     * <p>查找第一个 { 和最后一个 }，提取中间的内容作为 JSON。</p>
     *
     * @param raw 原始文本
     * @return 提取的 JSON 字符串，如无效则返回 null
     */
    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');

        if (start == -1 || end == -1 || end <= start) {
            return null;
        }

        return raw.substring(start, end + 1).trim();
    }
}
