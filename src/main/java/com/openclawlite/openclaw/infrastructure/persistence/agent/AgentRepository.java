package com.openclawlite.openclaw.infrastructure.persistence.agent;

import com.openclawlite.openclaw.application.agent.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Agent 配置持久化仓库
 * <p>
 * 负责 Agent 配置数据的数据库持久化操作。
 * 管理 agents 表的所有 CRUD 操作。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建 Agent 配置记录</li>
 *   <li>查询 Agent 配置（按 ID 或列表）</li>
 *   <li>更新 Agent 配置</li>
 *   <li>删除 Agent 配置</li>
 *   <li>检查 Agent 是否存在</li>
 *   <li>统计 Agent 数量</li>
 * </ul>
 *
 * <p>数据库表结构：</p>
 * <pre>
 * CREATE TABLE agents (
 *   agent_id VARCHAR(255) PRIMARY KEY,
 *   name VARCHAR(255) NOT NULL,
 *   workspace VARCHAR(500) NOT NULL,
 *   model VARCHAR(100),
 *   avatar VARCHAR(500),
 *   created_at BIGINT NOT NULL,
 *   updated_at BIGINT NOT NULL
 * );
 * </pre>
 */
@Repository
public class AgentRepository {

    private static final Logger log = LoggerFactory.getLogger(AgentRepository.class);

