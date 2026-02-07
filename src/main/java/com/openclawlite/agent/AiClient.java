package com.openclawlite.agent;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

// 模型调用抽象接口，便于替换不同实现。
public interface AiClient {
  // 将消息列表发送给模型并返回原始文本响应。
  String chat(List<Message> messages);
}
