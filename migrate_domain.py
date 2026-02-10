#!/usr/bin/env python3
"""
DDD Migration Script for Domain Layer
Migrates Java files to new DDD package structure with package and import updates
"""

import os
import re
from pathlib import Path

BASE_DIR = Path("/Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/src/main/java/com/openclawlite")

# Migration mappings: source -> destination
MIGRATIONS = {
    # agent/ -> openclaw/domain/agent/
    "agent/AgentService.java": "openclaw/domain/agent/AgentService.java",
    "agent/AiClient.java": "openclaw/domain/agent/AiClient.java",
    "agent/CompactionResult.java": "openclaw/domain/agent/CompactionResult.java",
    "agent/CompactionService.java": "openclaw/domain/agent/CompactionService.java",
    "agent/ConsoleRunner.java": "openclaw/domain/agent/ConsoleRunner.java",
    "agent/ContextWindowGuard.java": "openclaw/domain/agent/ContextWindowGuard.java",
    "agent/ContextWindowStatus.java": "openclaw/domain/agent/ContextWindowStatus.java",
    "agent/MessageRecord.java": "openclaw/domain/agent/MessageRecord.java",
    "agent/ProblemSolver.java": "openclaw/domain/agent/ProblemSolver.java",
    "agent/PromptBuilder.java": "openclaw/domain/agent/PromptBuilder.java",
    "agent/SessionState.java": "openclaw/domain/agent/SessionState.java",
    "agent/SessionStore.java": "openclaw/domain/agent/SessionStore.java",
    "agent/Skill.java": "openclaw/domain/agent/Skill.java",
    "agent/SkillEligibilityChecker.java": "openclaw/domain/agent/SkillEligibilityChecker.java",
    "agent/SkillRef.java": "openclaw/domain/agent/SkillRef.java",
    "agent/SkillService.java": "openclaw/domain/agent/SkillService.java",
    "agent/SkillsLoadTest.java": "openclaw/domain/agent/SkillsLoadTest.java",
    "agent/SkillSnapshot.java": "openclaw/domain/agent/SkillSnapshot.java",
    "agent/SolutionKnowledgeBase.java": "openclaw/domain/agent/SolutionKnowledgeBase.java",
    "agent/SpringAiClient.java": "openclaw/domain/agent/SpringAiClient.java",
    "agent/ToolCall.java": "openclaw/domain/agent/ToolCall.java",
    "agent/ToolDispatcher.java": "openclaw/domain/agent/ToolDispatcher.java",
    "agent/ToolParser.java": "openclaw/domain/agent/ToolParser.java",
    "agent/ToolParseResult.java": "openclaw/domain/agent/ToolParseResult.java",

    # agent/tools/ -> openclaw/domain/tool/
    "agent/tools/CommandTool.java": "openclaw/domain/tool/CommandTool.java",
    "agent/tools/DisabledTool.java": "openclaw/domain/tool/DisabledTool.java",
    "agent/tools/ExcelGeneratorTool.java": "openclaw/domain/tool/ExcelGeneratorTool.java",
    "agent/tools/GlobalToolContext.java": "openclaw/domain/tool/GlobalToolContext.java",
    "agent/tools/ImageAnalysisTool.java": "openclaw/domain/tool/ImageAnalysisTool.java",
    "agent/tools/ListDirectoryTool.java": "openclaw/domain/tool/ListDirectoryTool.java",
    "agent/tools/ProblemDiagnosisTool.java": "openclaw/domain/tool/ProblemDiagnosisTool.java",
    "agent/tools/ReadFileTool.java": "openclaw/domain/tool/ReadFileTool.java",
    "agent/tools/SessionsHistoryTool.java": "openclaw/domain/tool/SessionsHistoryTool.java",
    "agent/tools/SessionsListTool.java": "openclaw/domain/tool/SessionsListTool.java",
    "agent/tools/SessionsSendTool.java": "openclaw/domain/tool/SessionsSendTool.java",
    "agent/tools/SessionsSpawnTool.java": "openclaw/domain/tool/SessionsSpawnTool.java",
    "agent/tools/SessionStatusTool.java": "openclaw/domain/tool/SessionStatusTool.java",
    "agent/tools/SkillsStatusTool.java": "openclaw/domain/tool/SkillsStatusTool.java",
    "agent/tools/SmartCommandTool.java": "openclaw/domain/tool/SmartCommandTool.java",
    "agent/tools/SmartExecTool.java": "openclaw/domain/tool/SmartExecTool.java",
    "agent/tools/ToolContext.java": "openclaw/domain/tool/ToolContext.java",
    "agent/tools/ToolHandler.java": "openclaw/domain/tool/ToolHandler.java",
    "agent/tools/ToolPluginDefinition.java": "openclaw/domain/tool/ToolPluginDefinition.java",
    "agent/tools/ToolPluginLoader.java": "openclaw/domain/tool/ToolPluginLoader.java",
    "agent/tools/ToolRegistry.java": "openclaw/domain/tool/ToolRegistry.java",
    "agent/tools/ToolResult.java": "openclaw/domain/tool/ToolResult.java",
    "agent/tools/WebFetchTool.java": "openclaw/domain/tool/WebFetchTool.java",
    "agent/tools/WebSearchTool.java": "openclaw/domain/tool/WebSearchTool.java",
    "agent/tools/WordGeneratorTool.java": "openclaw/domain/tool/WordGeneratorTool.java",
    "agent/tools/WriteFileTool.java": "openclaw/domain/tool/WriteFileTool.java",

    # gateway/session/ -> openclaw/domain/session/
    "gateway/session/SessionManager.java": "openclaw/domain/session/SessionManager.java",

    # gateway/channel/core/ -> openclaw/domain/channel/core/
    "gateway/channel/core/ChannelAuthAdapter.java": "openclaw/domain/channel/core/ChannelAuthAdapter.java",
    "gateway/channel/core/ChannelAuthResult.java": "openclaw/domain/channel/core/ChannelAuthResult.java",
    "gateway/channel/core/ChannelCapabilities.java": "openclaw/domain/channel/core/ChannelCapabilities.java",
    "gateway/channel/core/ChannelConfigAdapter.java": "openclaw/domain/channel/core/ChannelConfigAdapter.java",
    "gateway/channel/core/ChannelConfigSchema.java": "openclaw/domain/channel/core/ChannelConfigSchema.java",
    "gateway/channel/core/ChannelGatewayAdapter.java": "openclaw/domain/channel/core/ChannelGatewayAdapter.java",
    "gateway/channel/core/ChannelMessage.java": "openclaw/domain/channel/core/ChannelMessage.java",
    "gateway/channel/core/ChannelMessageResult.java": "openclaw/domain/channel/core/ChannelMessageResult.java",
    "gateway/channel/core/ChannelMessagingAdapter.java": "openclaw/domain/channel/core/ChannelMessagingAdapter.java",
    "gateway/channel/core/ChannelMeta.java": "openclaw/domain/channel/core/ChannelMeta.java",
    "gateway/channel/core/ChannelOutboundAdapter.java": "openclaw/domain/channel/core/ChannelOutboundAdapter.java",
    "gateway/channel/core/ChannelPlugin.java": "openclaw/domain/channel/core/ChannelPlugin.java",
    "gateway/channel/core/ChannelSendResult.java": "openclaw/domain/channel/core/ChannelSendResult.java",
    "gateway/channel/core/ChannelStatus.java": "openclaw/domain/channel/core/ChannelStatus.java",
    "gateway/channel/core/ChannelStatusAdapter.java": "openclaw/domain/channel/core/ChannelStatusAdapter.java",

    # gateway/channel/impl/telegram/ -> openclaw/domain/channel/impl/telegram/
    "gateway/channel/impl/telegram/TelegramAuthAdapter.java": "openclaw/domain/channel/impl/telegram/TelegramAuthAdapter.java",
    "gateway/channel/impl/telegram/TelegramChannel.java": "openclaw/domain/channel/impl/telegram/TelegramChannel.java",
    "gateway/channel/impl/telegram/TelegramConfigAdapter.java": "openclaw/domain/channel/impl/telegram/TelegramConfigAdapter.java",
    "gateway/channel/impl/telegram/TelegramGatewayAdapter.java": "openclaw/domain/channel/impl/telegram/TelegramGatewayAdapter.java",
    "gateway/channel/impl/telegram/TelegramMessageConverter.java": "openclaw/domain/channel/impl/telegram/TelegramMessageConverter.java",
    "gateway/channel/impl/telegram/TelegramMessagingAdapter.java": "openclaw/domain/channel/impl/telegram/TelegramMessagingAdapter.java",
    "gateway/channel/impl/telegram/TelegramOutboundAdapter.java": "openclaw/domain/channel/impl/telegram/TelegramOutboundAdapter.java",

    # service/memory/ -> openclaw/domain/memory/
    "service/memory/Memory.java": "openclaw/domain/memory/Memory.java",
    "service/memory/MemoryService.java": "openclaw/domain/memory/MemoryService.java",
    "service/memory/search/HybridSearch.java": "openclaw/domain/memory/search/HybridSearch.java",
    "service/memory/search/VectorSearch.java": "openclaw/domain/memory/search/VectorSearch.java",
}

