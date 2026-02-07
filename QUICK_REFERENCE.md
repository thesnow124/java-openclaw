# java-openclaw-lite 快速参考

## 🚀 快速启动

```bash
cd /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite
mvn spring-boot:run
```

## 📚 可用中文技能

### 代码相关
- **代码审查** 🔍 - 审查代码质量、性能、安全
- **java-scripter** ☕ - Java 代码生成和脚本
- **github** 🐙 - GitHub CLI 操作

### 文档相关
- **技术文档** 📚 - 撰写 README、API 文档、用户指南
- **todo** ✅ - 待办事项管理

### 问题解决
- **问题排查** 🔧 - 系统化诊断技术问题

### 通用
- **hello** 👋 - 友好的问候和目标确认
- **session-logs** 📋 - 查看会话历史

## 🛠️ 可用工具

### 文件操作
- `read_file` - 读取文件
- `write_file` - 创建/覆盖文件
- `edit_file` - 编辑文件

### Office 生成
- `generate_excel` - 生成 Excel (.xlsx)
- `generate_word` - 生成 Word (.docx)

### 智能执行
- `smart_exec` - 自动修复问题的命令执行
- `exec` - 普通命令执行

### 图像分析
- `analyze_image` - 分析图像内容（需配置视觉模型）

### 技能管理
- `skills_status` - 查询技能状态
- `diagnose_problem` - 问题诊断和建议

## 💡 使用示例

### 代码审查
```
请帮我审查这段代码的性能和安全性
```

### 生成文档
```
帮我的项目写一个专业的 README
```

### 问题排查
```
我的应用启动失败了，报错 OutOfMemoryError
```

### 生成 Excel
```
{"tool": "generate_excel", "path": "data/report.xlsx", "content": {...}}
```

### 分析图像
```
{"tool": "analyze_image", "image_path": "/path/to/image.png"}
```

## 📖 完整文档

- **技能指南**: `skills/README.md`
- **中文技能报告**: `SKILLS_CHINESE_REPORT.md`
- **增强总结**: `ENHANCEMENT_SUMMARY.md`

## 🎯 核心特性

- ✅ 主动性 System Prompt
- ✅ 中文技能支持（10+）
- ✅ 智能问题诊断
- ✅ Office 文件生成
- ✅ 图像分析基础
- ✅ 代码审查能力

**版本**: v2.1 - 中文技能增强版
