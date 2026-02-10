package com.openclawlite.adapter.protocol.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Gateway schema validator
 * Validates request parameters against expected schemas
 */
@Component
public class SchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);

    private final ObjectMapper objectMapper;

    public SchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Validate connect parameters
     */
    public ValidationResult validateConnectParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);
            // Version is optional, defaults to PROTOCOL_VERSION
            if (node.has("role") && !node.get("role").isTextual()) {
                return ValidationResult.error("role must be a string");
            }
            if (node.has("scopes") && !node.get("scopes").isArray()) {
                return ValidationResult.error("scopes must be an array");
            }
            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Validate agents.create params
     */
    public ValidationResult validateAgentsCreateParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);

            String name = node.has("name") ? node.get("name").asText() : null;
            if (name == null || name.trim().isEmpty()) {
                return ValidationResult.error("name is required");
            }

            String workspace = node.has("workspace") ? node.get("workspace").asText() : null;
            if (workspace == null || workspace.trim().isEmpty()) {
                return ValidationResult.error("workspace is required");
            }

            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Validate agents.update params
     */
    public ValidationResult validateAgentsUpdateParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);

            String agentId = node.has("agentId") ? node.get("agentId").asText() : null;
            if (agentId == null || agentId.trim().isEmpty()) {
                return ValidationResult.error("agentId is required");
            }

            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Validate agents.delete params
     */
    public ValidationResult validateAgentsDeleteParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);

            String agentId = node.has("agentId") ? node.get("agentId").asText() : null;
            if (agentId == null || agentId.trim().isEmpty()) {
                return ValidationResult.error("agentId is required");
            }

            // deleteFiles is optional, defaults to true
            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Validate agents.files.list params
     */
    public ValidationResult validateAgentsFilesListParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);

            String agentId = node.has("agentId") ? node.get("agentId").asText() : null;
            if (agentId == null || agentId.trim().isEmpty()) {
                return ValidationResult.error("agentId is required");
            }

            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Validate agents.files.get params
     */
    public ValidationResult validateAgentsFilesGetParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);

            String agentId = node.has("agentId") ? node.get("agentId").asText() : null;
            if (agentId == null || agentId.trim().isEmpty()) {
                return ValidationResult.error("agentId is required");
            }

            String name = node.has("name") ? node.get("name").asText() : null;
            if (name == null || name.trim().isEmpty()) {
                return ValidationResult.error("name is required");
            }

            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Validate agents.files.set params
     */
    public ValidationResult validateAgentsFilesSetParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);

            String agentId = node.has("agentId") ? node.get("agentId").asText() : null;
            if (agentId == null || agentId.trim().isEmpty()) {
                return ValidationResult.error("agentId is required");
            }

            String name = node.has("name") ? node.get("name").asText() : null;
            if (name == null || name.trim().isEmpty()) {
                return ValidationResult.error("name is required");
            }

            if (!node.has("content")) {
                return ValidationResult.error("content is required");
            }

            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Validate sessions.list params
     */
    public ValidationResult validateSessionsListParams(Object params) {
        // params is optional, can be empty
        return ValidationResult.success();
    }

    /**
     * Validate sessions.preview params
     */
    public ValidationResult validateSessionsPreviewParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);

            String sessionKey = node.has("sessionKey") ? node.get("sessionKey").asText() : null;
            if (sessionKey == null || sessionKey.trim().isEmpty()) {
                return ValidationResult.error("sessionKey is required");
            }

            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Validate send params
     */
    public ValidationResult validateSendParams(Object params) {
        if (params == null) {
            return ValidationResult.error("params is required");
        }

        try {
            JsonNode node = objectMapper.valueToTree(params);

            // At least one of 'to' or 'sessionId' is required
            boolean hasTo = node.has("to") && !node.get("to").isNull();
            boolean hasSession = node.has("sessionId") && !node.get("sessionId").isNull();

            if (!hasTo && !hasSession) {
                return ValidationResult.error("either 'to' or 'sessionId' is required");
            }

            if (node.has("text") && node.get("text").isNull()) {
                return ValidationResult.error("text cannot be null");
            }

            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("invalid params: " + e.getMessage());
        }
    }

    /**
     * Generic validation result
     */
    public record ValidationResult(
        boolean valid,
        String error
    ) {
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String error) {
            return new ValidationResult(false, error);
        }

        public List<String> errors() {
            if (!valid) {
                return List.of(error);
            }
            return List.of();
        }
    }
}
