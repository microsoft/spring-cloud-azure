/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AutoConfigureUtils {

    private static final String AUTO_CONFIG_SUFFIX = "AutoConfiguration";

    public static String getServiceName(@NonNull Class configClass) {
        Assert.notNull(configClass.getAnnotation(Configuration.class), "should be @Configuration class");
        Assert.isTrue(configClass.getSimpleName().contains(AUTO_CONFIG_SUFFIX), "should contain auto config suffix");

        return configClass.getSimpleName().replace(AUTO_CONFIG_SUFFIX, "");
    }
}
