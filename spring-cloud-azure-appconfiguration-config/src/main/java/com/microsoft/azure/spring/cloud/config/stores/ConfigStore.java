/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import static com.microsoft.azure.spring.cloud.config.AzureCloudConfigProperties.LABEL_SEPARATOR;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.microsoft.azure.spring.cloud.config.resource.ConnectionString;

public class ConfigStore {
    private static final String[] EMPTY_LABEL_ONLY = {"\0"};
    private String name; // Config store name

    @Nullable
    @Pattern(regexp = "(/[a-zA-Z0-9.\\-_]+)*")
    private String prefix;

    private String connectionString;

    // Label values separated by comma in the Azure Config Service, can be empty
    @Nullable
    private String label;

    // The keys to be watched, won't take effect if watch not enabled
    @NotEmpty
    private String watchedKey = "*";

    public ConfigStore() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getWatchedKey() {
        return watchedKey;
    }

    public void setWatchedKey(String watchedKey) {
        this.watchedKey = watchedKey;
    }

    @PostConstruct
    public void validateAndInit() {
        if (StringUtils.hasText(label)) {
            Assert.isTrue(!label.contains("*"), "Label must not contain asterisk(*).");
        }

        if (StringUtils.hasText(connectionString)) {
            String endpoint = ConnectionString.of(connectionString).getEndpoint();
            try {
                URI uri = new URI(endpoint);
                this.name = uri.getHost().split("\\.")[0];
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Endpoint in connection string is not a valid URI.", e);
            }
        }

        Assert.isTrue(watchedKeyValid(this.watchedKey), "Watched key can only be a single asterisk(*) or " +
                "a specific key without asterisk(*)");
    }

    private boolean watchedKeyValid(String watchedKey) {
        if (!StringUtils.hasText(watchedKey)) {
            return false;
        }

        String trimmedKey = watchedKey.trim();
        // Watched key can either be single asterisk(*) or a specific key without asterisk(*)
        return trimmedKey.equals("*") || !trimmedKey.contains("*");
    }

    /**
     * @return List of reversed label values, which are split by the separator, the latter label has higher priority
     */
    public String[] getLabels() {
        if (!StringUtils.hasText(this.getLabel())) {
            return EMPTY_LABEL_ONLY;
        }

        List<String> labels =  Arrays.stream(this.getLabel().split(LABEL_SEPARATOR))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        Collections.reverse(labels);
        if (labels.isEmpty()) {
            return EMPTY_LABEL_ONLY;
        } else {
            String[] t = new String[labels.size()];
            return labels.toArray(t);
        }
    }
}
