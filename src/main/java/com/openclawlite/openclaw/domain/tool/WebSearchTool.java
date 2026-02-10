package com.openclawlite.openclaw.domain.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclawlite.openclaw.domain.agent.ToolCall;
import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web 搜索工具：支持 Brave Search API、Tavily API 等。
 */
@Component
public class WebSearchTool implements ToolHandler {

    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public WebSearchTool(AppProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String name() {
        return "web_search";
    }

    @Override
    public String description() {
        return "搜索网络并返回相关结果。支持搜索查询、结果数量限制、地区过滤等功能。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "web_search",
              "query": "搜索关键词",
              "count": 5,
              "country": "CN",
              "freshness": "pd"
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("query", Map.of(
            "type", "string",
            "description", "搜索查询字符串"
        ));
        properties.put("count", Map.of(
            "type", "integer",
            "description", "返回结果数量（1-20）",
            "minimum", 1,
            "maximum", 20,
            "default", 5
        ));
        properties.put("country", Map.of(
            "type", "string",
            "description", "国家代码（如 CN, US, UK）",
            "default", "CN"
        ));
        properties.put("freshness", Map.of(
            "type", "string",
            "description", "时间过滤：pd=一天内,pw=一周内,pm=一月内,py=一年内",
            "enum", List.of("pd", "pw", "pm", "py")
        ));

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of("query")
        );
    }

    @Override
    public String execute(ToolCall call, ToolContext context) {
        ToolResult result = executeTyped(call, context);
        return result.getText();
    }

    @Override
    public ToolResult executeTyped(ToolCall call, ToolContext context) {
        try {
            // 解析参数
            String query = (String) call.getArguments().get("query");
            if (query == null || query.trim().isEmpty()) {
                return ToolResult.error("缺少必需参数：query");
            }

            int count = parseInt(call.getArguments().get("count"), 5);
            String country = String.valueOf(call.getArguments().getOrDefault("country", "CN"));
            String freshness = (String) call.getArguments().get("freshness");

            // 检查 API 密钥
            String apiKey = getSearchApiKey();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return ToolResult.error("未配置搜索 API 密钥。请在 application.yml 中设置 app.search.api-key 或环境变量 SEARCH_API_KEY");
            }

            // 执行搜索（优先使用 Brave Search，回退到 Tavily）
            String result = performBraveSearch(apiKey, query, count, country, freshness);
            if (result == null) {
                result = performTavilySearch(apiKey, query, count);
            }

            if (result == null) {
                return ToolResult.error("搜索失败：所有 API 调用均失败");
            }

            return ToolResult.success(result);

        } catch (Exception e) {
            return ToolResult.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 使用 Brave Search API 执行搜索。
     */
    private String performBraveSearch(String apiKey, String query, int count, String country, String freshness) {
        try {
            String url = "https://api.search.brave.com/res/v1/web/search?" +
                "q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) +
                "&count=" + Math.min(count, 20) +
                "&country=" + country +
                (freshness != null ? "&freshness=" + freshness : "") +
                "&text_decorations=false" +
                "&search_lang=zh-CN";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "gzip")
                    .header("X-Subscription-Token", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode webResults = root.path("web").path("results");

            if (webResults.isEmpty()) {
                return "未找到相关结果。";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("找到 ").append(webResults.size()).append(" 个结果：\n\n");

            for (JsonNode item : webResults) {
                String title = item.path("title").asText();
                String itemUrl = item.path("url").asText();
                String snippet = item.path("description").asText();

                sb.append("**").append(escapeMarkdown(title)).append("**\n");
                sb.append("<").append(itemUrl).append(">\n");
                if (!snippet.isEmpty()) {
                    sb.append(snippet).append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 使用 Tavily API 执行搜索（备用）。
     */
    private String performTavilySearch(String apiKey, String query, int count) {
        try {
            String url = "https://api.tavily.com/search";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("api_key", apiKey);
            requestBody.put("query", query);
            requestBody.put("max_results", Math.min(count, 10));
            requestBody.put("search_depth", "basic");
            requestBody.put("include_answer", false);
            requestBody.put("include_raw_content", false);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode results = root.path("results");

            if (results.isEmpty()) {
                return "未找到相关结果。";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("找到 ").append(results.size()).append(" 个结果：\n\n");

            for (JsonNode item : results) {
                String title = item.path("title").asText();
                String itemUrl = item.path("url").asText();
                String content = item.path("content").asText();

                sb.append("**").append(escapeMarkdown(title)).append("**\n");
                sb.append("<").append(itemUrl).append(">\n");
                if (!content.isEmpty()) {
                    sb.append(content).append("\n");
                }
                sb.append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取搜索 API 密钥（支持多个来源）。
     */
    private String getSearchApiKey() {
        // 1. 从环境变量获取
        String key = System.getenv("SEARCH_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        key = System.getenv("BRAVE_SEARCH_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        key = System.getenv("TAVILY_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        // 2. 从系统属性获取
        key = System.getProperty("search.api-key");
        if (key != null && !key.trim().isEmpty()) {
            return key;
        }

        // 3. 从配置文件获取（需要扩展 AppProperties）
        // return properties.getSearchApiKey();

        return null;
    }

    /**
     * 转义 Markdown 特殊字符。
     */
    private String escapeMarkdown(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("*", "\\*")
                   .replace("_", "\\_")
                   .replace("`", "\\`")
                   .replace("[", "\\[")
                   .replace("]", "\\]");
    }

    /**
     * 安全解析整数值。
     */
    private int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
