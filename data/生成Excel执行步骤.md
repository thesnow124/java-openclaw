# 生成 .xlsx 文件执行步骤

## 🎯 目标
生成包含「人员、部门、名称」列的 data/excel.xlsx 文件

## 📦 已完成
✅ Apache POI 依赖已添加到 pom.xml
✅ Java脚本已创建: scripts/GenerateExcel.java
✅ 执行工具已就绪: tools/run_java.sh

---

## 🚀 执行步骤（按顺序执行）

### 步骤 1: 下载 Maven 依赖
在终端执行：
```bash
mvn dependency:copy-dependencies
```
**说明**: 这会将 Apache POI 的所有 jar 文件下载到 `target/dependency/` 目录

### 步骤 2: 添加执行权限
```bash
chmod +x tools/run_java.sh
```

### 步骤 3: 执行生成命令
```bash
./tools/run_java.sh scripts/GenerateExcel.java 'target/dependency/*'
```

**预期输出**：
```
📝 编译Java脚本: scripts/GenerateExcel.java
✅ 编译成功
🚀 执行Java程序: GenerateExcel
✅ Excel文件已成功生成: data/excel.xlsx
```

---

## 📊 生成的 Excel 文件内容

| 人员 | 部门 | 名称 |
|------|------|------|
| 张三 | 技术部 | 张三 |
| 李四 | 市场部 | 李四 |
| 王五 | 财务部 | 王五 |
| 赵六 | 人事部 | 赵六 |

**文件路径**: `data/excel.xlsx`

---

## ⚠️ 常见问题

### 问题1: mvn 命令未找到
**解决**: 安装 Maven
- macOS: `brew install maven`
- Ubuntu: `sudo apt install maven`

### 问题2: 编译失败
**可能原因**: Maven 依赖下载不完整
**解决**: 重新运行 `mvn dependency:copy-dependencies`

### 问题3: 找不到主类 GenerateExcel
**可能原因**: 编译失败或类路径问题
**解决**: 检查是否有编译错误信息

---

## ✅ 成功标志
- 看到 `✅ Excel文件已成功生成: data/excel.xlsx` 消息
- `data/` 目录下存在 `excel.xlsx` 文件
- 用 Excel 可以正常打开文件

---

## 📝 备注
- 这是一个标准的 Java Spring Boot + Maven 项目
- Java 版本: 21
- Apache POI 版本: 5.2.3
