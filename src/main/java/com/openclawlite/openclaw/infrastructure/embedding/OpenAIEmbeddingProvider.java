package com.openclawlite.openclaw.infrastructure.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Embedding Provider
 * Uses OpenAI API for text embeddings
 */
@Component
public class OpenAIEmbeddingProvider implements EmbeddingProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAIEmbeddingProvider.class);

    private static final String DEFAULT_MODEL = "text-embedding-3-small";  // 1536 dimensions
    private static final int DEFAULT_DIMENSION = 1536;
    private static final int MAX_BATCH_SIZE = 100;

    @Value("${openclaw.embedding.openai.api-key:}")
    private String apiKey;

    @Value("${openclaw.embedding.openai.model:text-embedding-3-small}")
    private String model;

    @Value("${openclaw.embedding.openai.api-url:https://api.openai.com}")
    private String apiUrl;

    @Value("${openclaw.embedding.openai.timeout:30000}")
    private int timeout;

    private final HttpClient httpClient;

    public OpenAIEmbeddingProvider() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(30000))
            .build();
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    public int getDimension() {
        return switch (model) {
            case "text-embedding-3-small" -> 1536;
            case "text-embedding-3-large" -> 3072;
            case "text-embedding-ada-002" -> 1536;
            default -> DEFAULT_DIMENSION;
        };
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isEmpty()) {
            return new float[getDimension()];
        }

        try {
            long start = System.currentTimeMillis();

            Map<String, Object> requestBody = Map.of(
                "input", text,
                "model", model
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/v1/embeddings"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(timeout))
                .POST(HttpRequest.BodyPublishers.ofString(jsonify(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("OpenAI API error: {}", response.body());
                return new float[getDimension()];
            }

            // Parse response
            Map<String, Object> responseBody = parseJson(response.body());
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");

            if (data == null || data.isEmpty()) {
                log.error("No embedding data in response");
                return new float[getDimension()];
            }

            List<Double> embedding = (List<Double>) data.get(0).get("embedding");
            float[] result = new float[embedding.size()];

            for (int i = 0; i < embedding.size(); i++) {
                result[i] = embedding.get(i).floatValue();
            }

            long duration = System.currentTimeMillis() - start;
            log.debug("Generated embedding in {} ms, dimension: {}", duration, result.length);

            return result;

        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            return new float[getDimension()];
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> embeddings = new ArrayList<>();

        // Process in batches
        for (int i = 0; i < texts.size(); i += MAX_BATCH_SIZE) {
            int end = Math.min(i + MAX_BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(i, end);

            try {
                long start = System.currentTimeMillis();

                Map<String, Object> requestBody = Map.of(
                    "input", batch,
                    "model", model
                );

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/v1/embeddings"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(timeout * 2))  // Longer timeout for batch
                    .POST(HttpRequest.BodyPublishers.ofString(jsonify(requestBody)))
                    .build();

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    log.error("OpenAI API error in batch: {}", response.body());
                    // Add zero vectors for failed batch
                    for (int j = 0; j < batch.size(); j++) {
                        embeddings.add(new float[getDimension()]);
                    }
                    continue;
                }

                Map<String, Object> responseBody = parseJson(response.body());
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");

                if (data != null) {
                    for (Map<String, Object> item : data) {
                        List<Double> embedding = (List<Double>) item.get("embedding");
                        float[] vector = new float[embedding.size()];

                        for (int j = 0; j < embedding.size(); j++) {
                            vector[j] = embedding.get(j).floatValue();
                        }

                        embeddings.add(vector);
                    }
                }

                long duration = System.currentTimeMillis() - start;
                log.debug("Batch embedding {} items in {} ms", batch.size(), duration);

            } catch (Exception e) {
                log.error("Failed to generate batch embeddings", e);
                // Add zero vectors for failed batch
                for (int j = 0; j < batch.size(); j++) {
                    embeddings.add(new float[getDimension()]);
                }
            }
        }

        return embeddings;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public int getMaxBatchSize() {
        return MAX_BATCH_SIZE;
    }

    /**
     * Simple JSON stringify
     */
    private String jsonify(Object obj) {
        StringBuilder sb = new StringBuilder();
        buildJson(obj, sb);
        return sb.toString();
    }

    private void buildJson(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            sb.append("\"").append(escapeJson((String) obj)).append("\"");
        } else if (obj instanceof Number) {
            sb.append(obj);
        } else if (obj instanceof Boolean) {
            sb.append(obj);
        } else if (obj instanceof Map) {
            sb.append("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                buildJson(entry.getValue(), sb);
                first = false;
            }
            sb.append("}");
        } else if (obj instanceof List) {
            sb.append("[");
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                buildJson(list.get(i), sb);
            }
            sb.append("]");
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Simple JSON parse (placeholder - would use proper JSON library)
     */
    private Map<String, Object> parseJson(String json) {
        // TODO: Use Jackson or other proper JSON library
        Map<String, Object> result = new java.util.HashMap<>();
        // Placeholder implementation
        return result;
    }
}
