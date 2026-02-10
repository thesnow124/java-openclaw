# OpenClaw Lite Java 项目使用指南

## 📋 项目概述

OpenClaw Lite 是一个基于 Java + Spring Boot 的 AI Agent 平台，支持多渠道消息接入、技能管理、记忆系统等功能。

**技术栈：**
- Java 21+
- Spring Boot 3.5.10
- Spring WebFlux (响应式编程)
- SQLite + HikariCP (数据库)
- Maven (构建工具)

---

## 🚀 快速开始

### 1. 环境要求

```bash
# 检查 Java 版本（需要 21+）
java -version

# 检查 Maven 版本
mvn -version
```

### 2. 编译项目

```bash
# 进入项目目录
cd /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite

# 清理并编译
mvn clean compile

# 或者打包（跳过测试）
mvn clean package -DskipTests
```

### 3. 运行项目

**方式一：使用 Maven 运行（推荐）**
```bash
mvn spring-boot:run
```

**方式二：使用 JAR 运行**
```bash
java -jar target/openclaw-lite-0.1.0.jar
```

### 4. 验证运行状态

```bash
# 检查健康状态
curl http://localhost:8080/actuator/health

# 预期输出：{"status":"UP"}
```

---

## 📂 项目目录结构

```
java-openclaw-lite/
├── src/main/java/com/openclawlite/
│   ├── agent/                      # AI Agent 核心逻辑
│   │   ├── AgentService.java       # Agent 服务
│   │   ├── SkillService.java       # 技能服务
│   │   └── ProblemSolver.java      # 问题解决器
│   │
│   ├── api/                        # API 层
│   │   ├── rest/                   # REST 控制器
│   │   ├── websocket/              # WebSocket 处理
│   │   └── protocol/               # Gateway 协议定义
│   │
│   ├── gateway/                    # Gateway 层
│   │   ├── channel/                # 渠道管理
│   │   │   ├── core/               # 核心接口
│   │   │   ├── impl/               # 渠道实现
│   │   │   │   ├── telegram/       # Telegram 渠道
│   │   │   │   ├── whatsapp/       # WhatsApp 渠道 (禁用)
│   │   │   │   └── lark/           # 飞书渠道 (禁用)
│   │   │   └── manager/            # 渠道管理器
│   │   ├── session/                # 会话管理
│   │   └── plugin/                 # 插件系统
│   │
│   ├── service/                    # 服务层
│   │   ├── agent/                  # Agent 相关服务
│   │   ├── memory/                 # 记忆和向量搜索
│   │   ├── embedding/              # Embedding 提供者
│   │   └── tool/                   # 工具系统
│   │
│   ├── repository/                 # 数据访问层
│   │   ├── session/                # 会话持久化
│   │   ├── memory/                 # 记忆持久化
│   │   └── agent/                  # Agent 配置
│   │
│   ├── config/                     # 配置类
│   │   ├── AppProperties.java      # 应用配置
│   │   └── DatabaseConfiguration.java
│   │
│   └── Application.java            # 主启动类
│
├── src/main/resources/
│   ├── application.yml             # 应用配置文件
│   └── data/                       # 数据目录（自动创建）
│       ├── session.db              # SQLite 数据库
│       └── memory.db               # 记忆数据库
│
└── pom.xml                         # Maven 配置
```

---

## ⚙️ 配置说明

### application.yml 配置

```yaml
# 服务器配置
server:
  port: 8080

# 应用配置
app:
  workspace-dir: .                  # 工作区目录
  session-path: data/session.json   # 会话存档路径
  skills-dir: skills                # 技能目录
  tools-dir: tools                  # 工具目录
  max-tool-steps: 100               # 每回合最大工具调用次数
  context-tokens: 8000              # 模型上下文窗口大小

# 数据库配置
spring:
  datasource:
    url: jdbc:sqlite:data/session.db
    driver-class-name: org.sqlite.JDBC
  hikari:
    maximum-pool-size: 1            # SQLite 单连接
```

---

## 🌐 API 端点

### 健康检查
```bash
curl http://localhost:8080/actuator/health
```

### 渠道管理（已禁用的端点）
```bash
# 获取所有渠道
curl http://localhost:8080/api/channels

# 启动渠道
curl -X POST http://localhost:8080/api/channels/telegram/start

# 停止渠道
curl -X POST http://localhost:8080/api/channels/telegram/stop
```

### Agent 管理（需要重新启用）
```bash
# 列出所有 Agent
curl http://localhost:8080/api/gateway/agents.list

# 创建 Agent
curl -X POST http://localhost:8080/api/gateway/agents.create \
  -H "Content-Type: application/json" \
  -d '{"name":"My Agent","emoji":"🤖","model":"gpt-4"}'
```

---

## 🛠️ 常用命令

### Maven 命令

```bash
# 清理编译
mvn clean compile

# 打包（跳过测试）
mvn clean package -DskipTests

# 运行
mvn spring-boot:run

# 查看依赖树
mvn dependency:tree

# 更新依赖
mvn versions:display-dependency-updates
```

### 调试命令

```bash
# 启用调试模式运行
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# 查看日志
tail -f /tmp/spring-run.log

# 检查端口占用
lsof -i :8080

# 停止应用
pkill -f "spring-boot:run"
```

---

## 📝 开发指南

### 添加新的渠道适配器

1. 创建渠道包：`src/main/java/com/openclawlite/gateway/channel/impl/mychannel/`
2. 实现核心接口：
   - `ChannelGatewayAdapter` - 启动/停止
   - `ChannelMessagingAdapter` - 消息处理
   - `ChannelOutboundAdapter` - 发送消息
3. 添加配置类
4. 在 `application.yml` 中配置

### 添加新的技能

1. 在 `skills/` 目录创建技能文件夹
2. 创建 `SKILL.md` 描述技能
3. 实现技能逻辑
4. 技能会自动被加载

---

## 🐛 故障排查

### 问题 1：编译失败
```bash
# 清理并重新编译
mvn clean compile -U
```

### 问题 2：端口被占用
```bash
# 查找占用进程
lsof -i :8080

# 杀死进程
kill -9 <PID>

# 或者修改端口
# 编辑 application.yml，修改 server.port
```

### 问题 3：数据库锁定
```bash
# 删除数据库文件
rm data/session.db
rm data/memory.db

# 重新启动应用会自动创建
```

### 问题 4：Bean 冲突
```bash
# 查看错误日志，找到冲突的 Bean
# 使用 @Qualifier 或 @Primary 注解解决
# 或者禁用冲突的组件
```

---

## 📚 下一步

### 已禁用的模块（待重新启用）
- ✅ Telegram 渠道（已启用）
- ❌ WhatsApp 渠道
- ❌ 飞书 渠道
- ❌ WebChat 渠道
- ❌ Gateway 协议处理器
- ❌ CLI 命令行界面

### 待完成功能
- [ ] 恢复测试目录并添加测试依赖
- [ ] 重新启用所有渠道适配器
- [ ] 完善 REST API 端点
- [ ] 添加 Web UI
- [ ] 完善 Agent Identity 系统
- [ ] 添加工具结果截断系统

---

## 📖 参考文档

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring WebFlux 文档](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [SQLite JDBC 文档](https://github.com/xerial/sqlite-jdbc)
- [项目原版](https://github.com/your-repo/openclaw)

---

**最后更新时间**: 2026-02-08
**维护者**: OpenClaw Lite Team
