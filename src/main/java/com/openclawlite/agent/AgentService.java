package com.openclawlite.agent;

import com.openclawlite.config.AppProperties;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
// 协调提示词构建、工具执行与会话持久化的核心服务。
public class AgentService {
    private final SessionStore sessionStore;
    private final SkillService skillService;
    private final PromptBuilder promptBuilder;
    private final ToolParser toolParser;
    private final ToolDispatcher toolDispatcher;
    private final AiClient aiClient;
    private final AppProperties properties;
    private final ContextWindowGuard contextWindowGuard;
    private final CompactionService compactionService;

    // 注入运行单次回合所需的核心依赖。
    public AgentService(
            SessionStore sessionStore,
            SkillService skillService,
            PromptBuilder promptBuilder,
            ToolParser toolParser,
            ToolDispatcher toolDispatcher,
            AiClient aiClient,
            AppProperties properties,
            ContextWindowGuard contextWindowGuard,
            CompactionService compactionService) {
        this.sessionStore = sessionStore;
        this.skillService = skillService;
        this.promptBuilder = promptBuilder;
        this.toolParser = toolParser;
        this.toolDispatcher = toolDispatcher;
        this.aiClient = aiClient;
        this.properties = properties;
        this.contextWindowGuard = contextWindowGuard;
        this.compactionService = compactionService;
    }

    // 执行一次用户回合，并在有限工具循环内完成响应。
    public String runTurn(String userInput) {
        SessionState session = sessionStore.load();
        SkillSnapshot snapshot = skillService.ensureSnapshot(session);
        String systemPrompt = promptBuilder.buildSystemPrompt(snapshot);

        session.addMessage("user", userInput);
        String finalText = null;
        String systemNotice = null;

        // 在进入工具循环前检查上下文长度并按需压缩。
        ContextWindowStatus status = contextWindowGuard.evaluate(session.getMessages());
        if (status.isShouldCompact()) {
            CompactionResult result = compactionService.compact(session);
            if (!result.isOk()) {
                finalText = "对话过长且压缩失败：" + result.getMessage();
                session.addMessage("assistant", finalText);
                session.setUpdatedAt(System.currentTimeMillis());
                sessionStore.save(session);
                return finalText;
            }
            if (result.isCompacted()) {
                systemNotice = "（系统提示：对话过长已自动压缩）\n";
            }
        } else if (status.isShouldWarn()) {
            systemNotice = "（系统提示：对话接近上限，可能触发自动压缩）\n";
        }

        // 允许模型在限定步数内调用工具。
        for (int step = 0; step < properties.getMaxToolSteps(); step++) {
            List<Message> messages = promptBuilder.buildMessages(systemPrompt, session.getMessages());
            String modelOutput = aiClient.chat(messages);
            ToolParseResult parsed = toolParser.parse(modelOutput);
            ToolCall toolCall = parsed.getToolCall();
            if (toolCall == null) {
                // 模型输出最终回答后保存并退出。
                finalText = parsed.getFinalText();
                break;
            }
            // 工具调用路径：记录模型输出、执行工具并追加结果。
            session.addMessage("assistant", modelOutput);
            String toolResult = toolDispatcher.execute(toolCall);
            session.addMessage("tool", toolResult);
        }

        // 超出工具步数上限时的兜底回复。
        if (finalText == null) {
            finalText = "工具循环次数已达上限，请尝试用更少步骤重新提问。";
        }

        if (systemNotice != null) {
            finalText = systemNotice + finalText;
        }
        session.addMessage("assistant", finalText);

        // 将更新后的会话持久化到磁盘。
        session.setUpdatedAt(System.currentTimeMillis());
        sessionStore.save(session);
        return finalText;
    }
}
