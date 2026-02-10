package com.openclawlite.openclaw.domain.agent;

// 上下文窗口估算结果与动作建议。
public class ContextWindowStatus {
  private final int estimatedTokens;
  private final int maxTokens;
  private final boolean shouldWarn;
  private final boolean shouldCompact;

  // 创建上下文窗口状态对象。
  public ContextWindowStatus(
      int estimatedTokens,
      int maxTokens,
      boolean shouldWarn,
      boolean shouldCompact) {
    this.estimatedTokens = estimatedTokens;
    this.maxTokens = maxTokens;
    this.shouldWarn = shouldWarn;
    this.shouldCompact = shouldCompact;
  }

  // 获取估算的 token 数。
  public int getEstimatedTokens() {
    return estimatedTokens;
  }

  // 获取上下文窗口上限。
  public int getMaxTokens() {
    return maxTokens;
  }

  // 是否需要提示接近上限。
  public boolean isShouldWarn() {
    return shouldWarn;
  }

  // 是否需要执行压缩。
  public boolean isShouldCompact() {
    return shouldCompact;
  }
}
