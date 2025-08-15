package com.smartcloudsolutions.tollingvision.samples.model;

import com.smartcloudsolutions.tollingvision.samples.patternbuilder.PatternBuilderConfig;

/**
 * User configuration data model for the TollingVision application.
 * Contains all user-configurable settings that should be persisted.
 */
public class UserConfiguration {
    
    private String inputFolder = "";
    private String serviceUrl = "localhost:50051";
    private boolean tlsEnabled = true;
    private boolean insecureAllowed = false;
    private String csvDirectory = "";
    private int maxParallel = 4;
    private String groupPattern = "^.{7}";
    private String frontPattern = ".*front.*";
    private String rearPattern = ".*rear.*";
    private String overviewPattern = ".*scene.*";
    private PatternBuilderConfig patternBuilderConfig;
    
    /**
     * Creates a new UserConfiguration with default values.
     */
    public UserConfiguration() {
        this.patternBuilderConfig = new PatternBuilderConfig();
    }
    
    /**
     * Creates a new UserConfiguration with the specified values.
     */
    public UserConfiguration(String inputFolder, String serviceUrl, boolean tlsEnabled, 
                           boolean insecureAllowed, String csvDirectory, int maxParallel,
                           String groupPattern, String frontPattern, String rearPattern, 
                           String overviewPattern, PatternBuilderConfig patternBuilderConfig) {
        this.inputFolder = inputFolder;
        this.serviceUrl = serviceUrl;
        this.tlsEnabled = tlsEnabled;
        this.insecureAllowed = insecureAllowed;
        this.csvDirectory = csvDirectory;
        this.maxParallel = maxParallel;
        this.groupPattern = groupPattern;
        this.frontPattern = frontPattern;
        this.rearPattern = rearPattern;
        this.overviewPattern = overviewPattern;
        this.patternBuilderConfig = patternBuilderConfig != null ? patternBuilderConfig : new PatternBuilderConfig();
    }
    
    // Getters
    public String getInputFolder() { return inputFolder; }
    public String getServiceUrl() { return serviceUrl; }
    public boolean isTlsEnabled() { return tlsEnabled; }
    public boolean isInsecureAllowed() { return insecureAllowed; }
    public String getCsvDirectory() { return csvDirectory; }
    public int getMaxParallel() { return maxParallel; }
    public String getGroupPattern() { return groupPattern; }
    public String getFrontPattern() { return frontPattern; }
    public String getRearPattern() { return rearPattern; }
    public String getOverviewPattern() { return overviewPattern; }
    public PatternBuilderConfig getPatternBuilderConfig() { return patternBuilderConfig; }
    
    // Setters
    public void setInputFolder(String inputFolder) { this.inputFolder = inputFolder; }
    public void setServiceUrl(String serviceUrl) { this.serviceUrl = serviceUrl; }
    public void setTlsEnabled(boolean tlsEnabled) { this.tlsEnabled = tlsEnabled; }
    public void setInsecureAllowed(boolean insecureAllowed) { this.insecureAllowed = insecureAllowed; }
    public void setCsvDirectory(String csvDirectory) { this.csvDirectory = csvDirectory; }
    public void setMaxParallel(int maxParallel) { this.maxParallel = maxParallel; }
    public void setGroupPattern(String groupPattern) { this.groupPattern = groupPattern; }
    public void setFrontPattern(String frontPattern) { this.frontPattern = frontPattern; }
    public void setRearPattern(String rearPattern) { this.rearPattern = rearPattern; }
    public void setOverviewPattern(String overviewPattern) { this.overviewPattern = overviewPattern; }
    public void setPatternBuilderConfig(PatternBuilderConfig patternBuilderConfig) { 
        this.patternBuilderConfig = patternBuilderConfig != null ? patternBuilderConfig : new PatternBuilderConfig(); 
    }
    
    /**
     * Creates a builder for constructing UserConfiguration instances.
     * 
     * @return a new builder instance
     */
    public static Builder newBuilder() {
        return new Builder();
    }
    
    /**
     * Creates a builder initialized with this configuration's values.
     * 
     * @return a new builder instance with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }
    
    /**
     * Builder class for UserConfiguration.
     */
    public static class Builder {
        private UserConfiguration config;
        
        public Builder() {
            this.config = new UserConfiguration();
        }
        
        public Builder(UserConfiguration existing) {
            this.config = new UserConfiguration(
                existing.inputFolder, existing.serviceUrl, existing.tlsEnabled,
                existing.insecureAllowed, existing.csvDirectory, existing.maxParallel,
                existing.groupPattern, existing.frontPattern, existing.rearPattern,
                existing.overviewPattern, existing.patternBuilderConfig != null ? 
                existing.patternBuilderConfig.copy() : new PatternBuilderConfig()
            );
        }
        
        public Builder setInputFolder(String inputFolder) {
            config.inputFolder = inputFolder;
            return this;
        }
        
        public Builder setServiceUrl(String serviceUrl) {
            config.serviceUrl = serviceUrl;
            return this;
        }
        
        public Builder setTlsEnabled(boolean tlsEnabled) {
            config.tlsEnabled = tlsEnabled;
            return this;
        }
        
        public Builder setInsecureAllowed(boolean insecureAllowed) {
            config.insecureAllowed = insecureAllowed;
            return this;
        }
        
        public Builder setCsvDirectory(String csvDirectory) {
            config.csvDirectory = csvDirectory;
            return this;
        }
        
        public Builder setMaxParallel(int maxParallel) {
            config.maxParallel = maxParallel;
            return this;
        }
        
        public Builder setGroupPattern(String groupPattern) {
            config.groupPattern = groupPattern;
            return this;
        }
        
        public Builder setFrontPattern(String frontPattern) {
            config.frontPattern = frontPattern;
            return this;
        }
        
        public Builder setRearPattern(String rearPattern) {
            config.rearPattern = rearPattern;
            return this;
        }
        
        public Builder setOverviewPattern(String overviewPattern) {
            config.overviewPattern = overviewPattern;
            return this;
        }
        
        public Builder setPatternBuilderConfig(PatternBuilderConfig patternBuilderConfig) {
            config.patternBuilderConfig = patternBuilderConfig != null ? patternBuilderConfig : new PatternBuilderConfig();
            return this;
        }
        
        public UserConfiguration build() {
            return new UserConfiguration(
                config.inputFolder, config.serviceUrl, config.tlsEnabled,
                config.insecureAllowed, config.csvDirectory, config.maxParallel,
                config.groupPattern, config.frontPattern, config.rearPattern,
                config.overviewPattern, config.patternBuilderConfig
            );
        }
    }
}