    /** JDBC 模板，用于执行数据库操作 */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数
     *
     * @param dataSource 数据源
     */
    public AgentRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * AgentConfig 行映射器
     * <p>
     * 将 ResultSet 映射为 AgentConfig 对象。
     * </p>
     */
    private static class AgentConfigRowMapper implements RowMapper<AgentConfig> {
        @Override
        public AgentConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AgentConfig(
                rs.getString("agent_id"),    // Agent ID
                rs.getString("name"),         // Agent 名称
                rs.getString("workspace"),    // 工作空间路径
                rs.getString("model"),        // 使用的模型
                rs.getString("avatar"),       // 头像 URL
                rs.getLong("created_at"),     // 创建时间戳
                rs.getLong("updated_at")      // 更新时间戳
            );
        }
    }

    /**
     * 根据 ID 查询 Agent
     * <p>
     * 从数据库查询指定 ID 的 Agent 配置。
     * </p>
     *
     * @param agentId Agent ID
     * @return Agent 配置的 Optional 对象，如果不存在则返回 Optional.empty()
     */
    public Optional<AgentConfig> findById(String agentId) {
        log.debug("查询 Agent: agentId={}", agentId);
        try {
            List<AgentConfig> results = jdbcTemplate.query(
                "SELECT agent_id, name, workspace, model, avatar, created_at, updated_at " +
                "FROM agents WHERE agent_id = ?",
                new AgentConfigRowMapper(),
                agentId
            );
            if (results.isEmpty()) {
                log.debug("Agent 不存在: agentId={}", agentId);
                return Optional.empty();
            }
            log.debug("找到 Agent: agentId={}", agentId);
            return Optional.of(results.get(0));
        } catch (Exception e) {
            log.error("查询 Agent 失败: agentId={}", agentId, e);
            return Optional.empty();
        }
    }

    /**
     * 查询所有 Agent
     * <p>
     * 从数据库获取所有 Agent 配置，按创建时间倒序排列。
     * </p>
     *
     * @return Agent 配置列表
     */
    public List<AgentConfig> findAll() {
        log.debug("查询所有 Agent");
        try {
            List<AgentConfig> results = jdbcTemplate.query(
                "SELECT agent_id, name, workspace, model, avatar, created_at, updated_at " +
                "FROM agents ORDER BY created_at DESC",
                new AgentConfigRowMapper()
            );
            log.debug("查询到 {} 个 Agent", results.size());
            return results;
        } catch (Exception e) {
            log.error("查询所有 Agent 失败", e);
            return List.of();
        }
    }

    /**
     * 保存新的 Agent 配置
     * <p>
     * 向数据库插入新的 Agent 记录。
     * </p>
     *
     * @param config Agent 配置对象
     * @return 如果保存成功返回 true，否则返回 false
     */
    public boolean save(AgentConfig config) {
        log.info("保存 Agent: agentId={}, name={}", config.agentId(), config.name());
        try {
            long now = System.currentTimeMillis();
            int updated = jdbcTemplate.update(
                "INSERT INTO agents (agent_id, name, workspace, model, avatar, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                config.agentId(),
                config.name(),
                config.workspace(),
                config.model(),
                config.avatar(),
                config.createdAt() > 0 ? config.createdAt() : now,  // 使用提供的创建时间或当前时间
                now                                                  // 更新时间设为当前时间
            );
            boolean success = updated > 0;
            if (success) {
                log.info("成功保存 Agent: agentId={}", config.agentId());
            } else {
                log.warn("Agent 保存失败，未插入任何记录: agentId={}", config.agentId());
            }
            return success;
        } catch (Exception e) {
            log.error("保存 Agent 时发生错误: agentId={}", config.agentId(), e);
            return false;
        }
    }

    /**
     * 更新现有的 Agent 配置
     * <p>
     * 更新数据库中的 Agent 记录。
     * </p>
     *
     * @param config Agent 配置对象
     * @return 如果更新成功返回 true，否则返回 false
     */
    public boolean update(AgentConfig config) {
        log.info("更新 Agent: agentId={}, name={}", config.agentId(), config.name());
        try {
            int updated = jdbcTemplate.update(
                "UPDATE agents SET name = ?, workspace = ?, model = ?, avatar = ?, updated_at = ? " +
                "WHERE agent_id = ?",
                config.name(),
                config.workspace(),
                config.model(),
                config.avatar(),
                System.currentTimeMillis(),  // 更新时间戳
                config.agentId()
            );
            boolean success = updated > 0;
            if (success) {
                log.info("成功更新 Agent: agentId={}", config.agentId());
            } else {
                log.warn("Agent 更新失败，未找到记录: agentId={}", config.agentId());
            }
            return success;
        } catch (Exception e) {
            log.error("更新 Agent 时发生错误: agentId={}", config.agentId(), e);
            return false;
        }
    }

    /**
     * 删除 Agent 配置
     * <p>
     * 从数据库删除指定的 Agent 记录。
     * </p>
     *
     * @param agentId Agent ID
     * @return 如果删除成功返回 true，否则返回 false
     */
    public boolean delete(String agentId) {
        log.info("删除 Agent: agentId={}", agentId);
        try {
            int updated = jdbcTemplate.update(
                "DELETE FROM agents WHERE agent_id = ?",
                agentId
            );
            boolean success = updated > 0;
            if (success) {
                log.info("成功删除 Agent: agentId={}", agentId);
            } else {
                log.warn("Agent 删除失败，未找到记录: agentId={}", agentId);
            }
            return success;
        } catch (Exception e) {
            log.error("删除 Agent 时发生错误: agentId={}", agentId, e);
            return false;
        }
    }

    /**
     * 检查 Agent 是否存在
     * <p>
     * 查询数据库中是否存在指定 ID 的 Agent。
     * </p>
     *
     * @param agentId Agent ID
     * @return 如果存在返回 true，否则返回 false
     */
    public boolean exists(String agentId) {
        log.debug("检查 Agent 是否存在: agentId={}", agentId);
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM agents WHERE agent_id = ?",
                Integer.class,
                agentId
            );
            boolean exists = count != null && count > 0;
            log.debug("Agent 存在性检查结果: agentId={}, exists={}", agentId, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查 Agent 存在性时发生错误: agentId={}", agentId, e);
            return false;
        }
    }

    /**
     * 统计 Agent 总数
     * <p>
     * 返回数据库中所有 Agent 的数量。
     * </p>
     *
     * @return Agent 总数
     */
    public int count() {
        log.debug("统计 Agent 总数");
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM agents",
                Integer.class
            );
            int total = count != null ? count : 0;
            log.debug("Agent 总数: {}", total);
            return total;
        } catch (Exception e) {
            log.error("统计 Agent 数量时发生错误", e);
            return 0;
        }
    }
}
