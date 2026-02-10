package com.openclawlite.openclaw.domain.agent;

import com.openclawlite.openclaw.infrastructure.config.AppProperties;
import com.openclawlite.openclaw.domain.channel.core.ChannelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 核心服务
 *
 * <p>负责协调 AI Agent 的完整执行流程，包括：</p>
 * <ul>
 *   <li>提示词构建与系统指令管理</li>
 *   <li>工具调用解析与执行分发</li>
 *   <li>会话状态管理与持久化</li>
 *   <li>多会话支持与上下文维护</li>
 *   <li>上下文窗口监控与自动压缩</li>
 * </ul>
 *
 * <p>这是整个 Agent 系统的中央调度器，协调各个组件完成一次完整的对话回合。</p>
 *
 * @author OpenClaw Lite
 * @since 1.0
 */
@Component
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    /** 会话持久化存储 */
    private final SessionStore sessionStore;

    /** 技能管理服务 */
    private final SkillService skillService;

    /** 提示词构建器 */
    private final PromptBuilder promptBuilder;

    /** 工具调用解析器 */
    private final ToolParser toolParser;

    /** 工具执行分发器 */
    private final ToolDispatcher toolDispatcher;

    /** AI 模型客户端 */
    private final AiClient aiClient;

    /** 应用配置属性 */
    private final AppProperties properties;

    /** 上下文窗口监控器 */
    private final ContextWindowGuard contextWindowGuard;

    /** 会话压缩服务 */
    private final CompactionService compactionService;

    /** 多会话支持：会话 Key 到会话状态的映射 */
    private final Map<String, SessionState> sessionMap = new ConcurrentHashMap<>();

    /**
     * 构造 Agent 核心服务
     *
     * @param sessionStore 会话持久化存储
     * @param skillService 技能管理服务
     * @param promptBuilder 提示词构建器
     * @param toolParser 工具调用解析器
     * @param toolDispatcher 工具执行分发器
     * @param aiClient AI 模型客户端
     * @param properties 应用配置属性
     * @param contextWindowGuard 上下文窗口监控器
     * @param compactionService 会话压缩服务
     */
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

    /**
     * 执行一次 Agent 对话回合
     *
     * <p>这是单会话模式的核心方法，完成以下流程：</p>
     * <ol>
     *   <li>加载或创建会话状态</li>
     *   <li>确保技能快照最新</li>
     *   <li>检查并压缩过长的上下文</li>
     *   <li>在工具循环内与模型交互</li>
     *   <li>持久化会话状态</li>
     * </ol>
     *
     * @param userInput 用户输入的文本消息
     * @return Agent 的最终回复文本
     */
    public String runTurn(String userInput) {
        log.info("开始执行 Agent 对话回合");
        log.debug("用户输入: {}", userInput);

        // 加载会话状态
        SessionState session = sessionStore.load();
        log.debug("已加载会话: {}", session.getSessionId());

        // 确保技能快照最新
        SkillSnapshot snapshot = skillService.ensureSnapshot(session);
        log.debug("技能快照版本: {}", snapshot.getVersion());

        // 构建系统提示词
        String systemPrompt = promptBuilder.buildSystemPrompt(snapshot);
        log.debug("系统提示词长度: {} 字符", systemPrompt.length());

        // 添加用户消息到会话
        session.addMessage("user", userInput);
        String finalText = null;
        String systemNotice = null;

        // 在进入工具循环前检查上下文长度并按需压缩
        log.debug("检查上下文窗口状态...");
        ContextWindowStatus status = contextWindowGuard.evaluate(session.getMessages());
        if (status.isShouldCompact()) {
            log.warn("上下文窗口超限，触发自动压缩");
            CompactionResult result = compactionService.compact(session);
            if (!result.isOk()) {
                log.error("会话压缩失败: {}", result.getMessage());
                finalText = "对话过长且压缩失败：" + result.getMessage();
                session.addMessage("assistant", finalText);
                session.setUpdatedAt(System.currentTimeMillis());
                sessionStore.save(session);
                return finalText;
            }
            if (result.isCompacted()) {
                log.info("会话已成功压缩");
                systemNotice = "（系统提示：对话过长已自动压缩）\n";
            }
        } else if (status.isShouldWarn()) {
            log.warn("上下文窗口接近上限");
            systemNotice = "（系统提示：对话接近上限，可能触发自动压缩）\n";
        }

        // 允许模型在限定步数内调用工具
        int maxSteps = properties.getMaxToolSteps();
        log.debug("开始工具循环，最大步数: {}", maxSteps);

        for (int step = 0; step < maxSteps; step++) {
            log.debug("工具循环步骤 {}/{}", step + 1, maxSteps);
            // 构建消息列表并调用模型
            List<Message> messages = promptBuilder.buildMessages(systemPrompt, session.getMessages());
            log.debug("发送给模型的消息数: {},", messages.size());

            String modelOutput = aiClient.chat(messages);
            log.info("模型原始输出: {} ", modelOutput);
            log.debug("模型原始输出长度: {} 字符", modelOutput.length());

            // 解析模型输出，判断是工具调用还是最终回答
            ToolParseResult parsed = toolParser.parse(modelOutput);
            ToolCall toolCall = parsed.getToolCall();

            if (toolCall == null) {
                // 模型输出最终回答，保存并退出
                log.info("模型返回最终回答，工具循环结束");
                finalText = parsed.getFinalText();
                break;
            }

            // 工具调用路径：记录模型输出、执行工具并追加结果
            log.info("模型调用工具: {}", toolCall.getTool());
            session.addMessage("assistant", modelOutput);

            String toolResult = toolDispatcher.execute(toolCall);
            log.debug("工具执行结果长度: {} 字符", toolResult.length());

            session.addMessage("tool", toolResult);
        }

        // 超出工具步数上限时的兜底回复
        if (finalText == null) {
            log.warn("工具循环达到最大步数限制，强制结束");
            finalText = "工具循环次数已达上限，请尝试用更少步骤重新提问。";
        }

        // 添加系统通知（如有）
        if (systemNotice != null) {
            finalText = systemNotice + finalText;
        }

        // 记录最终回答到会话
        session.addMessage("assistant", finalText);

        // 将更新后的会话持久化到磁盘
        log.debug("持久化会话状态");
        session.setUpdatedAt(System.currentTimeMillis());
        sessionStore.save(session);

        log.info("Agent 对话回合完成，回复长度: {} 字符", finalText.length());
        return finalText;
    }

    /**
     * 处理来自渠道的消息（支持多会话）
     *
     * <p>这是多会话模式的核心方法，为每个渠道会话维护独立的上下文状态。</p>
     *
     * @param sessionKey 会话唯一标识符
     * @param message 渠道消息对象
     * @return Mono<String> Agent 的响应（异步）
     */
    public Mono<String> processMessage(String sessionKey, ChannelMessage message) {
        return Mono.fromCallable(() -> {
            try {
                log.info("处理渠道消息 - 会话: {}", sessionKey);

                // 获取或创建会话状态
                SessionState session = sessionMap.computeIfAbsent(sessionKey, key -> {
                    log.info("创建新会话: {}", key);
                    SessionState newSession = new SessionState();
                    newSession.setSessionId(key);
                    newSession.setCreatedAt(System.currentTimeMillis());
                    return newSession;
                });

                // 提取消息文本
                String userText = message.getText();
                if (userText == null || userText.trim().isEmpty()) {
                    log.warn("会话 {} 的消息为空", sessionKey);
                    return "请发送文本消息。";
                }

                log.debug("处理会话 {} 的消息: {}", sessionKey, userText);

                // 执行 Agent 对话回合
                String response = runTurnWithSession(userText, session);

                log.debug("会话 {} 的 Agent 响应: {}", sessionKey, response);
                return response;

            } catch (Exception e) {
                log.error("处理会话 {} 的消息时发生错误", sessionKey, e);
                throw new RuntimeException("消息处理失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 使用指定会话状态执行 Agent 对话回合
     *
     * <p>与 runTurn 方法类似，但使用传入的会话状态而非从存储加载。</p>
     *
     * @param userInput 用户输入文本
     * @param session 会话状态对象
     * @return Agent 的响应文本
     */
    private String runTurnWithSession(String userInput, SessionState session) {
        log.debug("执行会话 {} 的 Agent 回合", session.getSessionId());

        // 确保技能快照最新
        SkillSnapshot snapshot = skillService.ensureSnapshot(session);
        String systemPrompt = promptBuilder.buildSystemPrompt(snapshot);

        // 添加用户消息
        session.addMessage("user", userInput);
        String finalText = null;
        String systemNotice = null;

        // 检查上下文窗口并按需压缩
        ContextWindowStatus status = contextWindowGuard.evaluate(session.getMessages());
        if (status.isShouldCompact()) {
            log.warn("会话 {} 上下文超限，触发压缩", session.getSessionId());
            CompactionResult result = compactionService.compact(session);
            if (!result.isOk()) {
                log.error("会话压缩失败: {}", result.getMessage());
                finalText = "对话过长且压缩失败：" + result.getMessage();
                session.addMessage("assistant", finalText);
                session.setUpdatedAt(System.currentTimeMillis());
                return finalText;
            }
            if (result.isCompacted()) {
                systemNotice = "（系统提示：对话过长已自动压缩）\n";
            }
        } else if (status.isShouldWarn()) {
            systemNotice = "（系统提示：对话接近上限，可能触发自动压缩）\n";
        }

        // 工具循环
        for (int step = 0; step < properties.getMaxToolSteps(); step++) {
            List<Message> messages = promptBuilder.buildMessages(systemPrompt, session.getMessages());
            String modelOutput = aiClient.chat(messages);
            ToolParseResult parsed = toolParser.parse(modelOutput);
            ToolCall toolCall = parsed.getToolCall();
            if (toolCall == null) {
                finalText = parsed.getFinalText();
                break;
            }
            log.info("模型调用工具: {}", toolCall.getTool());
            session.addMessage("assistant", modelOutput);
            String toolResult = toolDispatcher.execute(toolCall);
            session.addMessage("tool", toolResult);
        }

        if (finalText == null) {
            finalText = "工具循环次数已达上限，请尝试用更少步骤重新提问。";
        }

        if (systemNotice != null) {
            finalText = systemNotice + finalText;
        }
        session.addMessage("assistant", finalText);
        session.setUpdatedAt(System.currentTimeMillis());

        return finalText;
    }

    /**
     * 获取当前活跃会话数量
     *
     * @return 会话总数
     */
    public int getSessionCount() {
        return sessionMap.size();
    }

    /**
     * 清除指定会话
     *
     * @param sessionKey 要清除的会话标识符
     */
    public void clearSession(String sessionKey) {
        sessionMap.remove(sessionKey);
        log.info("已清除会话: {}", sessionKey);
    }
}
