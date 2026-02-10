package com.openclawlite.openclaw.infrastructure.embedding;

import java.util.List;

/**
 * 向量嵌入提供者接口
 * <p>
 * 定义文本向量嵌入生成器的标准接口。
 * 向量嵌入用于将文本转换为数值向量，以便进行语义搜索和相似度计算。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>为单个文本生成向量嵌入</li>
 *   <li>批量生成多个文本的向量嵌入</li>
 *   <li>提供嵌入维度信息</li>
 *   <li>检查提供者可用性</li>
 * </ul>
 *
 * <p>实现类：</p>
 * <ul>
 *   <li>LocalEmbeddingProvider: 本地简单实现（基于 TF-IDF）</li>
 *   <li>OpenAIEmbeddingProvider: OpenAI API 实现（待实现）</li>
 * </ul>
 */
public interface EmbeddingProvider {

    /**
     * 获取提供者名称
     * <p>
     * 返回提供者的唯一标识名称。
     * </p>
     *
     * @return 提供者名称（如 "local", "openai" 等）
     */
    String getName();

    /**
     * 获取向量嵌入的维度
     * <p>
     * 返回生成向量数组的维度大小。
     * </p>
     *
     * @return 向量维度（如 384, 768, 1536 等）
     */
    int getDimension();

    /**
     * 为单个文本生成向量嵌入
     * <p>
     * 将输入文本转换为数值向量。
     * </p>
     *
     * @param text 输入文本
     * @return 向量数组
     */
    float[] embed(String text);

    /**
     * 批量生成多个文本的向量嵌入
     * <p>
     * 一次调用为多个文本生成向量嵌入，通常比单独调用更高效。
     * </p>
     *
     * @param texts 输入文本列表
     * @return 向量数组列表
     */
    List<float[]> embedBatch(List<String> texts);

    /**
     * 检查提供者是否可用
     * <p>
     * 检查提供者是否处于可用状态（如 API 密钥是否配置、服务是否可达等）。
     * </p>
     *
     * @return 如果可用返回 true，否则返回 false
     */
    boolean isAvailable();

    /**
     * 获取批量处理的最大大小
     * <p>
     * 返回单次批量处理可以处理的最大文本数量。
     * </p>
     *
     * @return 最大批量大小
     */
    int getMaxBatchSize();

    /**
     * 向量嵌入结果
     * <p>
     * 包含向量嵌入及其相关元数据的数据类。
     * </p>
     *
     * @param vector 向量数组
     * @param dimension 向量维度
     * @param tokens 使用的 token 数量
     * @param durationMs 处理耗时（毫秒）
     */
    record EmbeddingResult(
        float[] vector,
        int dimension,
        long tokens,
        long durationMs
    ) {}
}
