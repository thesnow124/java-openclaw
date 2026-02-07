---
name: gemini
description: Gemini CLI 用于一次性问答、总结和生成。
homepage: https://ai.google.dev/
metadata:
  {
    "openclaw":
      {
        "emoji": "♊️",
        "requires": { "bins": ["gemini"] },
        "install":
          [
            {
              "id": "brew",
              "kind": "brew",
              "formula": "gemini-cli",
              "bins": ["gemini"],
              "label": "Install Gemini CLI (brew)",
            },
          ],
      },
  }
---

# Gemini CLI

使用一次性模式下的 Gemini，配合位置提示（避免交互模式）。

快速开始

- `gemini "回答这个问题..."`
- `gemini --model <name> "提示..."`
- `gemini --output-format json "返回 JSON"`

扩展功能

- 列表：`gemini --list-extensions`
- 管理：`gemini extensions <command>`

注意事项

- 如果需要身份验证，请以交互方式运行一次 `gemini` 并按照登录流程操作。
- 为安全起见，避免使用 `--yolo`。
