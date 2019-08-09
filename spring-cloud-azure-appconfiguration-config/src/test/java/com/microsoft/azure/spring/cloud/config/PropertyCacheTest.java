/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;

public class PropertyCacheTest {

    private static final String TEST_STORE_1 = "teststore1";

    private static final String TEST_STORE_2 = "teststore2";

    private PropertyCache propertyCache;

    @Before
    public void setup() {
        List<KeyValueItem> keys = new ArrayList<KeyValueItem>();

        KeyValueItem keyValueItem = new KeyValueItem();
        keyValueItem.setKey("TestKey1");
        keys.add(keyValueItem);
        keyValueItem = new KeyValueItem();
        keyValueItem.setKey("TestKey2");
        keys.add(keyValueItem);

        propertyCache = new PropertyCache();
        propertyCache.addKeyValuesToCache(keys, TEST_STORE_1);
    }

    @Test
    public void addKeyValuesToCacheTest() {
        assertEquals(2, propertyCache.getCache().size());
        assertEquals(2, propertyCache.getKeySet(TEST_STORE_1).size());
        assertEquals(0, propertyCache.getKeySet(TEST_STORE_2).size());
    }

    @Test
    public void findNonCachedKeysTest() {
        Duration delay = Duration.ofSeconds(2);
        
        List<String> refreshKeys = propertyCache.findNonCachedKeys(delay, TEST_STORE_1);
        assertEquals(0, refreshKeys.size());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail("Wait failed between cached keys test.");
        }
        
        refreshKeys = propertyCache.findNonCachedKeys(delay, TEST_STORE_1);
        assertEquals(2, refreshKeys.size());
    }

}
