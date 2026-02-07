# 工具使用说明

## 🛠️ Java脚本执行工具

### 📁 文件位置
`tools/run_java.sh`

### 🚀 使用方法

#### 1. 添加执行权限（首次使用）
```bash
chmod +x tools/run_java.sh
```

#### 2. 基本用法
```bash
# 不需要外部依赖的脚本
./tools/run_java.sh scripts/YourScript.java

# 需要外部依赖的脚本
./tools/run_java.sh scripts/GenerateExcel.java 'poi-ooxml-5.2.3.jar'
```

### 📋 功能特性
- ✅ 自动编译Java脚本
- ✅ 检查文件是否存在
- ✅ 支持外部类路径依赖
- ✅ 错误处理和友好的提示信息
- ✅ 自动提取类名

### 🎯 使用示例

#### 示例1: 执行简单Java脚本
```bash
./tools/run_java.sh scripts/HelloWorld.java
```

#### 示例2: 执行带依赖的Excel生成脚本
```bash
./tools/run_java.sh scripts/GenerateExcel.java 'poi-ooxml-5.2.3.jar'
```

### 🔧 工作流程
```
1. 检查脚本路径 → 2. 编译Java文件 → 3. 执行编译后的程序
```

### ⚠️ 注意事项
- 确保已安装Java开发环境 (JDK)
- 如果脚本需要外部依赖，请提供正确的类路径参数
- 脚本编译后会生成 .class 文件在同一目录下
- 首次使用需要添加执行权限