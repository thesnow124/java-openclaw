package com.openclawlite.openclaw.domain.agent;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * AI 模型客户端接口
 *
 * <p>抽象 AI 模型调用的统一接口，支持多种实现方式：</p>
 * <ul>
 *   <li>Spring AI ChatModel（默认实现）</li>
 *   <li>OpenAI API 直连</li>
 *   <li>其他兼容模型接口</li>
 * </ul>
 *
 * <p>通过接口抽象，便于在不同模型实现之间切换。</p>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
public interface AiClient {

    /**
     * 调用 AI 模型进行对话
     *
     * @param messages 消息列表，包含对话历史和当前输入
     * @return 模型的原始文本响应
     */
    String chat(List<Message> messages);
}
