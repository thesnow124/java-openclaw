# 🏗️ Java 项目架构重构方案

## 📊 当前结构分析

### 当前问题

1. **包结构不清晰**
   - `agent/` 和 `service/agent/` 有重叠
   - `api/` 和 `gateway/` 职责不明确
   - 扁平化的包结构（所有包都在 `com.openclawlite` 下）

2. **不符合 Java 惯例**
   - 缺少明确的分层架构
   - 没有使用 `core`、`common`、`util` 等标准包名
   - DTO 和实体类散落各处

3. **从 TypeScript 迁移的痕迹**
   - 包名照搬 TS 目录结构
   - 不符合 Java 企业级应用标准

---

## 🎯 目标架构（DDD + 标准分层）

```
com.openclawlite/
├── openclaw/                          # 领域层（业务核心）
│   ├── domain/                        # 领域模型
│   │   ├── agent/                     # Agent 领域
│   │   ├── session/                   # 会话领域
│   │   ├── channel/                   # 渠道领域
│   │   └── tool/                      # 工具领域
│   ├── application/                   # 应用服务层
│   │   ├── agent/                     # Agent 应用服务
│   │   ├── session/                   # 会话应用服务
│   │   └── channel/                   # 渠道应用服务
│   └── infrastructure/                # 基础设施层
│       ├── persistence/               # 持久化
│       ├── messaging/                 # 消息队列
│       └── config/                     # 配置
│
├── common/                            # 通用模块
│   ├── dto/                          # 数据传输对象
│   ├── enums/                        # 枚举
│   ├── exception/                    # 异常定义
│   └── util/                         # 工具类
│
├── adapter/                           # 适配器层
│   ├── rest/                         # REST 控制器
│   ├── websocket/                    # WebSocket
│   └── channel/                      # 渠道适配器
│
└── config/                            # 配置类
    ├── SpringConfig.java             # Spring 配置
    ├── IntegrationConfig.java        # 集成配置
    └── SecurityConfig.java           # 安全配置
```

---

## 📋 重构步骤

### 步骤 1: 创建新的包结构（不删除旧代码）
### 步骤 2: 逐层迁移代码
### 步骤 3: 更新所有 import 语句
### 步骤 4: 测试编译
### 步骤 5: 删除旧的空包

---

## 🚀 或者：更简单的重构方案

如果不进行大规模重构，可以**只调整包名**，使其更清晰：

```
com.openclawlite/
├── core/                              # 核心业务逻辑
│   ├── agent/                        # Agent 核心
│   ├── session/                     # 会话管理
│   ├── tool/                         # 工具系统
│   └── memory/                       # 记忆系统
│
├── adapter/                           # 适配器
│   ├── rest/                         # REST API
│   ├── channel/                      # 渠道适配器
│   └── protocol/                     # 协议处理
│
├── infrastructure/                     # 基础设施
│   ├── persistence/                 # 数据访问
│   ├── config/                       # 配置
│   └── embedding/                    # Embedding
│
├── model/                             # 数据模型
│   ├── entity/                       # 实体
│   ├── dto/                          # DTO
│   └── vo/                           # 视图对象
│
└── util/                              # 工具类
    ├── FileUtil.java
    └── JsonUtil.java
```

---

## ❓ 你的选择

### 选项 A：完整重构（DDD 架构）
- ✅ 符合企业级标准
- ✅ 清晰的分层
- ❌ 需要大量重命名和移动
- ⏱️ 需要 2-3 小时

### 选项 B：简单重构（调整包名）
- ✅ 更清晰的结构
- ✅ 保持代码不变
- ❌ 不算完美
- ⏱️ 需要 30-60 分钟

### 选项 C：只优化当前结构
- ✅ 最快
- ❌ 仍然不够清晰

---

你选择哪个方案？我推荐 **选项 B（简单重构）**，既能提升可读性，又不会花费太多时间。
