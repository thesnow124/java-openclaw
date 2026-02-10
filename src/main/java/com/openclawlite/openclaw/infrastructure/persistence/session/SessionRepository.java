package com.openclawlite.openclaw.infrastructure.persistence.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclawlite.common.enums.ChannelCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 会话持久化仓库
 * <p>
 * 负责会话（Session）数据的持久化操作。
 * 使用 SQLite 数据库存储会话和消息历史。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>会话的创建、查询、更新和删除</li>
 *   <li>会话消息的存储和检索</li>
 *   <li>会话活动时间跟踪</li>
 *   <li>JSON 格式的元数据和上下文存储</li>
 * </ul>
 *
 * <p>数据库表结构：</p>
 * <pre>
 * CREATE TABLE sessions (
 *   session_key VARCHAR(255) PRIMARY KEY,
 *   channel_id VARCHAR(100) NOT NULL,
 *   account_id VARCHAR(100),
 *   chat_id VARCHAR(255),
 *   chat_type VARCHAR(20),
 *   created_at BIGINT NOT NULL,
 *   updated_at BIGINT NOT NULL,
 *   last_activity_at BIGINT NOT NULL,
 *   metadata TEXT,
 *   context TEXT
 * );
 *
 * CREATE TABLE session_messages (
 *   id INTEGER PRIMARY KEY AUTOINCREMENT,
 *   session_key VARCHAR(255) NOT NULL,
 *   role VARCHAR(20) NOT NULL,
 *   content TEXT,
 *   tool_call TEXT,
 *   timestamp BIGINT NOT NULL,
 *   FOREIGN KEY (session_key) REFERENCES sessions(session_key)
 * );
 * </pre>
 */
@Repository
public class SessionRepository {

    private static final Logger log = LoggerFactory.getLogger(SessionRepository.class);

    /** JDBC 模板，用于执行数据库操作 */
    private final JdbcTemplate jdbcTemplate;

