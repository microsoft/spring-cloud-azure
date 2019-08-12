/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;

public class PropertyCacheTest {

    private static final String TEST_STORE_1 = "teststore1";

    private static final String TEST_STORE_2 = "teststore2";

    private static final String TEST_KEY_1 = "TestKey1";

    private static final String TEST_KEY_2 = "TestKey2";

    private PropertyCache propertyCache;

    @Mock
    private Date date;

    @Mock
    private Duration delay;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        List<KeyValueItem> keys = new ArrayList<KeyValueItem>();

        KeyValueItem keyValueItem = new KeyValueItem();
        keyValueItem.setKey(TEST_KEY_1);
        keys.add(keyValueItem);
        keyValueItem = new KeyValueItem();
        keyValueItem.setKey(TEST_KEY_2);
        keys.add(keyValueItem);

        propertyCache = new PropertyCache();
        propertyCache.addKeyValuesToCache(keys, TEST_STORE_1, new Date());
    }

    @Test
    public void addKeyValuesToCacheTest() {
        assertEquals(2, propertyCache.getCache().size());
        assertEquals(2, propertyCache.getKeySet(TEST_STORE_1).size());
        assertEquals(0, propertyCache.getKeySet(TEST_STORE_2).size());
    }

    @Test
    public void findNonCachedKeysTest() {
        when(delay.getSeconds()).thenReturn(new Long(1000));
        List<String> refreshKeys = propertyCache.findNonCachedKeys(delay, TEST_STORE_1);
        assertEquals(0, refreshKeys.size());
        when(delay.getSeconds()).thenReturn(new Long(0));
        refreshKeys = propertyCache.findNonCachedKeys(delay, TEST_STORE_1);
        assertEquals(2, refreshKeys.size());
    }

    @Test
    public void updateRefreshCacheTimeForKeyTest() {
        propertyCache.findNonCachedKeys(Duration.ofSeconds(1), TEST_STORE_1);
        assertTrue(propertyCache.getCache().get(TEST_KEY_1).getLastUpdated().getClass() == Date.class);

        List<String> refreshKeys = propertyCache.updateRefreshCacheTimeForKey(TEST_STORE_1, TEST_KEY_1, date);
        assertEquals(date, propertyCache.getCache().get(TEST_KEY_1).getLastUpdated());
        assertEquals(0, refreshKeys.size());
        assertEquals(0, propertyCache.getCache().get(TEST_KEY_1).getLastUpdated().getTime());
    }

}
