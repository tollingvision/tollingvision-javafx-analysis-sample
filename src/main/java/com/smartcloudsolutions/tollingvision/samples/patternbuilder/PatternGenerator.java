package com.smartcloudsolutions.tollingvision.samples.patternbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Service for generating regex patterns from visual configuration and
 * validating
 * pattern correctness. Converts token-based rules into regex patterns suitable
 * for the TollingVision processing pipeline.
 */
public class PatternGenerator {

    // Special regex characters that need escaping
    private static final String REGEX_SPECIAL_CHARS = "[]{}()*+?.\\^$|";

    /**
     * Generates a group pattern regex from tokens with automatic capturing group
     * insertion around the selected Group ID token.
     * 
     * @param tokens       list of filename tokens in order
     * @param groupIdToken the token selected as the group identifier
     * @return regex pattern with capturing group around the group ID token
     * @throws IllegalArgumentException if groupIdToken is not in the tokens list
     */
    public String generateGroupPattern(List<FilenameToken> tokens, FilenameToken groupIdToken) {
        if (tokens == null || tokens.isEmpty()) {
            return "";
        }

        if (groupIdToken == null) {
            throw new IllegalArgumentException("Group ID token cannot be null");
        }

        if (!tokens.contains(groupIdToken)) {
            throw new IllegalArgumentException("Group ID token must be present in the tokens list");
        }

        StringBuilder pattern = new StringBuilder();
        pattern.append("^"); // Start of string anchor
        Map<Integer, Set<String>> positionMap = new HashMap<>();
        for (FilenameToken token : tokens) {
            String regex = generateTokenPattern(token, true);
            if (token.equals(groupIdToken)) {
                // Wrap group ID token in capturing group
                regex = "(" + regex + ")";
            }
            // Add regex to the position map
            positionMap.computeIfAbsent(token.getPosition(), k -> new HashSet<>())
                    .add(regex);
        }
        // Iterate through positions in order
        for (int pos : positionMap.keySet()) {
            if (pos > 0) {
                pattern.append("[_\\-\\.\\s]+"); // Common delimiters
            }
            Set<String> regexSet = positionMap.get(pos);
            if (regexSet.size() > 1) {
                // If multiple regex patterns exist for this position, create a group
                String groupPattern = String.join("|", regexSet);
                pattern.append("[").append(groupPattern).append("]");
            } else {
                // If only one regex pattern exists, use it directly
                pattern.append(regexSet.iterator().next());
            }
        }
        pattern.append("$"); // End of string anchor
        return pattern.toString();
    }

    /**
     * Generates a regex pattern for a specific token based on its type and value.
     * For group patterns, camera side tokens are treated as generic patterns
     * since role detection happens separately.
     * 
     * @param token          the token to generate a pattern for
     * @param isGroupPattern true if this is for a group pattern (excludes
     *                       role-specific patterns)
     * @return regex pattern that matches the token
     */
    private String generateTokenPattern(FilenameToken token, boolean isGroupPattern) {
        return switch (token.getSuggestedType()) {
            case PREFIX, SUFFIX -> escapeRegexChars(token.getValue());
            case GROUP_ID -> "[\\w\\-]+"; // Alphanumeric with hyphens
            case CAMERA_SIDE -> {
                if (isGroupPattern) {
                    // For group patterns, treat camera side as generic word pattern
                    yield "\\w+";
                } else {
                    // For role patterns, use specific camera side patterns
                    yield generateCameraSidePattern(token.getValue());
                }
            }
            case DATE -> generateDatePattern(token.getValue());
            case INDEX -> "\\d+"; // One or more digits
            case EXTENSION -> generateExtensionPattern(token.getValue());
            case UNKNOWN -> "\\w*"; // Generic pattern for unknown tokens
        };
    }

