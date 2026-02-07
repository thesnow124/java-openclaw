package com.openclawlite.agent;

// 表示压缩执行的结果状态。
public class CompactionResult {
  private final boolean ok;
  private final boolean compacted;
  private final String message;

  private CompactionResult(boolean ok, boolean compacted, String message) {
    this.ok = ok;
    this.compacted = compacted;
    this.message = message;
  }

  // 压缩成功并生成摘要。
  public static CompactionResult compacted(String summary) {
    return new CompactionResult(true, true, summary);
  }

  // 不需要压缩。
  public static CompactionResult noop() {
    return new CompactionResult(true, false, null);
  }

  // 压缩失败。
  public static CompactionResult fail(String message) {
    return new CompactionResult(false, false, message);
  }

  // 是否成功执行（包含不需要压缩）。
  public boolean isOk() {
    return ok;
  }

  // 是否发生了压缩。
  public boolean isCompacted() {
    return compacted;
  }

  // 失败原因或摘要内容。
  public String getMessage() {
    return message;
  }
}
