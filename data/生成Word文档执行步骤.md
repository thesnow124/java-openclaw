# 生成 .docx 文件执行步骤

## 🎯 目标
生成包含标题、正文、列表和表格的 Word 文档：`data/document.docx`

## 📦 已完成
✅ Apache POI 依赖已在 pom.xml 中配置
✅ Java脚本已创建: scripts/GenerateDocx.java
✅ 执行工具已就绪: tools/run_java.sh

---

## 📄 文档内容预览

生成的文档包含：

### 📌 标题部分
- 主标题："项目报告"（18号字，加粗，居中）
- 副标题："2025年度工作总结"（12号字，斜体，居中）

### 📝 正文段落
- 两个自然段落，首行缩进
- 说明文档用途和 Apache POI 介绍

### 📋 功能列表
- 支持文档的创建、读取和修改
- 丰富的文本格式设置
- 支持段落样式和文档结构
- 支持表格、图片等复杂内容

### 📊 数据表格
| 项目 | 数量 | 完成率 |
|------|------|--------|
| 任务A | 50 | 100% |
| 任务B | 30 | 75% |
| 任务C | 20 | 50% |

### 📌 底部信息
- 生成日期：2025-01-04（灰色，斜体，右对齐）

---

## 🚀 执行步骤

### 步骤 1: 下载 Maven 依赖（如果还没执行过）
```bash
mvn dependency:copy-dependencies
```

### 步骤 2: 添加执行权限（如果还没执行过）
```bash
chmod +x tools/run_java.sh
```

### 步骤 3: 执行生成命令
```bash
./tools/run_java.sh scripts/GenerateDocx.java 'target/dependency/*'
```

**预期输出**：
```
📝 编译Java脚本: scripts/GenerateDocx.java
✅ 编译成功
🚀 执行Java程序: GenerateDocx
✅ Word文件已成功生成: data/document.docx
```

---

## ✅ 成功标志
- 看到 `✅ Word文件已成功生成: data/document.docx` 消息
- `data/` 目录下存在 `document.docx` 文件
- 用 Microsoft Word / WPS 可以正常打开文件

---

## ⚠️ 注意事项

1. **文件格式**: 生成的是 `.docx` 格式（Word 2007+），不是 `.doc`（Word 97-2003）格式
   - `.docx` 是现代 Word 的标准格式
   - 所有新版 Word 都可以打开

2. **字体兼容**: 文档使用中文字体"宋体"
   - 确保系统已安装中文字体

---

## 📝 备注
- 使用 Apache POI XWPF 库
- Java 版本: 21
- Apache POI 版本: 5.2.3
