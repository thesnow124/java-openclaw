# OpenClaw Lite（Java + Spring AI）

这是一个用 Java 复现 OpenClaw 核心逻辑的“控制台版”示例，方便你用熟悉的 Java/Spring 代码理解核心架构与执行流程。

## 核心能力（当前已实现）

- 控制台对话循环（无 CLI 子命令）
- JSON 会话存档（`data/session.json`）
- Skills 加载 + frontmatter 解析（`name/description/user-invocable/disable-model-invocation`）
- 工具循环（模型 JSON → 执行 → 再返回）
- 工具注册表（内置 + 插件）
- 上下文窗口保护 + 自动压缩（超限自动摘要）
- Spring AI Alibaba + Spring AI（ZhiPuAI/GLM-4.7）

## 运行要求

- Java 21+（与 Spring Boot 3.5.x / Spring AI 1.1.x 对齐）
- Maven 3.8+

## 快速开始

1) 配置 GLM-4.7（ZhiPuAI）环境变量：

```
export ZHIPUAI_API_KEY="你的 API Key"
export ZHIPUAI_BASE_URL="https://open.bigmodel.cn/api/paas"
export ZHIPUAI_MODEL="glm-4.7"
```

说明：Spring AI ZhiPuAI 默认使用 `https://open.bigmodel.cn/api/paas`，无需追加 `/v4` 前缀。

2) 启动：

```
mvn -f java-openclaw-lite/pom.xml spring-boot:run
```

3) 控制台输入 `/exit` 退出。

## Skills 用法

Skills 目录：`java-openclaw-lite/skills/*/SKILL.md`

示例（frontmatter）：

```md
---
name: summarize
description: 总结文本
user-invocable: true
disable-model-invocation: false
---
```

- `disable-model-invocation: true` → 该 skill 不会进入系统提示词（模型不会自动用它）
- `user-invocable` 目前只记录，不做权限拦截（后续可扩展）

## 工具系统

### 内置工具

- `read_file`
- `write_file`

模型必须返回 JSON 形式调用工具，例如：

```json
{"tool":"read_file","path":"data/notes.txt"}
```

### 插件工具（tools 目录）

默认目录：`java-openclaw-lite/tools/`（可在配置中改）

示例：`tools/list_dir.json`

```json
{
  "name": "list_dir",
  "description": "列出工作区目录",
  "type": "command",
  "command": "ls -la",
  "usage": "{\"tool\":\"list_dir\"}"
}
```

启用命令工具（默认关闭）：

```yml
app:
  enable-command-tools: true
```

⚠️ 注意：命令工具有安全风险，仅用于本地受信环境。

## 上下文保护 + 自动压缩

当对话过长：
- 接近上限 → 提示“可能触发自动压缩”
- 超过上限 → 自动生成摘要并保留最近若干条消息

相关配置（`application.yml`）：

```yml
app:
  context-tokens: 8000
  context-warn-ratio: 0.8
  compaction-target-tokens: 2000
  compaction-keep-messages: 6
  compaction-input-max-chars: 12000
```

## 配置说明

`java-openclaw-lite/src/main/resources/application.yml`

```yml
app:
  workspace-dir: .
  session-path: data/session.json
  skills-dir: skills
  tools-dir: tools
  enable-command-tools: false
  max-tool-steps: 100
  context-tokens: 8000
  context-warn-ratio: 0.8
  compaction-target-tokens: 2000
  compaction-keep-messages: 6
  compaction-input-max-chars: 12000
```

## 常见问题

- **为什么提示“无法列出目录”**  
  说明命令型插件工具未启用或未配置。请创建 `tools/*.json` 并设置 `enable-command-tools: true`。

- **为什么技能不生效**  
  请确认 `skills-dir` 配置正确，且 `SKILL.md` 使用了合法 frontmatter。

---

如需继续迁移 OpenClaw 的其他模块（路由、会话缓存、模型 fallback 等），直接告诉我优先级即可。  
