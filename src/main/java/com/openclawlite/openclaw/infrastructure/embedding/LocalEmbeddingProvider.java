package com.openclawlite.openclaw.infrastructure.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地向量嵌入提供者
 * <p>
 * 简单的本地嵌入实现，使用类似 TF-IDF 的方法生成向量。
 * 不如 OpenAI 准确，但可以离线工作，无需外部 API 调用。
 * </p>
 *
 * <p>实现原理：</p>
 * <ul>
 *   <li>基于词频和哈希生成向量</li>
 *   <li>使用标准化向量确保可比较性</li>
 *   <li>构建词汇表进行词到 ID 的映射</li>
 * </ul>
 *
 * <p>适用场景：</p>
 * <ul>
 *   <li>开发和测试环境</li>
 *   <li>离线场景</li>
 *   <li>不需要高精度嵌入的场景</li>
 * </ul>
 *
 * <p>注意事项：</p>
 * 生产环境建议使用专业的嵌入服务（如 OpenAI）以获得更好的语义表示。
 */
@Component("localEmbeddingProvider")
@Primary
public class LocalEmbeddingProvider implements EmbeddingProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalEmbeddingProvider.class);

    /** 默认向量维度（常见的嵌入维度） */
    private static final int DEFAULT_DIMENSION = 384;

    /** 批量处理的最大大小 */
    private static final int MAX_BATCH_SIZE = 50;

    /** 词汇表：单词到 ID 的映射 */
    private final Map<String, Integer> vocabulary = new HashMap<>();

    /** 词汇表大小（用于生成新的词 ID） */
    private int vocabSize = 0;

    @Override
    public String getName() {
        return "local";
    }

    @Override
    public int getDimension() {
        return DEFAULT_DIMENSION;
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.isEmpty()) {
            log.debug("输入文本为空，返回零向量");
            return new float[DEFAULT_DIMENSION];
        }

        // 分词并构建词频向量
        Map<String, Integer> wordCounts = new HashMap<>();
        String[] tokens = text.toLowerCase().split("\\s+");

        // 统计每个词的出现频率
        for (String token : tokens) {
            wordCounts.put(token, wordCounts.getOrDefault(token, 0) + 1);
        }

        log.debug("文本分词完成: tokens={}, uniqueWords={}", tokens.length, wordCounts.size());

        // 构建嵌入向量
        float[] vector = new float[DEFAULT_DIMENSION];

        int i = 0;
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            if (i >= DEFAULT_DIMENSION) break;

            // 基于哈希的简单嵌入算法
            int wordId = getWordId(entry.getKey());
            // 使用词频的倒数平方根作为权重
            float value = (float) (1.0 / Math.sqrt(1 + entry.getValue()));

            // 将词的权重分散到向量维度
            vector[i % DEFAULT_DIMENSION] += value * (wordId % 100) / 100.0f;
            i++;
        }

        // 向量标准化（L2 归一化）
        float norm = 0;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);

        if (norm > 0) {
            for (int j = 0; j < vector.length; j++) {
                vector[j] /= norm;
            }
            log.debug("向量标准化完成: norm={}", norm);
        }

        return vector;
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        log.debug("批量生成嵌入: texts={}", texts.size());
        List<float[]> embeddings = new ArrayList<>();

        for (String text : texts) {
            embeddings.add(embed(text));
        }

        log.debug("批量嵌入生成完成: count={}", embeddings.size());
        return embeddings;
    }

    @Override
    public boolean isAvailable() {
        // 本地提供者始终可用
        return true;
    }

    @Override
    public int getMaxBatchSize() {
        return MAX_BATCH_SIZE;
    }

    /**
     * 获取单词 ID
     * <p>
     * 从词汇表中获取单词的唯一 ID。
     * 如果单词不存在，则分配新的 ID。
     * </p>
     *
     * @param word 单词
     * @return 单词 ID
     */
    private int getWordId(String word) {
        return vocabulary.computeIfAbsent(word, k -> vocabSize++);
    }
}
