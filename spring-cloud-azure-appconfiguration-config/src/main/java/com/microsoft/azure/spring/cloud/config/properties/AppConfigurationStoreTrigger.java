package com.microsoft.azure.spring.cloud.config.properties;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

public class AppConfigurationStoreTrigger {

    private String key;

    private String label;

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @PostConstruct
    public void validateAndInit() {
        Assert.notNull(key, "All Triggers need a key value set.");
        Assert.notNull(label, "All Triggers need a label value set, (No Label) can be found using '\0' as the label.");
    }

}
