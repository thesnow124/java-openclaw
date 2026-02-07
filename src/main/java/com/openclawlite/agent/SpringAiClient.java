package com.openclawlite.agent;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gaoshuanglong
 */
@Component
// 基于 Spring AI ChatModel 的模型调用实现。
public class SpringAiClient implements AiClient {
    private final ChatModel chatModel;

    // 注入 Spring AI 的 ChatModel。
    public SpringAiClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    // 将消息封装为 Prompt 并调用模型，返回文本结果。
    public String chat(List<Message> messages) {
        Prompt prompt = new Prompt(messages);
        ChatResponse response = chatModel.call(prompt);
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        return response.getResult().getOutput().getText();
    }
}
