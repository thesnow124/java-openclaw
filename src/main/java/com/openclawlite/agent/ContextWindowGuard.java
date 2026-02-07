package com.openclawlite.agent;

import com.openclawlite.config.AppProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gaoshuanglong
 */
@Component
// 估算上下文长度并判断是否需要压缩或提示。
public class ContextWindowGuard {
  private final AppProperties properties;

  // 注入配置以获取上下文限制参数。
  public ContextWindowGuard(AppProperties properties) {
    this.properties = properties;
  }

  // 估算当前消息列表的 token 数并给出状态。
  public ContextWindowStatus evaluate(List<MessageRecord> messages) {
    int estimatedTokens = estimateTokens(messages);
    int maxTokens = Math.max(1, properties.getContextTokens());
    double warnRatio = Math.min(1.0, Math.max(0.0, properties.getContextWarnRatio()));
    boolean shouldWarn = estimatedTokens >= Math.ceil(maxTokens * warnRatio);
    boolean shouldCompact = estimatedTokens > maxTokens;
    return new ContextWindowStatus(estimatedTokens, maxTokens, shouldWarn, shouldCompact);
  }

  // 用字符数近似估算 token（中文场景取 2~4 字符/Token 的粗略平均）。
  public int estimateTokens(List<MessageRecord> messages) {
    if (messages == null || messages.isEmpty()) {
      return 0;
    }
    int chars = 0;
    for (MessageRecord record : messages) {
      if (record == null || record.getContent() == null) {
        continue;
      }
      chars += record.getContent().length();
    }
    return Math.max(1, chars / 4);
  }
}
