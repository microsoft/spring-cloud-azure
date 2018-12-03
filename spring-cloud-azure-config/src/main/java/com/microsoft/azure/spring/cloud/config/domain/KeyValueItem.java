/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyValueItem {
    private String etag;

    private String key;

    private String value;

    private String label;

    @JsonProperty("content_type")
    private String contentType;

    private Map<String, String> tags;

    private boolean locked;

    @JsonProperty("last_modified")
    private Date lastModified;
}
