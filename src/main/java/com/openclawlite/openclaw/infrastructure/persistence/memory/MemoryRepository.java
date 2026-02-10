package com.openclawlite.openclaw.infrastructure.persistence.memory;

import com.openclawlite.openclaw.domain.memory.Memory;
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
 * Repository for memory storage and retrieval
 * Handles database operations for memories
 */
@Repository
public class MemoryRepository {

    private static final Logger log = LoggerFactory.getLogger(MemoryRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public MemoryRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        initializeTables();
    }

    /**
     * Initialize memory tables
     */
    private void initializeTables() {
        // Create memories table
        jdbcTemplate.execute("""
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

        // Create memory_embeddings table
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS memory_embeddings (
                memory_id TEXT PRIMARY KEY,
                embedding BLOB NOT NULL,
                dimension INTEGER NOT NULL,
                FOREIGN KEY (memory_id) REFERENCES memories(id) ON DELETE CASCADE
            )
        """);

        // Create indexes
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_memories_session ON memories(session_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_memories_timestamp ON memories(timestamp)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_memories_importance ON memories(importance)");

        log.info("Memory tables initialized");
    }

    /**
     * Save a memory with embedding
     */
    public void save(Memory memory) {
        try {
            long now = System.currentTimeMillis();

            // Save memory
            jdbcTemplate.update("""
                INSERT INTO memories (id, session_id, content, role, timestamp, importance, metadata, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                memory.id(),
                memory.sessionId(),
                memory.content(),
                memory.role(),
                memory.timestamp(),
                memory.importance(),
                serializeMetadata(memory.metadata()),
                now
            );

            // Save embedding
            if (memory.embedding() != null && memory.embedding().length > 0) {
                byte[] blob = serializeEmbedding(memory.embedding());

                jdbcTemplate.update("""
                    INSERT INTO memory_embeddings (memory_id, embedding, dimension)
                    VALUES (?, ?, ?)
                    """,
                    memory.id(),
                    blob,
                    memory.embedding().length
                );
            }

            log.debug("Saved memory: {}", memory.id());

        } catch (Exception e) {
            log.error("Failed to save memory: {}", memory.id(), e);
            throw new RuntimeException("Failed to save memory", e);
        }
    }

    /**
     * Get memory by ID
     */
    public Optional<Memory> findById(String id) {
        try {
            List<Memory> results = jdbcTemplate.query("""
                SELECT m.*, e.embedding, e.dimension
                FROM memories m
                LEFT JOIN memory_embeddings e ON m.id = e.memory_id
                WHERE m.id = ?
                """,
                new MemoryRowMapper(),
                id
            );

            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));

        } catch (Exception e) {
            log.error("Failed to find memory: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * Get memories by session ID
     */
    public List<Memory> findBySessionId(String sessionId) {
        try {
            return jdbcTemplate.query("""
                SELECT m.*, e.embedding, e.dimension
                FROM memories m
                LEFT JOIN memory_embeddings e ON m.id = e.memory_id
                WHERE m.session_id = ?
                ORDER BY m.timestamp DESC
                """,
                new MemoryRowMapper(),
                sessionId
            );

        } catch (Exception e) {
            log.error("Failed to find memories for session: {}", sessionId, e);
            return List.of();
        }
    }

    /**
     * Get memories by session ID with pagination
     */
    public List<Memory> findBySessionId(String sessionId, int limit, int offset) {
        try {
            return jdbcTemplate.query("""
                SELECT m.*, e.embedding, e.dimension
                FROM memories m
                LEFT JOIN memory_embeddings e ON m.id = e.memory_id
                WHERE m.session_id = ?
                ORDER BY m.timestamp DESC
                LIMIT ? OFFSET ?
                """,
                new MemoryRowMapper(),
                sessionId, limit, offset
            );

        } catch (Exception e) {
            log.error("Failed to find memories for session: {}", sessionId, e);
            return List.of();
        }
    }

    /**
     * Get recent memories across all sessions
     */
    public List<Memory> findRecent(int limit) {
        try {
            return jdbcTemplate.query("""
                SELECT m.*, e.embedding, e.dimension
                FROM memories m
                LEFT JOIN memory_embeddings e ON m.id = e.memory_id
                ORDER BY m.timestamp DESC
                LIMIT ?
                """,
                new MemoryRowMapper(),
                limit
            );

        } catch (Exception e) {
            log.error("Failed to find recent memories", e);
            return List.of();
        }
    }

    /**
     * Delete memory by ID
     */
    public boolean deleteById(String id) {
        try {
            int deleted = jdbcTemplate.update("DELETE FROM memories WHERE id = ?", id);
            return deleted > 0;
        } catch (Exception e) {
            log.error("Failed to delete memory: {}", id, e);
            return false;
        }
    }

    /**
     * Delete all memories for a session
     */
    public int deleteBySessionId(String sessionId) {
        try {
            return jdbcTemplate.update("DELETE FROM memories WHERE session_id = ?", sessionId);
        } catch (Exception e) {
            log.error("Failed to delete memories for session: {}", sessionId, e);
            return 0;
        }
    }

    /**
     * Count memories for a session
     */
    public int countBySessionId(String sessionId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM memories WHERE session_id = ?",
                Integer.class,
                sessionId
            );
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Failed to count memories for session: {}", sessionId, e);
            return 0;
        }
    }

    /**
     * Serialize metadata to JSON string
     */
    private String serializeMetadata(java.util.Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        // TODO: Use proper JSON library
        return metadata.toString();
    }

    /**
     * Serialize embedding to byte array
     */
    private byte[] serializeEmbedding(float[] embedding) {
        byte[] blob = new byte[embedding.length * 4];
        for (int i = 0; i < embedding.length; i++) {
            int bits = Float.floatToIntBits(embedding[i]);
            blob[i * 4] = (byte) (bits >> 24);
            blob[i * 4 + 1] = (byte) (bits >> 16);
            blob[i * 4 + 2] = (byte) (bits >> 8);
            blob[i * 4 + 3] = (byte) bits;
        }
        return blob;
    }

    /**
     * Deserialize embedding from byte array
     */
    private float[] deserializeEmbedding(byte[] blob, int dimension) {
        float[] embedding = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            int bits = ((blob[i * 4] & 0xFF) << 24) |
                       ((blob[i * 4 + 1] & 0xFF) << 16) |
                       ((blob[i * 4 + 2] & 0xFF) << 8) |
                       (blob[i * 4 + 3] & 0xFF);
            embedding[i] = Float.intBitsToFloat(bits);
        }
        return embedding;
    }

    /**
     * Row mapper for Memory
     */
    private class MemoryRowMapper implements RowMapper<Memory> {
        @Override
        public Memory mapRow(ResultSet rs, int rowNum) throws SQLException {
            String id = rs.getString("id");
            String sessionId = rs.getString("session_id");
            String content = rs.getString("content");
            String role = rs.getString("role");
            long timestamp = rs.getLong("timestamp");
            int importance = rs.getInt("importance");

            // Deserialize embedding
            byte[] blob = rs.getBytes("embedding");
            float[] embedding = null;
            if (blob != null) {
                int dimension = rs.getInt("dimension");
                embedding = deserializeEmbedding(blob, dimension);
            }

            return new Memory(
                id,
                sessionId,
                content,
                embedding,
                role,
                timestamp,
                java.util.Map.of(),  // TODO: Deserialize metadata
                importance
            );
        }
    }
}