    /**
     * Generates a pattern for camera/side tokens that matches common synonyms.
     * 
     * @param value the camera/side value
     * @return regex pattern matching camera/side synonyms
     */
    private String generateCameraSidePattern(String value) {
        String lowerValue = value.toLowerCase();

        // Generate pattern based on detected camera type
        if (isOverviewSynonym(lowerValue)) {
            return "(?i:overview|ov|ovr|ovw|scene|full)";
        } else if (isFrontSynonym(lowerValue)) {
            return "(?i:front|f|fr|forward)";
        } else if (isRearSynonym(lowerValue)) {
            return "(?i:rear|r|rr|back|behind)";
        } else {
            // Fallback to exact match
            return "(?i:" + escapeRegexChars(value) + ")";
        }
    }

    /**
     * Generates a pattern for date tokens based on the detected format.
     * 
     * @param value the date value
     * @return regex pattern matching the date format
     */
    private String generateDatePattern(String value) {
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "\\d{4}-\\d{2}-\\d{2}"; // YYYY-MM-DD
        } else if (value.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return "\\d{2}-\\d{2}-\\d{4}"; // MM-DD-YYYY
        } else if (value.matches("\\d{8}")) {
            return "\\d{8}"; // YYYYMMDD
        } else {
            // Fallback to generic date pattern
            return "\\d{2,4}[\\-/]?\\d{2}[\\-/]?\\d{2,4}";
        }
    }

    /**
     * Generates a pattern for file extension tokens.
     * 
     * @param value the extension value
     * @return regex pattern matching the extension
     */
    private String generateExtensionPattern(String value) {
        return "(?i:" + escapeRegexChars(value) + ")";
    }

    /**
     * Generates a role pattern regex from role rules for a specific image role.
     * 
     * @param rules list of role rules
     * @param role  the target image role
     * @return regex pattern that matches filenames for the specified role
     */
    public String generateRolePattern(List<RoleRule> rules, ImageRole role) {
        if (rules == null || rules.isEmpty()) {
            return "";
        }

        List<String> rolePatterns = rules.stream()
                .filter(rule -> rule.getTargetRole() == role)
                .sorted((r1, r2) -> Integer.compare(r1.getPriority(), r2.getPriority()))
                .map(this::generateRulePattern)
                .filter(pattern -> !pattern.isEmpty())
                .toList();

        if (rolePatterns.isEmpty()) {
            return "";
        }

        // Combine patterns with OR logic
        if (rolePatterns.size() == 1) {
            return rolePatterns.get(0);
        } else {
            return "(?:" + String.join("|", rolePatterns) + ")";
        }
    }

    /**
     * Generates a regex pattern for a single role rule.
     * 
     * @param rule the role rule
     * @return regex pattern for the rule
     */
    private String generateRulePattern(RoleRule rule) {
        if (rule.getRuleValue() == null || rule.getRuleValue().trim().isEmpty()) {
            return "";
        }

        String value = rule.getRuleValue().trim();
        String escapedValue = escapeRegexChars(value);

        // Handle case sensitivity
        String caseFlag = rule.isCaseSensitive() ? "" : "(?i:";
        String caseClose = rule.isCaseSensitive() ? "" : ")";

        return switch (rule.getRuleType()) {
            case EQUALS -> caseFlag + "^" + escapedValue + "$" + caseClose;
            case CONTAINS -> caseFlag + ".*" + escapedValue + ".*" + caseClose;
            case STARTS_WITH -> caseFlag + "^" + escapedValue + ".*" + caseClose;
            case ENDS_WITH -> caseFlag + ".*" + escapedValue + "$" + caseClose;
            case REGEX_OVERRIDE -> value; // Use as-is for regex override
        };
    }

    /**
     * Validates a pattern configuration for correctness and completeness.
     * 
     * @param config the pattern configuration to validate
     * @return validation result with errors and warnings
     */
    public ValidationResult validatePatterns(PatternConfiguration config) {
        if (config == null) {
            return ValidationResult.failure(
                    new ValidationError(ValidationErrorType.NO_GROUP_ID_SELECTED,
                            "Pattern configuration cannot be null"));
        }

        List<ValidationError> errors = new ArrayList<>();
        List<ValidationWarning> warnings = new ArrayList<>();

        // Validate group pattern
        validateGroupPattern(config, errors);

        // Validate role patterns
        validateRolePatterns(config, errors, warnings);

        // Validate role rules
        validateRoleRules(config, errors, warnings);

        // Validate regex syntax
        validateRegexSyntax(config, errors);

        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings);
    }

    /**
     * Validates the group pattern for capturing group requirements.
     */
    private void validateGroupPattern(PatternConfiguration config, List<ValidationError> errors) {
        String groupPattern = config.getGroupPattern();

        if (groupPattern == null || groupPattern.trim().isEmpty()) {
            if (config.getGroupIdToken() == null) {
                errors.add(new ValidationError(ValidationErrorType.NO_GROUP_ID_SELECTED,
                        "Please select a token to use as Group ID"));
            } else {
                errors.add(new ValidationError(ValidationErrorType.EMPTY_GROUP_PATTERN,
                        "Group pattern cannot be empty"));
            }
            return;
        }

        // Count capturing groups
        int capturingGroups = countCapturingGroups(groupPattern);

        if (capturingGroups == 0) {
            errors.add(new ValidationError(ValidationErrorType.NO_CAPTURING_GROUPS,
                    "Group pattern must contain exactly one capturing group",
                    "Ensure the Group ID token is properly selected"));
        } else if (capturingGroups > 1) {
            errors.add(new ValidationError(ValidationErrorType.MULTIPLE_CAPTURING_GROUPS,
                    "Group pattern contains " + capturingGroups + " capturing groups - only one is allowed",
                    "Remove extra parentheses or use non-capturing groups (?:...)"));
        }
    }

    /**
     * Validates role patterns for completeness.
     */
    private void validateRolePatterns(PatternConfiguration config,
            List<ValidationError> errors,
            List<ValidationWarning> warnings) {
        String frontPattern = config.getFrontPattern();
        String rearPattern = config.getRearPattern();
        String overviewPattern = config.getOverviewPattern();

        boolean hasFront = frontPattern != null && !frontPattern.trim().isEmpty();
        boolean hasRear = rearPattern != null && !rearPattern.trim().isEmpty();
        boolean hasOverview = overviewPattern != null && !overviewPattern.trim().isEmpty();

        if (!hasFront && !hasRear && !hasOverview) {
            errors.add(new ValidationError(ValidationErrorType.NO_ROLE_PATTERNS,
                    "At least one role pattern must be defined",
                    "Define rules for front, rear, or overview images"));
        }

        if (!hasOverview) {
            warnings.add(new ValidationWarning(ValidationWarningType.NO_OVERVIEW_IMAGES,
                    "No overview pattern defined - some images may not be categorized"));
        }
    }

    /**
     * Validates role rules for completeness and consistency.
     */
    private void validateRoleRules(PatternConfiguration config,
            List<ValidationError> errors,
            List<ValidationWarning> warnings) {
        List<RoleRule> rules = config.getRoleRules();

        // Check if we have role patterns instead of role rules (advanced mode)
        boolean hasRolePatterns = hasValidPattern(config.getFrontPattern()) ||
                hasValidPattern(config.getRearPattern()) ||
                hasValidPattern(config.getOverviewPattern());

        if ((rules == null || rules.isEmpty()) && !hasRolePatterns) {
            errors.add(new ValidationError(ValidationErrorType.NO_ROLE_RULES_DEFINED,
                    "Please define rules for identifying image roles"));
            return;
        }

        // If we have role patterns but no rules, that's valid for advanced mode
        if (rules == null || rules.isEmpty()) {
            return;
        }

        // Check for empty rule values
        for (RoleRule rule : rules) {
            if (rule.getRuleValue() == null || rule.getRuleValue().trim().isEmpty()) {
                errors.add(new ValidationError(ValidationErrorType.INVALID_RULE_VALUE,
                        "Rule value cannot be empty for " + rule.getTargetRole() + " role"));
            }
        }

        // Check for overlapping rules (warning only)
        checkForOverlappingRules(rules, warnings);
    }

    /**
     * Validates regex syntax for all patterns.
     */
    private void validateRegexSyntax(PatternConfiguration config, List<ValidationError> errors) {
        validateRegexSyntax(config.getGroupPattern(), "Group pattern", errors);
        validateRegexSyntax(config.getFrontPattern(), "Front pattern", errors);
        validateRegexSyntax(config.getRearPattern(), "Rear pattern", errors);
        validateRegexSyntax(config.getOverviewPattern(), "Overview pattern", errors);
    }

    /**
     * Validates the syntax of a single regex pattern.
     */
    private void validateRegexSyntax(String pattern, String patternName, List<ValidationError> errors) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return;
        }

        try {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            errors.add(new ValidationError(ValidationErrorType.REGEX_SYNTAX_ERROR,
                    patternName + " has invalid regex syntax: " + e.getMessage(),
                    "Check for unescaped special characters or unmatched parentheses"));
        }
    }

    /**
     * Counts the number of capturing groups in a regex pattern.
     */
    private int countCapturingGroups(String pattern) {
        if (pattern == null) {
            return 0;
        }

        int count = 0;
        boolean inCharClass = false;
        boolean escaped = false;

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '[') {
                inCharClass = true;
            } else if (c == ']') {
                inCharClass = false;
            } else if (!inCharClass && c == '(' && i + 1 < pattern.length()) {
                // Check if it's a non-capturing group
                if (pattern.charAt(i + 1) != '?') {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Checks for overlapping rules that might cause conflicts.
     */
    private void checkForOverlappingRules(List<RoleRule> rules, List<ValidationWarning> warnings) {
        // This is a simplified check - in practice, you might want more sophisticated
        // overlap detection
        for (int i = 0; i < rules.size(); i++) {
            for (int j = i + 1; j < rules.size(); j++) {
                RoleRule rule1 = rules.get(i);
                RoleRule rule2 = rules.get(j);

                if (rule1.getTargetRole() != rule2.getTargetRole() &&
                        rulesOverlap(rule1, rule2)) {
                    warnings.add(new ValidationWarning(ValidationWarningType.OVERLAPPING_RULES,
                            "Rules for " + rule1.getTargetRole() + " and " +
                                    rule2.getTargetRole() + " may overlap"));
                }
            }
        }
    }

    /**
     * Checks if two rules might overlap in their matching.
     */
    private boolean rulesOverlap(RoleRule rule1, RoleRule rule2) {
        // Simple overlap check - both rules use CONTAINS with similar values
        if (rule1.getRuleType() == RuleType.CONTAINS && rule2.getRuleType() == RuleType.CONTAINS) {
            String value1 = rule1.getRuleValue().toLowerCase();
            String value2 = rule2.getRuleValue().toLowerCase();
            return value1.contains(value2) || value2.contains(value1);
        }

        return false;
    }

    /**
     * Checks if a pattern string is valid (not null or empty).
     * 
     * @param pattern the pattern to check
     * @return true if the pattern is valid
     */
    private boolean hasValidPattern(String pattern) {
        return pattern != null && !pattern.trim().isEmpty();
    }

    /**
     * Escapes special regex characters in a string.
     */
    private String escapeRegexChars(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (REGEX_SPECIAL_CHARS.indexOf(c) >= 0) {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    /**
     * Checks if a value is an overview synonym.
     */
    private boolean isOverviewSynonym(String value) {
        return List.of("overview", "ov", "ovr", "ovw", "scene", "full").contains(value);
    }

    /**
     * Checks if a value is a front synonym.
     */
    private boolean isFrontSynonym(String value) {
        return List.of("front", "f", "fr", "forward").contains(value);
    }

    /**
     * Checks if a value is a rear synonym.
     */
    private boolean isRearSynonym(String value) {
        return List.of("rear", "r", "rr", "back", "behind").contains(value);
    }
}