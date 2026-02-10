#!/bin/bash

# DDD Migration Script for Domain Layer
# This script migrates Java files to the new DDD package structure

BASE_DIR="/Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/src/main/java/com/openclawlite"

# Define migration mappings
declare -A MIGRATIONS=(
    # agent/ -> openclaw/domain/agent/
    ["agent/AgentService.java"]="openclaw/domain/agent/AgentService.java"
    ["agent/AiClient.java"]="openclaw/domain/agent/AiClient.java"
    ["agent/CompactionResult.java"]="openclaw/domain/agent/CompactionResult.java"
    ["agent/CompactionService.java"]="openclaw/domain/agent/CompactionService.java"
    ["agent/ConsoleRunner.java"]="openclaw/domain/agent/ConsoleRunner.java"
    ["agent/ContextWindowGuard.java"]="openclaw/domain/agent/ContextWindowGuard.java"
    ["agent/ContextWindowStatus.java"]="openclaw/domain/agent/ContextWindowStatus.java"
    ["agent/MessageRecord.java"]="openclaw/domain/agent/MessageRecord.java"
    ["agent/ProblemSolver.java"]="openclaw/domain/agent/ProblemSolver.java"
    ["agent/PromptBuilder.java"]="openclaw/domain/agent/PromptBuilder.java"
    ["agent/SessionState.java"]="openclaw/domain/agent/SessionState.java"
    ["agent/SessionStore.java"]="openclaw/domain/agent/SessionStore.java"
    ["agent/Skill.java"]="openclaw/domain/agent/Skill.java"
    ["agent/SkillEligibilityChecker.java"]="openclaw/domain/agent/SkillEligibilityChecker.java"
    ["agent/SkillRef.java"]="openclaw/domain/agent/SkillRef.java"
    ["agent/SkillService.java"]="openclaw/domain/agent/SkillService.java"
    ["agent/SkillsLoadTest.java"]="openclaw/domain/agent/SkillsLoadTest.java"
    ["agent/SkillSnapshot.java"]="openclaw/domain/agent/SkillSnapshot.java"
    ["agent/SolutionKnowledgeBase.java"]="openclaw/domain/agent/SolutionKnowledgeBase.java"
    ["agent/SpringAiClient.java"]="openclaw/domain/agent/SpringAiClient.java"
    ["agent/ToolCall.java"]="openclaw/domain/agent/ToolCall.java"
    ["agent/ToolDispatcher.java"]="openclaw/domain/agent/ToolDispatcher.java"
    ["agent/ToolParser.java"]="openclaw/domain/agent/ToolParser.java"
    ["agent/ToolParseResult.java"]="openclaw/domain/agent/ToolParseResult.java"

    # agent/tools/ -> openclaw/domain/tool/
    ["agent/tools/CommandTool.java"]="openclaw/domain/tool/CommandTool.java"
    ["agent/tools/DisabledTool.java"]="openclaw/domain/tool/DisabledTool.java"
    ["agent/tools/ExcelGeneratorTool.java"]="openclaw/domain/tool/ExcelGeneratorTool.java"
    ["agent/tools/GlobalToolContext.java"]="openclaw/domain/tool/GlobalToolContext.java"
    ["agent/tools/ImageAnalysisTool.java"]="openclaw/domain/tool/ImageAnalysisTool.java"
    ["agent/tools/ListDirectoryTool.java"]="openclaw/domain/tool/ListDirectoryTool.java"
    ["agent/tools/ProblemDiagnosisTool.java"]="openclaw/domain/tool/ProblemDiagnosisTool.java"
    ["agent/tools/ReadFileTool.java"]="openclaw/domain/tool/ReadFileTool.java"
    ["agent/tools/SessionsHistoryTool.java"]="openclaw/domain/tool/SessionsHistoryTool.java"
    ["agent/tools/SessionsListTool.java"]="openclaw/domain/tool/SessionsListTool.java"
    ["agent/tools/SessionsSendTool.java"]="openclaw/domain/tool/SessionsSendTool.java"
    ["agent/tools/SessionsSpawnTool.java"]="openclaw/domain/tool/SessionsSpawnTool.java"
    ["agent/tools/SessionStatusTool.java"]="openclaw/domain/tool/SessionStatusTool.java"
    ["agent/tools/SkillsStatusTool.java"]="openclaw/domain/tool/SkillsStatusTool.java"
    ["agent/tools/SmartCommandTool.java"]="openclaw/domain/tool/SmartCommandTool.java"
    ["agent/tools/SmartExecTool.java"]="openclaw/domain/tool/SmartExecTool.java"
    ["agent/tools/ToolContext.java"]="openclaw/domain/tool/ToolContext.java"
    ["agent/tools/ToolHandler.java"]="openclaw/domain/tool/ToolHandler.java"
    ["agent/tools/ToolPluginDefinition.java"]="openclaw/domain/tool/ToolPluginDefinition.java"
    ["agent/tools/ToolPluginLoader.java"]="openclaw/domain/tool/ToolPluginLoader.java"
    ["agent/tools/ToolRegistry.java"]="openclaw/domain/tool/ToolRegistry.java"
    ["agent/tools/ToolResult.java"]="openclaw/domain/tool/ToolResult.java"
    ["agent/tools/WebFetchTool.java"]="openclaw/domain/tool/WebFetchTool.java"
    ["agent/tools/WebSearchTool.java"]="openclaw/domain/tool/WebSearchTool.java"
    ["agent/tools/WordGeneratorTool.java"]="openclaw/domain/tool/WordGeneratorTool.java"
    ["agent/tools/WriteFileTool.java"]="openclaw/domain/tool/WriteFileTool.java"

    # gateway/session/ -> openclaw/domain/session/
    ["gateway/session/SessionManager.java"]="openclaw/domain/session/SessionManager.java"

    # gateway/channel/core/ -> openclaw/domain/channel/core/
    ["gateway/channel/core/ChannelAuthAdapter.java"]="openclaw/domain/channel/core/ChannelAuthAdapter.java"
    ["gateway/channel/core/ChannelAuthResult.java"]="openclaw/domain/channel/core/ChannelAuthResult.java"
    ["gateway/channel/core/ChannelCapabilities.java"]="openclaw/domain/channel/core/ChannelCapabilities.java"
    ["gateway/channel/core/ChannelConfigAdapter.java"]="openclaw/domain/channel/core/ChannelConfigAdapter.java"
    ["gateway/channel/core/ChannelConfigSchema.java"]="openclaw/domain/channel/core/ChannelConfigSchema.java"
    ["gateway/channel/core/ChannelGatewayAdapter.java"]="openclaw/domain/channel/core/ChannelGatewayAdapter.java"
    ["gateway/channel/core/ChannelMessage.java"]="openclaw/domain/channel/core/ChannelMessage.java"
    ["gateway/channel/core/ChannelMessageResult.java"]="openclaw/domain/channel/core/ChannelMessageResult.java"
    ["gateway/channel/core/ChannelMessagingAdapter.java"]="openclaw/domain/channel/core/ChannelMessagingAdapter.java"
    ["gateway/channel/core/ChannelMeta.java"]="openclaw/domain/channel/core/ChannelMeta.java"
    ["gateway/channel/core/ChannelOutboundAdapter.java"]="openclaw/domain/channel/core/ChannelOutboundAdapter.java"
    ["gateway/channel/core/ChannelPlugin.java"]="openclaw/domain/channel/core/ChannelPlugin.java"
    ["gateway/channel/core/ChannelSendResult.java"]="openclaw/domain/channel/core/ChannelSendResult.java"
    ["gateway/channel/core/ChannelStatus.java"]="openclaw/domain/channel/core/ChannelStatus.java"
    ["gateway/channel/core/ChannelStatusAdapter.java"]="openclaw/domain/channel/core/ChannelStatusAdapter.java"

    # gateway/channel/impl/telegram/ -> openclaw/domain/channel/impl/telegram/
    ["gateway/channel/impl/telegram/TelegramAuthAdapter.java"]="openclaw/domain/channel/impl/telegram/TelegramAuthAdapter.java"
    ["gateway/channel/impl/telegram/TelegramChannel.java"]="openclaw/domain/channel/impl/telegram/TelegramChannel.java"
    ["gateway/channel/impl/telegram/TelegramConfigAdapter.java"]="openclaw/domain/channel/impl/telegram/TelegramConfigAdapter.java"
    ["gateway/channel/impl/telegram/TelegramGatewayAdapter.java"]="openclaw/domain/channel/impl/telegram/TelegramGatewayAdapter.java"
    ["gateway/channel/impl/telegram/TelegramMessageConverter.java"]="openclaw/domain/channel/impl/telegram/TelegramMessageConverter.java"
    ["gateway/channel/impl/telegram/TelegramMessagingAdapter.java"]="openclaw/domain/channel/impl/telegram/TelegramMessagingAdapter.java"
    ["gateway/channel/impl/telegram/TelegramOutboundAdapter.java"]="openclaw/domain/channel/impl/telegram/TelegramOutboundAdapter.java"

    # service/memory/ -> openclaw/domain/memory/
    ["service/memory/Memory.java"]="openclaw/domain/memory/Memory.java"
    ["service/memory/MemoryService.java"]="openclaw/domain/memory/MemoryService.java"
    ["service/memory/search/HybridSearch.java"]="openclaw/domain/memory/search/HybridSearch.java"
    ["service/memory/search/VectorSearch.java"]="openclaw/domain/memory/search/VectorSearch.java"
)