    /** JSON 序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     *
     * @param jdbcTemplate JDBC 模板
     * @param objectMapper JSON 序列化工具
     */
    public SessionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存或更新会话
     * <p>
     * 使用 INSERT OR REPLACE 语义，如果会话不存在则创建，如果存在则更新。
     * 元数据和上下文对象会被序列化为 JSON 存储。
     * </p>
     *
     * @param session 会话对象
     * @return 保存后的会话对象
     */
    public Session save(Session session) {
        log.debug("保存会话: sessionKey={}, channelId={}", session.sessionKey(), session.channelId());

        String sql = """
            INSERT OR REPLACE INTO sessions
            (session_key, channel_id, account_id, chat_id, chat_type,
             created_at, updated_at, last_activity_at, metadata, context)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        long now = Instant.now().getEpochSecond();
        long createdAt = session.createdAt() != null
            ? session.createdAt().toEpochSecond()
            : now;

        jdbcTemplate.update(sql,
            session.sessionKey(),
            session.channelId(),
            session.accountId(),
            session.chatId(),
            session.chatType().name(),
            createdAt,
            now,
            now,
            toJson(session.metadata()),
            toJson(session.context())
        );

        log.debug("会话保存成功: sessionKey={}", session.sessionKey());
        return session;
    }

    /**
     * 根据会话键查询会话
     *
     * @param sessionKey 会话键
     * @return 会话的 Optional 对象，如果不存在则返回 Optional.empty()
     */
    public Optional<Session> findByKey(String sessionKey) {
        log.debug("查询会话: sessionKey={}", sessionKey);
        String sql = "SELECT * FROM sessions WHERE session_key = ?";

        List<Session> results = jdbcTemplate.query(sql, new SessionRowMapper(), sessionKey);
        if (results.isEmpty()) {
            log.debug("会话不存在: sessionKey={}", sessionKey);
        }
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 查询指定渠道的所有会话
     * <p>
     * 按最后活动时间倒序排列。
     * </p>
     *
     * @param channelId 渠道 ID
     * @return 会话列表
     */
    public List<Session> findByChannel(String channelId) {
        log.debug("查询渠道的所有会话: channelId={}", channelId);
        String sql = "SELECT * FROM sessions WHERE channel_id = ? ORDER BY last_activity_at DESC";
        return jdbcTemplate.query(sql, new SessionRowMapper(), channelId);
    }

    /**
     * 查询指定渠道和账户的所有会话
     * <p>
     * 按最后活动时间倒序排列。
     * </p>
     *
     * @param channelId 渠道 ID
     * @param accountId 账户 ID
     * @return 会话列表
     */
    public List<Session> findByAccount(String channelId, String accountId) {
        log.debug("查询账户的会话: channelId={}, accountId={}", channelId, accountId);
        String sql = "SELECT * FROM sessions WHERE channel_id = ? AND account_id = ? ORDER BY last_activity_at DESC";
        return jdbcTemplate.query(sql, new SessionRowMapper(), channelId, accountId);
    }

    /**
     * 更新会话最后活动时间
     * <p>
     * 更新 last_activity_at 和 updated_at 字段为当前时间。
     * </p>
     *
     * @param sessionKey 会话键
     */
    public void updateActivity(String sessionKey) {
        log.debug("更新会话活动时间: sessionKey={}", sessionKey);
        String sql = "UPDATE sessions SET last_activity_at = ?, updated_at = ? WHERE session_key = ?";
        long now = Instant.now().getEpochSecond();
        jdbcTemplate.update(sql, now, now, sessionKey);
    }

    /**
     * 删除会话
     *
     * @param sessionKey 会话键
     */
    public void delete(String sessionKey) {
        log.info("删除会话: sessionKey={}", sessionKey);
        String sql = "DELETE FROM sessions WHERE session_key = ?";
        jdbcTemplate.update(sql, sessionKey);
        log.debug("会话删除成功: sessionKey={}", sessionKey);
    }

    /**
     * 保存会话消息
     * <p>
     * 向会话历史中添加一条消息。
     * 消息包含角色、内容和可选的工具调用信息。
     * </p>
     *
     * @param message 会话消息对象
     * @return 保存后的消息对象（包含生成的 ID）
     */
    public SessionMessage saveMessage(SessionMessage message) {
        log.debug("保存会话消息: sessionKey={}, role={}", message.sessionKey(), message.role());

        String sql = """
            INSERT INTO session_messages
            (session_key, role, content, tool_call, timestamp)
            VALUES (?, ?, ?, ?, ?)
            """;

        long now = Instant.now().getEpochSecond();
        long timestamp = message.timestamp() != null
            ? message.timestamp().toEpochSecond()
            : now;

        jdbcTemplate.update(sql,
            message.sessionKey(),
            message.role(),
            message.content(),
            toJson(message.toolCall()),
            timestamp
        );

        // 获取自动生成的 ID
        Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
        log.debug("消息保存成功: id={}, sessionKey={}", id, message.sessionKey());

        return new SessionMessage(
            id != null ? id : 0,
            message.sessionKey(),
            message.role(),
            message.content(),
            message.toolCall(),
            message.timestamp()
        );
    }

    /**
     * 获取会话的消息列表
     * <p>
     * 按时间倒序获取指定会话的最近消息。
     * </p>
     *
     * @param sessionKey 会话键
     * @param limit 返回的消息数量限制
     * @return 消息列表
     */
    public List<SessionMessage> findMessages(String sessionKey, int limit) {
        log.debug("查询会话消息: sessionKey={}, limit={}", sessionKey, limit);
        String sql = """
            SELECT id, session_key, role, content, tool_call, timestamp
            FROM session_messages
            WHERE session_key = ?
            ORDER BY timestamp DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, new SessionMessageRowMapper(), sessionKey, limit);
    }

    /**
     * 获取最近的消息
     * <p>
     * 跨所有会话获取最近的消息。
     * </p>
     *
     * @param limit 返回的消息数量限制
     * @return 消息列表
     */
    public List<SessionMessage> findRecentMessages(int limit) {
        log.debug("查询最近的消息: limit={}", limit);
        String sql = """
            SELECT id, session_key, role, content, tool_call, timestamp
            FROM session_messages
            ORDER BY timestamp DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, new SessionMessageRowMapper(), limit);
    }

    /**
     * 删除会话的所有消息
     *
     * @param sessionKey 会话键
     */
    public void deleteMessages(String sessionKey) {
        log.info("删除会话消息: sessionKey={}", sessionKey);
        String sql = "DELETE FROM session_messages WHERE session_key = ?";
        int count = jdbcTemplate.update(sql, sessionKey);
        log.debug("删除了 {} 条消息: sessionKey={}", count, sessionKey);
    }

    /**
     * 统计会话的消息数量
     *
     * @param sessionKey 会话键
     * @return 消息数量
     */
    public int countMessages(String sessionKey) {
        log.debug("统计会话消息数量: sessionKey={}", sessionKey);
        String sql = "SELECT COUNT(*) FROM session_messages WHERE session_key = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sessionKey);
        int result = count != null ? count : 0;
        log.debug("会话消息数量: sessionKey={}, count={}", sessionKey, result);
        return result;
    }

    /**
     * 获取总会话数量
     *
     * @return 会话总数
     */
    public int getSessionCount() {
        log.debug("统计总会话数量");
        String sql = "SELECT COUNT(*) FROM sessions";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        int result = count != null ? count : 0;
        log.debug("总会话数量: {}", result);
        return result;
    }

    /**
     * 获取活跃会话数量
     * <p>
     * 统计过去 24 小时内有活动的会话数量。
     * </p>
     *
     * @return 活跃会话数量
     */
    public int getActiveSessionCount() {
        log.debug("统计活跃会话数量");
        long oneDayAgo = Instant.now().minusSeconds(24 * 60 * 60).getEpochSecond();
        String sql = "SELECT COUNT(*) FROM sessions WHERE last_activity_at > ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, oneDayAgo);
        int result = count != null ? count : 0;
        log.debug("活跃会话数量: {}", result);
        return result;
    }

    // ============ 辅助方法 ============

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param obj 要序列化的对象
     * @return JSON 字符串，如果序列化失败则返回 null
     */
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON 序列化失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为对象
     *
     * @param json JSON 字符串
     * @param clazz 目标类型
     * @return 反序列化后的对象，如果失败则返回 null
     */
    private <T> T fromJson(String json, Class<T> clazz) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON 反序列化失败: {}, error={}", json, e.getMessage());
            return null;
        }
    }

    // ============ 行映射器 ============

    /**
     * 会话行映射器
     * <p>
     * 将 ResultSet 映射为 Session 对象。
     * </p>
     */
    private class SessionRowMapper implements RowMapper<Session> {
        @Override
        public Session mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Session(
              rs.getString("session_key"),                                    // 会话键
              rs.getString("channel_id"),                                     // 渠道 ID
              rs.getString("account_id"),                                     // 账户 ID
              rs.getString("chat_id"),                                        // 聊天 ID
              ChannelCapabilities.ChatType.valueOf(rs.getString("chat_type")), // 聊天类型
              ZonedDateTime.ofInstant(                                        // 创建时间
                  Instant.ofEpochSecond(rs.getLong("created_at")),
                  ZoneId.systemDefault()
              ),
              ZonedDateTime.ofInstant(                                        // 更新时间
                  Instant.ofEpochSecond(rs.getLong("updated_at")),
                  ZoneId.systemDefault()
              ),
              ZonedDateTime.ofInstant(                                        // 最后活动时间
                  Instant.ofEpochSecond(rs.getLong("last_activity_at")),
                  ZoneId.systemDefault()
              ),
              fromJson(rs.getString("metadata"), Map.class),                   // 元数据
              fromJson(rs.getString("context"), Map.class)                    // 上下文
          );
        }
    }

    /**
     * 会话消息行映射器
     * <p>
     * 将 ResultSet 映射为 SessionMessage 对象。
     * </p>
     */
    private class SessionMessageRowMapper implements RowMapper<SessionMessage> {
        @Override
        public SessionMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SessionMessage(
                rs.getLong("id"),                                             // 消息 ID
                rs.getString("session_key"),                                   // 会话键
                rs.getString("role"),                                          // 角色（user/assistant/system）
                rs.getString("content"),                                       // 内容
                fromJson(rs.getString("tool_call"), Map.class),               // 工具调用信息
                ZonedDateTime.ofInstant(                                       // 时间戳
                    Instant.ofEpochSecond(rs.getLong("timestamp")),
                    ZoneId.systemDefault()
                )
            );
        }
    }

    // ============ 数据类 ============

    /**
     * 会话数据类
     * <p>
     * 表示一个用户会话，包含会话的基本信息、时间戳和上下文数据。
     * </p>
     *
     * @param sessionKey 会话唯一标识符
     * @param channelId 渠道 ID
     * @param accountId 账户 ID
     * @param chatId 聊天 ID
     * @param chatType 聊天类型（私聊、群聊等）
     * @param createdAt 创建时间
     * @param updatedAt 更新时间
     * @param lastActivityAt 最后活动时间
     * @param metadata 元数据（JSON 格式的键值对）
     * @param context 会话上下文（JSON 格式的键值对）
     */
    public record Session(
        String sessionKey,
        String channelId,
        String accountId,
        String chatId,
        ChannelCapabilities.ChatType chatType,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        ZonedDateTime lastActivityAt,
        Map<String, Object> metadata,
        Map<String, Object> context
    ) {
        public Session {
            // 确保元数据和上下文不为 null
            if (metadata == null) metadata = Map.of();
            if (context == null) context = Map.of();
        }
    }

    /**
     * 会话消息数据类
     * <p>
     * 表示会话中的一条消息，包含角色、内容和可选的工具调用信息。
     * </p>
     *
     * @param id 消息 ID（自动生成）
     * @param sessionKey 会话键
     * @param role 消息角色（user/assistant/system）
     * @param content 消息内容
     * @param toolCall 工具调用信息（JSON 格式）
     * @param timestamp 消息时间戳
     */
    public record SessionMessage(
        long id,
        String sessionKey,
        String role,
        String content,
        Map<String, Object> toolCall,
        ZonedDateTime timestamp
    ) {
        public SessionMessage {
            // 确保工具调用信息不为 null
            if (toolCall == null) toolCall = Map.of();
        }
    }
}
