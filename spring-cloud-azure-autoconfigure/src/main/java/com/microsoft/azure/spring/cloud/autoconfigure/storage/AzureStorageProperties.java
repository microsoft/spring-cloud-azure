/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author Warren Zhu
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("spring.cloud.azure.storage")
public class AzureStorageProperties {

    @NotEmpty
    @Pattern(regexp = "^[a-z0-9]{3,24}$",
            message = "must be between 3 and 24 characters in length and use numbers and lower-case letters only")
    private String account;
}
