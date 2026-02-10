package com.openclawlite.openclaw.domain.channel.impl.telegram;

import com.openclawlite.adapter.channel.base.ChannelMessageConverter;
import com.openclawlite.openclaw.domain.channel.core.ChannelMessage;

import java.util.Map;

/**
 * Telegram message converter
 */
public class TelegramMessageConverter extends ChannelMessageConverter {

    @Override
    public ChannelMessage toInternal(Object rawMessage) {
        ChannelMessage msg = new ChannelMessage();
        msg.setChannelId("telegram");
        msg.setText("Test message");
        return msg;
    }

    @Override
    public Object fromInternal(ChannelMessage message) {
        return Map.of(
            "text", message.getText()
        );
    }

    @Override
    public String extractSenderId(Object rawMessage) {
        return "telegram-user";
    }

    @Override
    public String extractChatId(Object rawMessage) {
        return "telegram-chat";
    }
}
