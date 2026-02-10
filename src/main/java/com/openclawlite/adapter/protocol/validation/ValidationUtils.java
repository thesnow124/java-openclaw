package com.openclawlite.adapter.protocol.validation;

import com.openclawlite.common.dto.ErrorShape;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validation utilities
 */
public class ValidationUtils {

    /**
     * Format validation errors
     */
    public static String formatValidationErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "unknown validation error";
        }

        // Remove duplicates while preserving order
        List<String> unique = errors.stream()
            .filter(e -> e != null && !e.trim().isEmpty())
            .distinct()
            .collect(Collectors.toList());

        if (unique.isEmpty()) {
            return "unknown validation error";
        }

        return String.join("; ", unique);
    }

    /**
     * Create error shape from validation result
     */
    public static ErrorShape validationError(ValidationUtils.ValidationResult result) {
        return ErrorShape.validationError(formatValidationErrors(result.errors()));
    }

    /**
     * Check if a string is null or empty
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Simple validation result record
     */
    public record ValidationResult(
        boolean valid,
        List<String> errors
    ) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult error(String error) {
            return new ValidationResult(false, List.of(error));
        }

        public static ValidationResult errors(List<String> errors) {
            return new ValidationResult(false, errors);
        }
    }
}
