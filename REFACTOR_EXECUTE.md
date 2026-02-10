# 🏗️ DDD 架构完整重构执行计划

## 🎯 目标架构

```
com.openclawlite.openclaw/
├── domain/                    # 领域层（业务核心）
│   ├── agent/                # Agent 领域模型和服务
│   ├── session/             # 会话领域
│   ├── channel/             # 渠道领域
│   ├── tool/                # 工具领域
│   └── memory/              # 记忆领域
├── application/             # 应用服务层
│   ├── agent/                # Agent 应用服务
│   ├── session/             # 会话应用服务
│   ├── channel/             # 渠道应用服务
│   └── tool/                # 工具应用服务
├── infrastructure/          # 基础设施层
│   ├── persistence/         # 数据访问
│   ├── config/             # 配置
│   ├── messaging/          # 消息队列
│   └── embedding/          # Embedding
├── common/                   # 通用模块
│   ├── dto/                 # 数据传输对象
│   ├── exception/          # 异常定义
│   ├── enums/               # 枚举
│   └── util/                # 工具类
└── adapter/                  # 适配器层
    ├── rest/                # REST 控制器
    ├── websocket/         # WebSocket 处理
    ├── channel/            # 渠道适配器
    └── protocol/           # 协议处理
```

---

## 📋 迁移映射表

### Domain 层

| 旧包 | 新包 |
|------|------|
| `agent/` | `openclaw/domain/agent/` |
| `gateway/session/` | `openclaw/domain/session/` |
| `gateway/channel/core/` | `openclaw/domain/channel/` |
| `gateway/channel/impl/` | `openclaw/domain/channel/impl/` |
| `agent/tools/` | `openclaw/domain/tool/` |
| `service/memory/` | `openclaw/domain/memory/` |
| `service/embedding/` | `openclaw/infrastructure/embedding/` |

### Application 层

| 旧包 | 新包 |
|------|------|
| `service/agent/` | `openclaw/application/agent/` |
| `agent/` (AgentService) | `openclaw/application/agent/` |

### Infrastructure 层

| 旧包 | 新包 |
|------|------|
| `repository/` | `openclaw/infrastructure/persistence/` |
| `config/` | `openclaw/infrastructure/config/` |

### Adapter 层

| 旧包 | 新包 |
|------|------|
| `api/rest/` | `openclaw/adapter/rest/` |
| `api/websocket/` | `openclaw/adapter/websocket/` |
| `api/protocol/` | `openclaw/adapter/protocol/` |
| `gateway/channel/` | `openclaw/adapter/channel/` |
| `gateway/plugin/` | `openclaw/infrastructure/messaging/` |

### Common 层

| 旧包 | 新包 |
|------|------|
| `gateway/channel/core/` (ChannelCapabilities) | `openclaw/common/enums/` |
| `gateway/core/` | `openclaw/common/` |
| (新建) | `openclaw/common/dto/` |
| (新建) | `openclaw/common/exception/` |
| (新建) | `openclaw/common/util/` |

---

## 🚀 执行步骤

1. ✅ 创建新包结构
2. ✅ 迁移 Domain 层
3. ✅ 迁移 Application 层
4. ✅ 迁移 Infrastructure 层
5. ✅ 创建 Common 模块
6. ✅ 迁移 Adapter 层
7. ✅ 更新 import 语句
8. ✅ 测试编译
9. ✅ 删除旧包

---

## ⏱️ 预计时间

- 创建结构: 5 分钟
- 迁移代码: 30-45 分钟
- 更新 import: 20-30 分钟
- 测试验证: 10 分钟

**总计: 约 1-1.5 小时**
