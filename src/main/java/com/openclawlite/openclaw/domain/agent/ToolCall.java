package com.openclawlite.openclaw.domain.agent;

// 模型请求的工具调用参数结构。
public class ToolCall {
  private String tool;
  private String path;
  private String content;
  private java.util.Map<String, Object> arguments;

  // 默认构造器，供序列化/反序列化使用。
  public ToolCall() {}

  // 构造一个完整的工具调用参数对象。
  public ToolCall(String tool, String path, String content) {
    this.tool = tool;
    this.path = path;
    this.content = content;
    this.arguments = null;
  }

  // 构造一个包含额外参数的工具调用对象。
  public ToolCall(String tool, String path, String content, java.util.Map<String, Object> arguments) {
    this.tool = tool;
    this.path = path;
    this.content = content;
    this.arguments = arguments;
  }

  // 获取工具名称。
  public String getTool() {
    return tool;
  }

  // 设置工具名称。
  public void setTool(String tool) {
    this.tool = tool;
  }

  // 获取文件路径参数。
  public String getPath() {
    return path;
  }

  // 设置文件路径参数。
  public void setPath(String path) {
    this.path = path;
  }

  // 获取写入内容参数。
  public String getContent() {
    return content;
  }

  // 设置写入内容参数。
  public void setContent(String content) {
    this.content = content;
  }

  // 获取额外参数（别名）。
  public java.util.Map<String, Object> getArgs() {
    return arguments;
  }

  // 设置额外参数（别名）。
  public void setArgs(java.util.Map<String, Object> args) {
    this.arguments = args;
  }

  // 获取额外参数。
  public java.util.Map<String, Object> getArguments() {
    return arguments;
  }

  // 设置额外参数。
  public void setArguments(java.util.Map<String, Object> arguments) {
    this.arguments = arguments;
  }
}
