/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Credentials to authenticate with AAD. Support below authentication approaches:
 * - Certificate
 * - Secret
 * TODO:
 * - MSI support
 * - Extract to autoconfigure package and reuse by other services
 */
@Getter
@Setter
@Validated
public class Credentials {

    @NotEmpty
    private String clientId;

    private String clientSecret;

    private Resource clientCertificate;
}
