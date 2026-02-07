#!/bin/bash

# Java脚本执行工具
# 用法: ./tools/run_java.sh <脚本路径> [类路径参数]

SCRIPT_PATH="$1"
CLASSPATH_ARGS="$2"

# 检查脚本路径是否提供
if [ -z "$SCRIPT_PATH" ]; then
    echo "错误: 请提供Java脚本路径"
    echo "用法: ./tools/run_java.sh <脚本路径> [类路径参数]"
    echo "示例: ./tools/run_java.sh scripts/GenerateExcel.java 'poi-ooxml-5.2.3.jar'"
    exit 1
fi

# 检查文件是否存在
if [ ! -f "$SCRIPT_PATH" ]; then
    echo "错误: 文件不存在: $SCRIPT_PATH"
    exit 1
fi

# 获取脚本目录和文件名
SCRIPT_DIR=$(dirname "$SCRIPT_PATH")
SCRIPT_NAME=$(basename "$SCRIPT_PATH" .java)
CLASS_NAME="$(basename "$SCRIPT_NAME")"

echo "📝 编译Java脚本: $SCRIPT_PATH"

# 编译Java文件
if [ -n "$CLASSPATH_ARGS" ]; then
    javac -cp "$CLASSPATH_ARGS" "$SCRIPT_PATH"
else
    javac "$SCRIPT_PATH"
fi

# 检查编译是否成功
if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请检查错误信息"
    exit 1
fi

echo "✅ 编译成功"
echo "🚀 执行Java程序: $CLASS_NAME"

# 执行Java程序
if [ -n "$CLASSPATH_ARGS" ]; then
    java -cp "$CLASSPATH_ARGS:$SCRIPT_DIR" "$CLASS_NAME"
else
    java -cp "$SCRIPT_DIR" "$CLASS_NAME"
fi
