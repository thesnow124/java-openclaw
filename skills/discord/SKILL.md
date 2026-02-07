---
name: discord
description: 通过 discord 工具控制 Discord：发送消息、表情反应、上传贴纸/表情、发起投票、管理线程/置顶/搜索、创建/编辑/删除频道和分类、获取权限/成员/角色/频道信息、设置机器人状态/活动，或在 Discord 私信/频道中执行管理操作
metadata: {"openclaw":{"emoji":"🎮","requires":{"config":["channels.discord"]}}}
---

# Discord 操作

## 概述

使用 `discord` 管理消息、反应、线程、投票和审核。你可以通过 `discord.actions.*` 禁用功能组（默认启用，除了角色/管理）。该工具使用为 OpenClaw 配置的机器人令牌。

## 需要收集的输入

- **反应**：`channelId`、`messageId` 和 `emoji`
- **获取消息**：`guildId`、`channelId`、`messageId`，或消息链接如 `https://discord.com/channels/<guildId>/<channelId>/<messageId>`
- **贴纸/投票/发送消息**：`to` 目标（`channel:<id>` 或 `user:<id>`），可选的 `content` 文本
- **投票**还需要 `question` 和 2-10 个 `answers`
- **媒体**：`mediaUrl`，本地文件使用 `file:///path`，远程文件使用 `https://...`
- **表情上传**：`guildId`、`name`、`mediaUrl`，可选的 `roleIds`（限制 256KB，PNG/JPG/GIF）
- **贴纸上传**：`guildId`、`name`、`description`、`tags`、`mediaUrl`（限制 512KB，PNG/APNG/Lottie JSON）

消息上下文行包含可直接重用的 `discord message id` 和 `channel` 字段。

**注意**：`sendMessage` 使用 `to: "channel:<id>"` 格式，而不是 `channelId`。其他操作如 `react`、`readMessages`、`editMessage` 直接使用 `channelId`。
**注意**：`fetchMessage` 接受消息 ID 或完整链接如 `https://discord.com/channels/<guildId>/<channelId>/<messageId>`。

## 操作

### 对消息进行反应

```json
{
  "action": "react",
  "channelId": "123",
  "messageId": "456",
  "emoji": "✅"
}
```

### 列出反应和用户

```json
{
  "action": "reactions",
  "channelId": "123",
  "messageId": "456",
  "limit": 100
}
```

### 发送贴纸

```json
{
  "action": "sticker",
  "to": "channel:123",
  "stickerIds": ["9876543210"],
  "content": "做得好！"
}
```

- 每条消息最多 3 个贴纸 ID
- `to` 可以是 `user:<id>` 用于私信

### 上传自定义表情

```json
{
  "action": "emojiUpload",
  "guildId": "999",
  "name": "party_blob",
  "mediaUrl": "file:///tmp/party.png",
  "roleIds": ["222"]
}
```

- 表情图片必须是 PNG/JPG/GIF 且 ≤ 256KB
- `roleIds` 是可选的；省略则让所有人都可以使用该表情

### 上传贴纸

```json
{
  "action": "stickerUpload",
  "guildId": "999",
  "name": "openclaw_wave",
  "description": "OpenClaw 挥手问好",
  "tags": "👋",
  "mediaUrl": "file:///tmp/wave.png"
}
```

- 贴纸需要 `name`、`description` 和 `tags`
- 上传文件必须是 PNG/APNG/Lottie JSON 且 ≤ 512KB

### 创建投票

```json
{
  "action": "poll",
  "to": "channel:123",
  "question": "午餐吃什么？",
  "answers": ["披萨", "寿司", "沙拉"],
  "allowMultiselect": false,
  "durationHours": 24,
  "content": "请投票"
}
```

- `durationHours` 默认为 24；最大 32 天（768 小时）

### 检查机器人在频道的权限

```json
{
  "action": "permissions",
  "channelId": "123"
}
```

## 可以尝试的想法

- 用 ✅/⚠️ 反应标记状态更新
- 为发布决策或会议时间发起快速投票
- 成功部署后发送庆祝贴纸
- 为发布时刻上传新表情/贴纸
- 在团队频道每周运行"优先级检查"投票
- 用户请求完成后发送私信贴纸确认

### 读取最近的消息

```json
{
  "action": "readMessages",
  "channelId": "123",
  "limit": 20
}
```

### 获取单条消息

```json
{
  "action": "fetchMessage",
  "guildId": "999",
  "channelId": "123",
  "messageId": "456"
}
```

