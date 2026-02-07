package com.openclawlite.agent.tools;

import com.openclawlite.agent.ToolCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Web 获取工具：获取网页内容并转换为 Markdown 格式。
 */
@Component
public class WebFetchTool implements ToolHandler {

    private final HttpClient httpClient;

    @Autowired
    public WebFetchTool() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public String name() {
        return "web_fetch";
    }

    @Override
    public String description() {
        return "获取指定 URL 的网页内容，自动转换为易读的 Markdown 格式。支持 HTML 到 Markdown 的转换。";
    }

    @Override
    public String usage() {
        return """
            {
              "tool": "web_fetch",
              "url": "https://example.com"
            }
            """;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("url", Map.of(
            "type", "string",
            "description", "要获取的网页 URL"
        ));
        properties.put("format", Map.of(
            "type", "string",
            "description", "输出格式：markdown（默认）或 html",
            "enum", List.of("markdown", "html"),
            "default", "markdown"
        ));

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", List.of("url")
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
            String url = (String) call.getArguments().get("url");
            if (url == null || url.trim().isEmpty()) {
                return ToolResult.error("缺少必需参数：url");
            }

            String format = String.valueOf(call.getArguments().getOrDefault("format", "markdown"));

            // 验证 URL
            if (!isValidUrl(url)) {
                return ToolResult.error("无效的 URL：" + url);
            }

            // 获取网页内容
            String html = fetchUrl(url);

            if (html == null) {
                return ToolResult.error("无法获取网页内容：" + url);
            }

            // 转换为 Markdown
            String markdown = "markdown".equals(format) ? htmlToMarkdown(url, html) : html;

            return ToolResult.success(markdown);

        } catch (Exception e) {
            return ToolResult.error("获取网页失败：" + e.getMessage());
        }
    }

    /**
     * 获取 URL 内容。
     */
    private String fetchUrl(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "Mozilla/5.0 (compatible; OpenClawLite/1.0)")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            return response.body();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 简单的 HTML 到 Markdown 转换。
     * 注意：这是一个简化实现，生产环境建议使用专门的库如 jsoup。
     */
    private String htmlToMarkdown(String baseUrl, String html) {
        // 移除 script、style 等标签
        html = html.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        html = html.replaceAll("(?i)<style[^>]*>.*?</style>", "");
        html = html.replaceAll("(?i)<nav[^>]*>.*?</nav>", "");
        html = html.replaceAll("(?i)<footer[^>]*>.*?</footer>", "");
        html = html.replaceAll("(?i)<header[^>]*>.*?</header>", "");

        // 转换标题
        html = html.replaceAll("(?i)<h1[^>]*>", "# ");
        html = html.replaceAll("(?i)</h1>", "\n\n");
        html = html.replaceAll("(?i)<h2[^>]*>", "## ");
        html = html.replaceAll("(?i)</h2>", "\n\n");
        html = html.replaceAll("(?i)<h3[^>]*>", "### ");
        html = html.replaceAll("(?i)</h3>", "\n\n");
        html = html.replaceAll("(?i)<h4[^>]*>", "#### ");
        html = html.replaceAll("(?i)</h4>", "\n\n");
        html = html.replaceAll("(?i)<h5[^>]*>", "##### ");
        html = html.replaceAll("(?i)</h5>", "\n\n");
        html = html.replaceAll("(?i)<h6[^>]*>", "###### ");
        html = html.replaceAll("(?i)</h6>", "\n\n");

        // 转换段落
        html = html.replaceAll("(?i)<p[^>]*>", "\n");
        html = html.replaceAll("(?i)</p>", "\n\n");

        // 转换链接
        html = html.replaceAll("(?i)<a[^>]+href=\"([^\"]+)\"[^>]*>([^<]+)</a>", "[$2]($1)");

        // 转换粗体和斜体
        html = html.replaceAll("(?i)<strong[^>]*>", "**");
        html = html.replaceAll("(?i)</strong>", "**");
        html = html.replaceAll("(?i)<b[^>]*>", "**");
        html = html.replaceAll("(?i)</b>", "**");
        html = html.replaceAll("(?i)<em[^>]*>", "*");
        html = html.replaceAll("(?i)</em>", "*");
        html = html.replaceAll("(?i)<i[^>]*>", "*");
        html = html.replaceAll("(?i)</i>", "*");

        // 转换代码块
        html = html.replaceAll("(?i)<pre[^>]*><code[^>]*>", "```\\n");
        html = html.replaceAll("(?i)</code></pre>", "\\n```");
        html = html.replaceAll("(?i)<code[^>]*>", "`");
        html = html.replaceAll("(?i)</code>", "`");

        // 转换列表
        html = html.replaceAll("(?i)<ul[^>]*>", "\n");
        html = html.replaceAll("(?i)</ul>", "\n");
        html = html.replaceAll("(?i)<ol[^>]*>", "\n");
        html = html.replaceAll("(?i)</ol>", "\n");
        html = html.replaceAll("(?i)<li[^>]*>", "\n- ");
        html = html.replaceAll("(?i)</li>", "\n");

        // 转换块引用
        html = html.replaceAll("(?i)<blockquote[^>]*>", "\n> ");
        html = html.replaceAll("(?i)</blockquote>", "\n\n");

        // 转换水平线
        html = html.replaceAll("(?i)<hr[^>]*/?", "\n---\n");

        // 转换表格（简化）
        html = html.replaceAll("(?i)<table[^>]*>", "\n");
        html = html.replaceAll("(?i)</table>", "\n");
        html = html.replaceAll("(?i)<tr[^>]*>", "\n");
        html = html.replaceAll("(?i)</tr>", "\n");
        html = html.replaceAll("(?i)<th[^>]*>", "| ");
        html = html.replaceAll("(?i)</th>", " |");
        html = html.replaceAll("(?i)<td[^>]*>", "| ");
        html = html.replaceAll("(?i)</td>", " |");

        // 移除所有剩余的 HTML 标签
        html = html.replaceAll("<[^>]+>", "");

        // 清理多余的空行
        html = html.replaceAll("\n{3,}", "\n\n");

        // 解码 HTML 实体
        html = html.replace("&nbsp;", " ");
        html = html.replace("&lt;", "<");
        html = html.replace("&gt;", ">");
        html = html.replace("&amp;", "&");
        html = html.replace("&quot;", "\"");
        html = html.replace("&#39;", "'");
        html = html.replace("&mdash;", "—");
        html = html.replace("&ndash;", "–");
        html = html.replace("&hellip;", "…");

        return html.trim();
    }

    /**
     * 验证 URL 格式。
     */
    private boolean isValidUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equals("http") && !scheme.equals("https"))) {
                return false;
            }
            String host = uri.getHost();
            return host != null && !host.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
