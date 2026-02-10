package com.openclawlite.openclaw.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Database configuration for OpenClaw Lite.
 * Uses SQLite with HikariCP connection pooling.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:data/openclaw.db");

        // HikariCP settings
        config.setMaximumPoolSize(1);  // SQLite doesn't support multiple writers
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // SQLite-specific settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        // Initialize database schema
        initializeDatabase(template);
        return template;
    }

    private void initializeDatabase(JdbcTemplate template) {
        // Create agents table (新增)
        template.execute("""
            CREATE TABLE IF NOT EXISTS agents (
                agent_id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                workspace TEXT NOT NULL,
                model TEXT,
                avatar TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """);

        // Create sessions table
        template.execute("""
            CREATE TABLE IF NOT EXISTS sessions (
                session_key TEXT PRIMARY KEY,
                channel_id TEXT NOT NULL,
                account_id TEXT NOT NULL,
                chat_id TEXT NOT NULL,
                chat_type TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                last_activity_at INTEGER NOT NULL,
                metadata TEXT,
                context TEXT,
                agent_id TEXT,
                FOREIGN KEY (agent_id) REFERENCES agents(agent_id)
            )
        """);

        // Create session_messages table (增强)
        template.execute("""
            CREATE TABLE IF NOT EXISTS session_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_key TEXT NOT NULL,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                tool_call TEXT,
                timestamp INTEGER NOT NULL,
                tool_call_id TEXT,
                tool_name TEXT,
                is_synthetic INTEGER DEFAULT 0,
                FOREIGN KEY (session_key) REFERENCES sessions(session_key)
            )
        """);

        // Create indexes for better query performance
        template.execute("CREATE INDEX IF NOT EXISTS idx_session_messages_session ON session_messages(session_key)");
        template.execute("CREATE INDEX IF NOT EXISTS idx_session_messages_timestamp ON session_messages(timestamp)");
        template.execute("CREATE INDEX IF NOT EXISTS idx_sessions_channel ON sessions(channel_id)");
        template.execute("CREATE INDEX IF NOT EXISTS idx_sessions_account ON sessions(account_id)");
        template.execute("CREATE INDEX IF NOT EXISTS idx_sessions_agent ON sessions(agent_id)");  // 新增
        template.execute("CREATE INDEX IF NOT EXISTS idx_session_messages_tool_call_id ON session_messages(tool_call_id)");  // 新增

        // Create users table
        template.execute("""
            CREATE TABLE IF NOT EXISTS users (
                user_id TEXT PRIMARY KEY,
                channel_id TEXT NOT NULL,
                account_id TEXT NOT NULL,
                external_user_id TEXT NOT NULL,
                username TEXT,
                display_name TEXT,
                profile_url TEXT,
                first_seen_at INTEGER NOT NULL,
                last_seen_at INTEGER NOT NULL,
                metadata TEXT,
                UNIQUE(channel_id, account_id, external_user_id)
            )
        """);

        // Create channel_accounts table
        template.execute("""
            CREATE TABLE IF NOT EXISTS channel_accounts (
                account_id TEXT PRIMARY KEY,
                channel_id TEXT NOT NULL,
                name TEXT,
                enabled INTEGER NOT NULL DEFAULT 0,
                configured INTEGER NOT NULL DEFAULT 0,
                linked INTEGER NOT NULL DEFAULT 0,
                running INTEGER NOT NULL DEFAULT 0,
                connected INTEGER NOT NULL DEFAULT 0,
                config TEXT,
                metadata TEXT
            )
        """);

        // Create skills table
        template.execute("""
            CREATE TABLE IF NOT EXISTS skills (
                skill_id TEXT PRIMARY KEY,
                name TEXT NOT NULL UNIQUE,
                description TEXT,
                emoji TEXT,
                user_invocable INTEGER NOT NULL DEFAULT 0,
                disable_model_invocation INTEGER NOT NULL DEFAULT 0,
                version INTEGER NOT NULL,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """);

        // Create tools table
        template.execute("""
            CREATE TABLE IF NOT EXISTS tools (
                tool_id TEXT PRIMARY KEY,
                name TEXT NOT NULL UNIQUE,
                description TEXT,
                type TEXT NOT NULL,
                enabled INTEGER NOT NULL DEFAULT 1,
                config TEXT,
                plugin_id TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """);

        // Create memories table (新增 - 阶段 8)
        template.execute("""
            CREATE TABLE IF NOT EXISTS memories (
                id TEXT PRIMARY KEY,
                session_id TEXT NOT NULL,
                content TEXT NOT NULL,
                role TEXT,
                timestamp INTEGER NOT NULL,
                importance INTEGER DEFAULT 1,
                metadata TEXT,
                created_at INTEGER NOT NULL
            )
        """);

        // Create memory_embeddings table (新增 - 阶段 8)
        template.execute("""
            CREATE TABLE IF NOT EXISTS memory_embeddings (
                memory_id TEXT PRIMARY KEY,
                embedding BLOB NOT NULL,
                dimension INTEGER NOT NULL,
                FOREIGN KEY (memory_id) REFERENCES memories(id) ON DELETE CASCADE
            )
        """);

        // Create memory indexes (新增 - 阶段 8)
        template.execute("CREATE INDEX IF NOT EXISTS idx_memories_session ON memories(session_id)");
        template.execute("CREATE INDEX IF NOT EXISTS idx_memories_timestamp ON memories(timestamp)");
        template.execute("CREATE INDEX IF NOT EXISTS idx_memories_importance ON memories(importance)");
    }
}