```json
{
  "action": "fetchMessage",
  "messageLink": "https://discord.com/channels/999/123/456"
}
```

### 发送/编辑/删除消息

```json
{
  "action": "sendMessage",
  "to": "channel:123",
  "content": "来自 OpenClaw 的问候"
}
```

**附带媒体附件**：

```json
{
  "action": "sendMessage",
  "to": "channel:123",
  "content": "听听这个音频！",
  "mediaUrl": "file:///tmp/audio.mp3"
}
```

- `to` 使用格式 `channel:<id>` 或 `user:<id>` 用于私信（不是 `channelId`！）
- `mediaUrl` 支持本地文件（`file:///path/to/file`）和远程 URL（`https://...`）
- 可选的 `replyTo` 配合消息 ID 用于回复特定消息

```json
{
  "action": "editMessage",
  "channelId": "123",
  "messageId": "456",
  "content": "修正错字"
}
```

```json
{
  "action": "deleteMessage",
  "channelId": "123",
  "messageId": "456"
}
```

### 线程

```json
{
  "action": "threadCreate",
  "channelId": "123",
  "name": "缺陷分类",
  "messageId": "456"
}
```

```json
{
  "action": "threadList",
  "guildId": "999"
}
```

```json
{
  "action": "threadReply",
  "channelId": "777",
  "content": "在线程中回复"
}
```

### 置顶

```json
{
  "action": "pinMessage",
  "channelId": "123",
  "messageId": "456"
}
```

```json
{
  "action": "listPins",
  "channelId": "123"
}
```

### 搜索消息

```json
{
  "action": "searchMessages",
  "guildId": "999",
  "content": "发布说明",
  "channelIds": ["123", "456"],
  "limit": 10
}
```

### 成员和角色信息

```json
{
  "action": "memberInfo",
  "guildId": "999",
  "userId": "111"
}
```

```json
{
  "action": "roleInfo",
  "guildId": "999"
}
```

### 列出可用的自定义表情

```json
{
  "action": "emojiList",
  "guildId": "999"
}
```

### 角色变更（默认禁用）

```json
{
  "action": "roleAdd",
  "guildId": "999",
  "userId": "111",
  "roleId": "222"
}
```

### 频道信息

```json
{
  "action": "channelInfo",
  "channelId": "123"
}
```

```json
{
  "action": "channelList",
  "guildId": "999"
}
```

### 频道管理（默认禁用）

创建、编辑、删除和移动频道和分类。通过 `discord.actions.channels: true` 启用。

**创建文本频道**：

```json
{
  "action": "channelCreate",
  "guildId": "999",
  "name": "general-chat",
  "type": 0,
  "parentId": "888",
  "topic": "一般讨论"
}
```

- `type`：Discord 频道类型整数（0 = 文本，2 = 语音，4 = 分类；支持其他值）
- `parentId`：要嵌套在的分类 ID（可选）
- `topic`、`position`、`nsfw`：可选

**创建分类**：

```json
{
  "action": "categoryCreate",
  "guildId": "999",
  "name": "项目"
}
```

**编辑频道**：

```json
{
  "action": "channelEdit",
  "channelId": "123",
  "name": "new-name",
  "topic": "更新后的主题"
}
```

- 支持 `name`、`topic`、`position`、`parentId`（null 表示从分类中移除）、`nsfw`、`rateLimitPerUser`

**移动频道**：

```json
{
  "action": "channelMove",
  "guildId": "999",
  "channelId": "123",
  "parentId": "888",
  "position": 2
}
```

- `parentId`：目标分类（null 表示移动到顶层）

**删除频道**：

```json
{
  "action": "channelDelete",
  "channelId": "123"
}
```

**编辑/删除分类**：

```json
{
  "action": "categoryEdit",
  "categoryId": "888",
  "name": "重命名的分类"
}
```

```json
{
  "action": "categoryDelete",
  "categoryId": "888"
}
```

### 语音状态

```json
{
  "action": "voiceStatus",
  "guildId": "999",
  "userId": "111"
}
```

### 计划事件

```json
{
  "action": "eventList",
  "guildId": "999"
}
```

### 管理（默认禁用）

```json
{
  "action": "timeout",
  "guildId": "999",
  "userId": "111",
  "durationMinutes": 10
}
```

### 机器人状态/活动（默认禁用）

设置机器人的在线状态和活动。通过 `discord.actions.presence: true` 启用。

