package com.openclawlite.openclaw.domain.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring AI 模型客户端实现
 *
 * <p>基于 Spring AI 框架的 ChatModel 接口实现 AI 模型调用。</p>
 * <p>支持多种模型提供商（OpenAI、Azure、Anthropic 等）的统一调用方式。</p>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
@Component
public class SpringAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(SpringAiClient.class);

    /** Spring AI 聊天模型实例 */
    private final ChatModel chatModel;

    /**
     * 构造 Spring AI 客户端
     *
     * @param chatModel Spring AI 聊天模型（由 Spring 容器自动注入）
     */
    public SpringAiClient(ChatModel chatModel) {
        this.chatModel = chatModel;
        log.info("Spring AI 客户端已初始化");
    }

    /**
     * 调用 AI 模型进行对话
     *
     * <p>将消息列表封装为 Spring AI 的 Prompt 对象，调用模型并返回文本结果。</p>
     *
     * @param messages 消息列表
     * @return 模型响应的文本内容
     */
    @Override
    public String chat(List<Message> messages) {
        log.debug("调用 Spring AI 模型，消息数: {}", messages.size());

        // 封装为 Prompt 对象
        Prompt prompt = new Prompt(messages);

        // 调用模型
        ChatResponse response = chatModel.call(prompt);

        // 提取并返回文本
        if (response == null) {
            log.warn("模型返回 null 响应");
            return "";
        }

        if (response.getResult() == null || response.getResult().getOutput() == null) {
            log.warn("模型响应结果为空");
            return "";
        }

        String text = response.getResult().getOutput().getText();
        log.debug("模型响应长度: {} 字符", text != null ? text.length() : 0);

        return text;
    }
}
