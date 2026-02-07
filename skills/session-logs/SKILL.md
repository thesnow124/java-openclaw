---
name: session-logs
description: 查看 OpenClaw Lite 会话存档并回答历史问题
---

用于查看 OpenClaw Lite 的会话存档（JSON）并回答历史问题。

## 触发时机
- 用户问“之前我们聊过什么？”、“上次的结论是什么？”之类历史问题。
- 用户要求回顾某个时间点或某次对话的内容。

## 存档位置
- 默认路径：`data/session.json`（相对工作目录）。
- 如果配置了 `app.session-path` 或 `app.workspace-dir`，路径会随之变化。

## 数据结构（简化）
- `sessionId`: 会话 ID
- `updatedAt`: 更新时间（毫秒时间戳）
- `messages`: 消息数组
  - `role`: user / assistant / tool
  - `content`: 文本内容

## 使用流程
1. 使用 `read_file` 读取 `data/session.json`。
2. 根据用户问题定位相关消息：按时间顺序梳理，提取对应 `role` 与 `content`。
3. 若数据量大，先给“时间范围/关键词”选择项，请用户确定后再精读。

## 输出建议
- 先给简短概览（时间范围 + 关键结论）。
- 再给 3-7 条关键摘录（标注角色）。
- 必要时给出“下一步/是否需要展开”的提问。

## 约束与注意
- 不擅自修改存档；如需修正，请先征得用户确认，再用 `write_file`。
- 不暴露敏感信息；必要时先提醒用户。
