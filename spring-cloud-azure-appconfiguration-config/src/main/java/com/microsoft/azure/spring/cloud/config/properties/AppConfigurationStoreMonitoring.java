package com.microsoft.azure.spring.cloud.config.properties;

import java.time.Duration;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

public class AppConfigurationStoreMonitoring {

    private boolean enabled = false;

    private Duration cacheExpiration = Duration.ofSeconds(30);

    private List<AppConfigurationStoreTrigger> triggers;

    @Value("pushNotification.primaryToken.name")
    private String primaryTokenName;

    @Value("pushNotification.primaryToken.secret")
    private String primaryTokenSecret;

    @Value("pushNotification.secondaryToken.name")
    private String secondaryTokenName;

    @Value("pushNotification.secondaryToken.secret")
    private String secondaryTokenSecret;

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the cacheExpiration
     */
    public Duration getCacheExpiration() {
        return cacheExpiration;
    }

    /**
     * The minimum time between checks. The minimum valid cache time is 1s. The default
     * cache time is 30s.
     * 
     * @param cacheExpiration minimum time between refresh checks
     */
    public void setCacheExpiration(Duration cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }

    /**
     * @return the triggers
     */
    public List<AppConfigurationStoreTrigger> getTriggers() {
        return triggers;
    }

    /**
     * @param triggers the triggers to set
     */
    public void setTriggers(List<AppConfigurationStoreTrigger> triggers) {
        this.triggers = triggers;
    }

    /**
     * @return the primaryTokenName
     */
    public String getPrimaryTokenName() {
        return primaryTokenName;
    }

    /**
     * @param primaryTokenName the primaryTokenName to set
     */
    public void setPrimaryTokenName(String primaryTokenName) {
        this.primaryTokenName = primaryTokenName;
    }

    /**
     * @return the primaryTokenSecret
     */
    public String getPrimaryTokenSecret() {
        return primaryTokenSecret;
    }

    /**
     * @param primaryTokenSecret the primaryTokenSecret to set
     */
    public void setPrimaryTokenSecret(String primaryTokenSecret) {
        this.primaryTokenSecret = primaryTokenSecret;
    }

    /**
     * @return the secondaryTokenName
     */
    public String getSecondaryTokenName() {
        return secondaryTokenName;
    }

    /**
     * @param secondaryTokenName the secondaryTokenName to set
     */
    public void setSecondaryTokenName(String secondaryTokenName) {
        this.secondaryTokenName = secondaryTokenName;
    }

    /**
     * @return the secondaryTokenSecret
     */
    public String getSecondaryTokenSecret() {
        return secondaryTokenSecret;
    }

    /**
     * @param secondaryTokenSecret the secondaryTokenSecret to set
     */
    public void setSecondaryTokenSecret(String secondaryTokenSecret) {
        this.secondaryTokenSecret = secondaryTokenSecret;
    }
    
    @PostConstruct
    public void validateAndInit() {
        if (enabled) {
            Assert.notEmpty(triggers, "Triggers need to be set if refresh is enabled.");
        }
        Assert.isTrue(cacheExpiration.getSeconds() >= 1, "Minimum Watch time is 1 Second.");
    }

}