# Count total files
TOTAL=${#MIGRATIONS[@]}
CURRENT=0

# Process each file
for SOURCE in "${!MIGRATIONS[@]}"; do
    DEST="${MIGRATIONS[$SOURCE]}"
    CURRENT=$((CURRENT + 1))

    SOURCE_PATH="$BASE_DIR/$SOURCE"
    DEST_PATH="$BASE_DIR/$DEST"

    echo "[$CURRENT/$TOTAL] Migrating: $SOURCE"

    # Check if source exists
    if [ ! -f "$SOURCE_PATH" ]; then
        echo "  ⚠ Source file not found: $SOURCE_PATH"
        continue
    fi

    # Create destination directory
    DEST_DIR=$(dirname "$DEST_PATH")
    mkdir -p "$DEST_DIR"

    # Read file content
    CONTENT=$(cat "$SOURCE_PATH")

    # Update package declaration
    case "$SOURCE" in
        agent/*)
            # agent/ files -> openclaw.domain.agent
            CONTENT=$(echo "$CONTENT" | sed 's/package com\.openclawlite\.agent;/package com.openclawlite.openclaw.domain.agent;/')
            ;;
        agent/tools/*)
            # agent/tools/ files -> openclaw.domain.tool
            CONTENT=$(echo "$CONTENT" | sed 's/package com\.openclawlite\.agent\.tools;/package com.openclawlite.openclaw.domain.tool;/')
            ;;
        gateway/session/*)
            # gateway/session/ files -> openclaw.domain.session
            CONTENT=$(echo "$CONTENT" | sed 's/package com\.openclawlite\.gateway\.session;/package com.openclawlite.openclaw.domain.session;/')
            ;;
        gateway/channel/core/*)
            # gateway/channel/core/ files -> openclaw.domain.channel.core
            CONTENT=$(echo "$CONTENT" | sed 's/package com\.openclawlite\.gateway\.channel\.core;/package com.openclawlite.openclaw.domain.channel.core;/')
            ;;
        gateway/channel/impl/*)
            # gateway/channel/impl/ files -> openclaw.domain.channel.impl
            CONTENT=$(echo "$CONTENT" | sed 's/package com\.openclawlite\.gateway\.channel\.impl\.[^;]*;/package com.openclawlite.openclaw.domain.channel.impl.telegram;/')
            ;;
        service/memory/*)
            # service/memory/ files -> openclaw.domain.memory
            CONTENT=$(echo "$CONTENT" | sed 's/package com\.openclawlite\.service\.memory;/package com.openclawlite.openclaw.domain.memory;/')
            ;;
        service/memory/search/*)
            # service/memory/search/ files -> openclaw.domain.memory.search
            CONTENT=$(echo "$CONTENT" | sed 's/package com\.openclawlite\.service\.memory\.search;/package com.openclawlite.openclaw.domain.memory.search;/')
            ;;
    esac

    # Update imports
    # agent imports
    CONTENT=$(echo "$CONTENT" | sed 's/import com\.openclawlite\.agent\./import com.openclawlite.openclaw.domain.agent./g')

    # agent.tools imports -> openclaw.domain.tool
    CONTENT=$(echo "$CONTENT" | sed 's/import com\.openclawlite\.agent\.tools\./import com.openclawlite.openclaw.domain.tool./g')

    # gateway.session imports -> openclaw.domain.session
    CONTENT=$(echo "$CONTENT" | sed 's/import com\.openclawlite\.gateway\.session\./import com.openclawlite.openclaw.domain.session./g')

    # gateway.channel.core imports -> openclaw.domain.channel.core
    CONTENT=$(echo "$CONTENT" | sed 's/import com\.openclawlite\.gateway\.channel\.core\./import com.openclawlite.openclaw.domain.channel.core./g')

    # gateway.channel.impl imports -> openclaw.domain.channel.impl
    CONTENT=$(echo "$CONTENT" | sed 's/import com\.openclawlite\.gateway\.channel\.impl\./import com.openclawlite.openclaw.domain.channel.impl./g')

    # service.memory imports -> openclaw.domain.memory
    CONTENT=$(echo "$CONTENT" | sed 's/import com\.openclawlite\.service\.memory\./import com.openclawlite.openclaw.domain.memory./g')

    # Write to destination
    echo "$CONTENT" > "$DEST_PATH"

    echo "  ✓ Migrated to: $DEST"
done

echo ""
echo "Migration complete!"
echo "Processed $TOTAL files."
echo ""
echo "Next steps:"
echo "1. Review the migrated files"
echo "2. Update any remaining references in other parts of the codebase"
echo "3. Run tests to ensure everything works"
echo "4. Remove old files after verification"