def update_package_and_imports(content, source_path):
    """Update package declaration and imports based on source path"""

    # Update package declarations
    if source_path.startswith("agent/"):
        # agent/ files -> openclaw.domain.agent
        content = re.sub(
            r'package com\.openclawlite\.agent;',
            'package com.openclawlite.openclaw.domain.agent;',
            content
        )
    elif source_path.startswith("agent/tools/"):
        # agent/tools/ files -> openclaw.domain.tool
        content = re.sub(
            r'package com\.openclawlite\.agent\.tools;',
            'package com.openclawlite.openclaw.domain.tool;',
            content
        )
    elif source_path.startswith("gateway/session/"):
        # gateway/session/ files -> openclaw.domain.session
        content = re.sub(
            r'package com\.openclawlite\.gateway\.session;',
            'package com.openclawlite.openclaw.domain.session;',
            content
        )
    elif source_path.startswith("gateway/channel/core/"):
        # gateway/channel/core/ files -> openclaw.domain.channel.core
        content = re.sub(
            r'package com\.openclawlite\.gateway\.channel\.core;',
            'package com.openclawlite.openclaw.domain.channel.core;',
            content
        )
    elif source_path.startswith("gateway/channel/impl/"):
        # gateway/channel/impl/ files -> openclaw.domain.channel.impl.X
        impl_match = re.match(r'gateway/channel/impl/([^/]+)', source_path)
        if impl_match:
            impl_type = impl_match.group(1)
            content = re.sub(
                r'package com\.openclawlite\.gateway\.channel\.impl\.' + impl_type + r';',
                f'package com.openclawlite.openclaw.domain.channel.impl.{impl_type};',
                content
            )
    elif source_path.startswith("service/memory/"):
        if source_path.startswith("service/memory/search/"):
            # service/memory/search/ files -> openclaw.domain.memory.search
            content = re.sub(
                r'package com\.openclawlite\.service\.memory\.search;',
                'package com.openclawlite.openclaw.domain.memory.search;',
                content
            )
        else:
            # service/memory/ files -> openclaw.domain.memory
            content = re.sub(
                r'package com\.openclawlite\.service\.memory;',
                'package com.openclawlite.openclaw.domain.memory;',
                content
            )

    # Update imports
    # agent imports -> openclaw.domain.agent
    content = re.sub(
        r'import com\.openclawlite\.agent\.',
        'import com.openclawlite.openclaw.domain.agent.',
        content
    )

    # agent.tools imports -> openclaw.domain.tool
    content = re.sub(
        r'import com\.openclawlite\.agent\.tools\.',
        'import com.openclawlite.openclaw.domain.tool.',
        content
    )

    # gateway.session imports -> openclaw.domain.session
    content = re.sub(
        r'import com\.openclawlite\.gateway\.session\.',
        'import com.openclawlite.openclaw.domain.session.',
        content
    )

    # gateway.channel.core imports -> openclaw.domain.channel.core
    content = re.sub(
        r'import com\.openclawlite\.gateway\.channel\.core\.',
        'import com.openclawlite.openclaw.domain.channel.core.',
        content
    )

    # gateway.channel.impl imports -> openclaw.domain.channel.impl
    content = re.sub(
        r'import com\.openclawlite\.gateway\.channel\.impl\.',
        'import com.openclawlite.openclaw.domain.channel.impl.',
        content
    )

    # service.memory imports -> openclaw.domain.memory
    content = re.sub(
        r'import com\.openclawlite\.service\.memory\.',
        'import com.openclawlite.openclaw.domain.memory.',
        content
    )

    return content

