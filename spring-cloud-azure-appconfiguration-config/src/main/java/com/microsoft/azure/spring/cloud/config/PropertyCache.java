/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;

public class PropertyCache {

    private Hashtable<String, CachedKey> cache;

    private List<String> refreshKeys;

    /**
     * @return the refreshKeys
     */
    public List<String> getRefreshKeys() {
        return refreshKeys;
    }

    public void setRefreshKeys(List<String> newRefreshKeys) {
        if (refreshKeys == null) {
            refreshKeys = new ArrayList<String>();
        }
        refreshKeys = newRefreshKeys;
    }

    /**
     * @param cache the cache to set
     */
    public void setCache(Hashtable<String, CachedKey> cache) {
        this.cache = cache;
    }

    public Hashtable<String, CachedKey> getCache() {
        return cache;
    }

    public void createNewCache() {
        cache = new Hashtable<String, CachedKey>();
        refreshKeys = new ArrayList<String>();
    }

    public void addToCache(String key, CachedKey value) {
        cache.put(key, value);
    }

    public void addKeyValuesToCache(List<KeyValueItem> items, String storeName) {
        Date date = new Date();
        for (KeyValueItem item : items) {
            CachedKey cachedKey = new CachedKey(item, storeName, date);
            cache.put(item.getKey(), cachedKey);
        }
    }

    public Set<String> getKeySet() {
        return cache.keySet();
    }

    public Object getCachedValue(String key) {
        return cache.get(key).getValue();
    }

    public List<String> findNonCachedKeys(Duration delay, String storeName) {
        refreshKeys = new ArrayList<String>();
        Date date = new Date();
        for (String key : cache.keySet()) {
            if (cache.get(key).getStoreName().equals(storeName)) {
                Date notCachedTime = DateUtils.addSeconds(cache.get(key).getLastUpdated(),
                        Math.toIntExact(delay.getSeconds()));
                if (date.after(notCachedTime)) {
                    refreshKeys.add(key);
                }
            }
        }
        return refreshKeys;
    }
    
    public void updateRefreshCacheTime() {
        Date date = new Date();
        for (String refreshKey: refreshKeys) {
            cache.get(refreshKey).setLastUpdated(date);
        }
        refreshKeys = new ArrayList<String>();
    }

}
