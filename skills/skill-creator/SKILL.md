---
name: skill-creator
description: 在 OpenClaw Lite 中创建或维护 skills
---

用于在 OpenClaw Lite 中创建或维护 skills 的最小规范与流程。

## 目录结构
每个技能一个目录，至少包含一个 `SKILL.md`：

```
skills/<skill-name>/SKILL.md
```

加载逻辑：系统会把 `SKILL.md` 的正文拼到系统提示词中；第一行非空文本会被当作简短描述。

## 编写要点
- **简短可执行**：只写模型需要的“流程/约束/注意事项”。
- **触发条件**：列出用户可能的表述（关键词/问题类型）。
- **输入输出**：说明需要的输入与建议的输出格式。
- **工具限制**：当前仅有 `read_file` / `write_file`，不要写需要其他 CLI/系统工具的步骤。
- **确认节点**：凡是写文件或可能改变状态的操作，先征得用户确认。

## 推荐模板
```
---
name: <skill-name>
description: <一句话描述>
---

<一句话作用说明>

## 触发时机
- ...

## 使用流程
1. ...

## 输出建议
- ...

## 约束与注意
- ...
```

## 可选资料
如果需要补充资料，可以放在：
- `skills/<skill-name>/references/`
- `skills/<skill-name>/assets/`

但 OpenClaw Lite **不会自动加载这些文件**，需要通过 `read_file` 手动读取。