Discord 机器人只能设置活动的 `name`、`state`、`type` 和 `url`。其他活动字段（details、emoji、assets）被网关接受，但 Discord 会静默忽略机器人。

**各活动类型的字段渲染方式**：

- **playing、streaming、listening、watching、competing**：`activityName` 显示在机器人名称下方的侧边栏中（例如，对于类型 "playing" 和名称 "with fire"，显示为"**with fire**"）。`activityState` 显示在个人资料弹出窗口中。
- **custom**：`activityName` 被忽略。仅 `activityState` 作为状态文本显示在侧边栏中。
- **streaming**：`activityUrl` 可能被客户端显示或嵌入。

**设置播放状态**：

```json
{
  "action": "setPresence",
  "activityType": "playing",
  "activityName": "with fire"
}
```

侧边栏结果："**with fire**"。弹出窗口显示："Playing: with fire"

**带状态（在弹出窗口中显示）**：

```json
{
  "action": "setPresence",
  "activityType": "playing",
  "activityName": "My Game",
  "activityState": "In the lobby"
}
```

侧边栏结果："**My Game**"。弹出窗口显示："Playing: My Game (换行) In the lobby"。

**设置直播（可选 URL，可能不适用于机器人）**：

```json
{
  "action": "setPresence",
  "activityType": "streaming",
  "activityName": "Live coding",
  "activityUrl": "https://twitch.tv/example"
}
```

**设置收听/观看**：

```json
{
  "action": "setPresence",
  "activityType": "listening",
  "activityName": "Spotify"
}
```

```json
{
  "action": "setPresence",
  "activityType": "watching",
  "activityName": "the logs"
}
```

**设置自定义状态（侧边栏中的文本）**：

```json
{
  "action": "setPresence",
  "activityType": "custom",
  "activityState": "Vibing"
}
```

侧边栏结果："Vibing"。注意：`activityName` 对于自定义类型被忽略。

**仅设置机器人状态（无活动/清除状态）**：

```json
{
  "action": "setPresence",
  "status": "dnd"
}
```

**参数**：

- `activityType`：`playing`、`streaming`、`listening`、`watching`、`competing`、`custom`
- `activityName`：非自定义类型在侧边栏显示的文本（对于 `custom` 被忽略）
- `activityUrl`：直播类型的 Twitch 或 YouTube URL（可选；可能不适用于机器人）
- `activityState`：对于 `custom` 这是状态文本；对于其他类型显示在个人资料弹出窗口中
- `status`：`online`（默认）、`dnd`、`idle`、`invisible`

## Discord 写作风格指南

**保持对话式！** Discord 是聊天平台，不是文档平台。

### 要做

- 短小精悍的消息（理想 1-3 句）
- 多个快速回复 > 一大段文字
- 使用表情符号表达语气/强调 🦞
- 小写休闲风格也可以
- 将信息分解为易于理解的部分
- 匹配对话的能量

### 不要做

- 不要 markdown 表格（Discord 将它们渲染为丑陋的原始 `| text |`）
- 不要在休闲聊天中使用 `## 标题`（使用 **粗体** 或全大写表示强调）
- 避免多段式文章
- 不要过度解释简单的事情
- 跳过"我很乐意帮忙！"的客套话

### 有效的格式

- **粗体**用于强调
- `代码`用于技术术语
- 列表用于多个项目
- > 引用用于引用
- 将多个链接包装在 `<>` 中以抑制嵌入

### 示例转换

❌ 不好：

```
我很乐意帮忙！这是可用版本策略的全面概述：

## 语义化版本
Semver 使用 MAJOR.MINOR.PATCH 格式，其中...

## 日历版本
CalVer 使用基于日期的版本，如...
```

✅ 好：

```
版本选项：semver (1.2.3)、calver (2026.01.04) 或 yolo (`latest` 永远）。哪个适合你的发布节奏？
```

### 操作分组

使用 `discord.actions.*` 禁用操作组：

- `reactions`（react + reactions 列表 + emojiList）
- `stickers`、`polls`、`permissions`、`messages`、`threads`、`pins`、`search`
- `emojiUploads`、`stickerUploads`
- `memberInfo`、`roleInfo`、`channelInfo`、`voiceStatus`、`events`
- `roles`（角色添加/删除，默认 `false`）
- `channels`（频道/分类创建/编辑/删除/移动，默认 `false`）
- `moderation`（超时/踢出/封禁，默认 `false`）
- `presence`（机器人状态/活动，默认 `false`）
