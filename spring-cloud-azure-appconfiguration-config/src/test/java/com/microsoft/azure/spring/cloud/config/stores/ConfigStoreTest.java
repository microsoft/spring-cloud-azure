/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.stores;

import static org.junit.Assert.fail;

import org.junit.Test;

public class ConfigStoreTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidLabel() {
        ConfigStore configStore = new ConfigStore();
        configStore.setLabel("*");
        configStore.validateAndInit();
        fail();
    }

    @Test(expected = IllegalStateException.class)
    public void invalidEndpoint() {
        ConfigStore configStore = new ConfigStore();
        configStore.setConnectionString("Endpoint=a^a;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==");
        configStore.validateAndInit();
        fail();
    }

}
