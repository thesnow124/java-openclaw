# Excel文件生成指南

## 📋 任务说明
生成包含「人员、部门、名称」列的 xlsx 文件

## 🎯 文件准备情况
✅ Java脚本已生成: `scripts/GenerateExcel.java`
✅ 执行工具已就绪: `tools/run_java.sh`

## 📦 前置要求

### 1. Java环境
需要安装 JDK 8 或更高版本
```bash
java -version
javac -version
```

### 2. Apache POI 依赖
需要下载 Apache POI 库：

#### 选项A: 手动下载
1. 访问: https://poi.apache.org/download.html
2. 下载: poi-bin-5.2.3-xxxxx.zip
3. 解压后找到以下jar文件：
   - poi-5.2.3.jar
   - poi-ooxml-5.2.3.jar
   - poi-ooxml-lite-5.2.3.jar
   - xmlbeans-5.1.1.jar
   - commons-collections4-4.4.jar
   - commons-compress-1.26.0.jar

4. 将jar文件放到项目根目录

#### 选项B: Maven (推荐)
如果你使用Maven，这些依赖已在pom.xml中配置，直接运行：
```bash
mvn dependency:copy-dependencies
```

## 🚀 执行步骤

### 步骤1: 添加执行权限
```bash
chmod +x tools/run_java.sh
```

### 步骤2: 编译并运行

#### 如果有所有jar文件：
```bash
./tools/run_java.sh scripts/GenerateExcel.java 'poi-5.2.3.jar:poi-ooxml-5.2.3.jar:poi-ooxml-lite-5.2.3.jar:xmlbeans-5.1.1.jar:commons-collections4-4.4.jar:commons-compress-1.26.0.jar'
```

#### 如果使用Maven管理依赖：
```bash
./tools/run_java.sh scripts/GenerateExcel.java 'target/dependency/*'
```

## 📊 预期结果

执行成功后会生成文件：
- `data/excel.xlsx`

文件内容：

| 人员 | 部门 | 名称 |
|------|------|------|
| 张三 | 技术部 | 张三 |
| 李四 | 市场部 | 李四 |
| 王五 | 财务部 | 王五 |
| 赵六 | 人事部 | 赵六 |

## ⚠️ 常见问题

### 编译错误: 找不到包 org.apache.poi
**原因**: 缺少Apache POI依赖
**解决**: 按照上面的步骤下载并添加jar文件

### 执行错误: 找不到主类 GenerateExcel
**原因**: 编译失败或类路径问题
**解决**: 检查编译步骤是否成功，确认.class文件位置

## 🎉 完成
执行成功后，你可以在 `data/excel.xlsx` 找到生成的Excel文件！