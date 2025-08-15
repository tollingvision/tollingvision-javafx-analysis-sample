package com.smartcloudsolutions.tollingvision.samples.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.smartcloudsolutions.tollingvision.samples.model.UserConfiguration;
import com.smartcloudsolutions.tollingvision.samples.patternbuilder.PatternBuilderConfig;
import com.smartcloudsolutions.tollingvision.samples.patternbuilder.PatternConfiguration;

/**
 * Integration tests for Pattern Builder functionality.
 * Tests the integration between UserConfiguration and PatternBuilderConfig.
 */
class PatternBuilderIntegrationTest {

    @Test
    void testUserConfigurationWithPatternBuilderConfig() {
        UserConfiguration config = new UserConfiguration();
        
        assertNotNull(config.getPatternBuilderConfig(), "PatternBuilderConfig should be initialized");
        
        PatternBuilderConfig builderConfig = new PatternBuilderConfig();
        builderConfig.setLastUsedDirectory("/test/directory");
        builderConfig.setAutoGeneratePatterns(false);
        
        config.setPatternBuilderConfig(builderConfig);
        
        assertEquals("/test/directory", config.getPatternBuilderConfig().getLastUsedDirectory());
        assertFalse(config.getPatternBuilderConfig().isAutoGeneratePatterns());
    }

    @Test
    void testUserConfigurationBuilder() {
        PatternBuilderConfig builderConfig = new PatternBuilderConfig();
        builderConfig.setLastUsedDirectory("/test/directory");
        
        UserConfiguration config = UserConfiguration.newBuilder()
                .setInputFolder("/input")
                .setServiceUrl("localhost:50051")
                .setGroupPattern("^(.{7})")
                .setFrontPattern(".*front.*")
                .setPatternBuilderConfig(builderConfig)
                .build();
        
        assertEquals("/input", config.getInputFolder());
        assertEquals("localhost:50051", config.getServiceUrl());
        assertEquals("^(.{7})", config.getGroupPattern());
        assertEquals(".*front.*", config.getFrontPattern());
        assertNotNull(config.getPatternBuilderConfig());
        assertEquals("/test/directory", config.getPatternBuilderConfig().getLastUsedDirectory());
    }

    @Test
    void testPatternBuilderConfigCopy() {
        PatternBuilderConfig original = new PatternBuilderConfig();
        original.setLastUsedDirectory("/test/directory");
        original.setAutoGeneratePatterns(false);
        
        PatternBuilderConfig copy = original.copy();
        
        assertEquals(original.getLastUsedDirectory(), copy.getLastUsedDirectory());
        assertEquals(original.isAutoGeneratePatterns(), copy.isAutoGeneratePatterns());
        
        // Verify it's a deep copy
        copy.setLastUsedDirectory("/different/directory");
        assertNotEquals(original.getLastUsedDirectory(), copy.getLastUsedDirectory());
    }

    @Test
    void testPatternBuilderConfigToFromPatternConfiguration() {
        PatternBuilderConfig builderConfig = new PatternBuilderConfig();
        PatternConfiguration patternConfig = builderConfig.toPatternConfiguration();
        
        assertNotNull(patternConfig);
        assertNotNull(patternConfig.getRoleRules());
        assertNotNull(patternConfig.getTokens());
        
        // Test round-trip conversion
        PatternBuilderConfig newBuilderConfig = new PatternBuilderConfig();
        newBuilderConfig.fromPatternConfiguration(patternConfig);
        
        assertEquals(builderConfig.getRoleRules().size(), newBuilderConfig.getRoleRules().size());
        assertEquals(builderConfig.getTokens().size(), newBuilderConfig.getTokens().size());
    }

    @Test
    void testBackwardCompatibility() {
        // Test that existing configurations work with new PatternBuilderConfig
        UserConfiguration config = UserConfiguration.newBuilder()
                .setInputFolder("/input")
                .setGroupPattern("^(.{7})")
                .setFrontPattern(".*front.*")
                .build();
        
        // PatternBuilderConfig should be automatically initialized
        assertNotNull(config.getPatternBuilderConfig());
        assertTrue(config.getPatternBuilderConfig().isAutoGeneratePatterns());
        assertEquals("", config.getPatternBuilderConfig().getLastUsedDirectory());
    }

    @Test
    void testPatternValidation() {
        // Test basic pattern validation logic
        String validGroupPattern = "^(.{7})";
        String validFrontPattern = ".*front.*";
        String invalidPattern = "[invalid";
        
        // Valid patterns should compile without exception
        assertDoesNotThrow(() -> {
            java.util.regex.Pattern.compile(validGroupPattern);
            java.util.regex.Pattern.compile(validFrontPattern);
        });
        
        // Invalid pattern should throw exception
        assertThrows(java.util.regex.PatternSyntaxException.class, () -> {
            java.util.regex.Pattern.compile(invalidPattern);
        });
    }

    @Test
    void testPatternConfigurationValidation() {
        PatternConfiguration config = new PatternConfiguration();
        
        // Empty configuration should be invalid
        assertFalse(config.isValid());
        
        // Configuration with only group pattern should be invalid
        config.setGroupPattern("^(.{7})");
        assertFalse(config.isValid());
        
        // Configuration with group pattern and at least one role pattern should be valid
        config.setFrontPattern(".*front.*");
        assertTrue(config.isValid());
        
        // Configuration with empty group pattern should be invalid
        config.setGroupPattern("");
        assertFalse(config.isValid());
        
        // Configuration with whitespace-only group pattern should be invalid
        config.setGroupPattern("   ");
        assertFalse(config.isValid());
    }
}