def migrate_file(source, destination):
    """Migrate a single file with package and import updates"""
    source_path = BASE_DIR / source
    dest_path = BASE_DIR / destination

    if not source_path.exists():
        print(f"  ⚠️  Source file not found: {source_path}")
        return False

    # Read source file
    try:
        with open(source_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"  ❌ Error reading file: {e}")
        return False

    # Update package and imports
    content = update_package_and_imports(content, source)

    # Create destination directory
    dest_path.parent.mkdir(parents=True, exist_ok=True)

    # Write to destination
    try:
        with open(dest_path, 'w', encoding='utf-8') as f:
            f.write(content)
    except Exception as e:
        print(f"  ❌ Error writing file: {e}")
        return False

    return True

def main():
    """Main migration function"""
    total = len(MIGRATIONS)
    current = 0
    success_count = 0
    skip_count = 0

    print("=" * 70)
    print("DDD Domain Layer Migration")
    print("=" * 70)
    print()

    for source, destination in sorted(MIGRATIONS.items()):
        current += 1
        print(f"[{current}/{total}] Migrating: {source}")
        print(f"         → {destination}")

        if migrate_file(source, destination):
            print(f"         ✅ Success")
            success_count += 1
        else:
            print(f"         ⚠️  Skipped")
            skip_count += 1
        print()

    print("=" * 70)
    print("Migration Summary")
    print("=" * 70)
    print(f"Total files:  {total}")
    print(f"Successful:   {success_count}")
    print(f"Skipped:      {skip_count}")
    print()
    print("Next steps:")
    print("1. Review the migrated files")
    print("2. Update any remaining references in other parts of the codebase")
    print("3. Run tests to ensure everything works")
    print("4. Remove old files after verification")
    print()

if __name__ == "__main__":
    main()
