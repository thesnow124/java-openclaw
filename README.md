# OpenClaw Lite

企业级 AI Agent 系统 - Java 实现

OpenClaw Lite 是一个功能强大的 AI Agent 系统，支持多渠道消息集成、插件系统、向量搜索内存管理等功能。

## 特性

### 核心功能
- ✅ **多渠道支持**: Telegram, WhatsApp, Lark (飞书) 等
- ✅ **Agent Identity 系统**: 自定义 Agent 个性化和配置
- ✅ **工具结果截断**: 智能上下文管理，防止溢出
- ✅ **插件系统**: 动态加载和管理插件
- ✅ **向量搜索内存**: 语义搜索和历史记录
- ✅ **问题自动修复**: 智能诊断和自动恢复
- ✅ **工具系统**: 可扩展的工具框架
- ✅ **媒体处理**: 图片、音频、视频处理管道
- ✅ **Web UI**: 管理控制台
- ✅ **CLI 命令行**: 命令行管理工具

### 技术栈
- **框架**: Spring Boot 3.5.10
- **Web**: Spring WebFlux (响应式)
- **AI**: Spring AI 1.1.2
- **数据库**: SQLite (生产可用，可切换 PostgreSQL)
- **前端**: Vue 3 + Element Plus
- **构建**: Maven + Vite

## 快速开始

### 环境要求
- Java 21+
- Maven 3.9+
- Node.js 18+ (可选，用于 UI 开发)

### 构建

\`\`\`bash
# 克隆仓库
git clone <repository-url>
cd java-openclaw-lite

# 构建项目
mvn clean package

# 运行
java -jar target/openclaw-lite-1.0.0.jar start
\`\`\`

### Docker 部署

\`\`\`bash
# 使用 docker-compose
docker-compose up -d

# 单独构建镜像
docker build -t openclaw-lite:latest .
\`\`\`

### 访问
- **API**: http://localhost:8080
- **WebSocket**: ws://localhost:8080/ws
- **Web UI**: http://localhost:8080 (构建后)
- **管理 API**: http://localhost:8080/api/admin

## 配置

### 应用配置

主要配置文件: `src/main/resources/application.yml`

### 渠道配置

1. **Telegram**:
   - 创建 Bot: https://t.me/BotFather
   - 获取 Bot Token
   - 配置 Webhook (可选)

2. **WhatsApp**:
   - 创建应用: https://developers.facebook.com
   - 获取 Phone Number ID 和 Access Token
   - 配置 Webhook

3. **Lark (飞书)**:
   - 创建应用: https://open.feishu.cn/app
   - 获取 App ID 和 App Secret
   - 配置事件订阅

### Agent 配置

通过 API 或 Web UI 创建和配置 Agent。

## CLI 使用

\`\`\`bash
# 启动服务
java -jar openclaw-lite.jar start

# 查看帮助
java -jar openclaw-lite.jar help

# 管理渠道
java -jar openclaw-lite.jar channel list
java -jar openclaw-lite.jar channel start telegram
\`\`\`

## API 文档

### REST API

#### 管理端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/admin/status` | GET | 系统状态 |
| `/api/admin/channels/stats` | GET | 渠道统计 |
| `/api/admin/sessions/stats` | GET | 会话统计 |

#### Gateway 协议

**连接**: `POST /api/gateway/connect`

**请求**: `POST /api/gateway/request`

更多 API 文档请参考 `docs/api.md`

## 开发

### 项目结构

\`\`\`
com.openclawlite
├── api/              # REST API
│   ├── rest/         # REST Controllers
│   ├── websocket/    # WebSocket Handlers
│   └── protocol/     # Gateway Protocol
├── gateway/          # Gateway Layer
│   ├── channel/      # Channel Management
│   ├── session/      # Session Management
│   └── plugin/       # Plugin System
├── service/          # Service Layer
│   ├── agent/        # Agent Services
│   ├── memory/       # Memory & Embeddings
│   ├── tool/         # Tool System
│   └── media/        # Media Processing
├── repository/       # Data Access Layer
└── config/          # Configuration
\`\`\`

### 添加新渠道

1. 实现 `ChannelPlugin` 接口
2. 注册到 `ChannelRegistry`
3. 添加配置适配器
4. 添加管理接口

### 添加新工具

1. 实现 `Tool` 接口
2. 添加 `@Component` 注解
3. 自动注册到 `ToolRegistry`

## 测试

\`\`\`bash
# 运行所有测试
mvn test

# 运行单元测试
mvn test -Dtest=unit

# 运行集成测试
mvn test -Dtest=integration
\`\`\`

## 性能优化

### 数据库优化
- HikariCP 连接池
- 批量操作
- 索引优化

### 缓存策略
- Redis 支持（可选）
- 本地缓存
- Embedding 缓存

### 异步处理
- 响应式 WebFlux
- 异步消息处理
- 并发控制

## 贡献

欢迎贡献！请查看 `CONTRIBUTING.md` 了解详情。

## 许可证

[Apache License 2.0](LICENSE)

## 支持

- 问题反馈: [GitHub Issues](https://github.com/openclawlite/java-openclaw-lite/issues)
- 文档: [Wiki](https://github.com/openclawlite/java-openclaw-lite/wiki)

---

**OpenClaw Lite** - Enterprise AI Agent System for Java
