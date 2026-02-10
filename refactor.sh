#!/bin/bash

# OpenClaw Lite DDD 架构重构脚本

set -e  # 遇到错误立即退出

PROJECT_DIR="/Users/gaoshuanglong/IdeaProjects/java-openclaw-lite"
SRC_DIR="$PROJECT_DIR/src/main/java/com/openclawlite"

echo "=========================================="
echo "  DDD 架构重构"
echo "=========================================="
echo ""
echo "⚠️  警告：这将重构整个项目的包结构"
echo "⏱️  预计时间：1-1.5 小时"
echo ""
read -p "确认执行重构？(yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "操作已取消"
    exit 0
fi

cd "$PROJECT_DIR" || exit 1

echo ""
echo "=========================================="
echo "  开始重构..."
echo "=========================================="

# ========================================
# 步骤 1: 创建新包结构
# ========================================
echo ""
echo "📦 步骤 1/9: 创建新包结构..."

# Domain 层
mkdir -p src/main/java/com/openclawlite/openclaw/domain/agent
mkdir -p src/main/java/com/openclawlite/openclaw/domain/session
mkdir -p src/main/java/com/openclawlite/openclaw/domain/channel/core
mkdir -p src/main/java/com/openclawlite/openclaw/domain/channel/impl
mkdir -p src/main/java/com/openclawlite/domain/tool
mkdir -p src/main/java/com/openclawlite/domain/memory
mkdir -p src/main/java/com/openclawlite/domain/memory/search

# Application 层
mkdir -p src/main/java/com/openclawlite/openclaw/application/agent
mkdir -p src/main/java/com/openclawlite/openclaw/application/session
mkdir -p src/main/java/com/openclawlite/openclaw/application/channel
mkdir -p src/main/java/com/openclawlite/openclaw/application/tool
mkdir -p src/main/java/com/openclawlite/openclaw/application/memory

# Infrastructure 层
mkdir -p src/main/java/com/openclawlite/openclaw/infrastructure/persistence
mkdir -p src/main/java/com/openclawlite/openclaw/infrastructure/config
mkdir -p src/main/java/com/openclawlite/openclaw/infrastructure/messaging
mkdir -p src/main/java/com/openclawlite/openclaw/infrastructure/embedding

# Common 层
mkdir -p src/main/java/com/openclawlite/common/dto
mkdir -p src/main/java/com/openclawlite/common/enums
mkdir -p src/main/java/com/openclawlite/common/exception
mkdir -p src/main/java/com/openclawlite/common/util

# Adapter 层
mkdir -p src/main/java/com/openclawlite/adapter/rest
mkdir -p src/main/java/com/openclawlite/adapter/websocket
mkdir -p src/main/java/com/openclawlite/adapter/protocol
mkdir -p src/main/java/com/openclawlite/adapter/channel
mkdir -p src/main/java/com/openclawlite/adapter/plugin

echo "✓ 包结构已创建"

echo ""
echo "⏸️  暂停脚本。由于重构规模较大，"
echo "   请查看 REFACTOR_EXECUTE.md 了解完整计划。"
echo "   将分步执行重构以确保稳定性。"

EOF
chmod +x /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/refactor.sh
echo "✓ 重构脚本已创建"