/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;

import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;

public class PropertyCache {

    private ConcurrentMap<String, CachedKey> cache;

    private ConcurrentMap<String, List<String>> refreshKeys;

    private ConcurrentHashMap<String, String> contextLookup;

    public PropertyCache() {
        cache = new ConcurrentHashMap<String, CachedKey>();
        refreshKeys = new ConcurrentHashMap<String, List<String>>();
        contextLookup = new ConcurrentHashMap<String, String>();
    }

    /**
     * @return the refreshKeys
     */
    public List<String> getRefreshKeys(String storeName) {
        return refreshKeys.get(storeName);
    }
    
    public List<String> getRefreshKeys(String storeName, String filter) {
        return refreshKeys.get(storeName).stream().filter(key -> key.startsWith(filter)).collect(Collectors.toList());
    }

    public ConcurrentMap<String, CachedKey> getCache() {
        return cache;
    }

    public void addToCache(KeyValueItem item, String storeName, Date date) {
        cache.put(item.getKey(), new CachedKey(item, storeName, date));
    }

    public void addKeyValuesToCache(List<KeyValueItem> items, String storeName, Date date) {
        ConcurrentMap<String, CachedKey> newCacheItems = items.stream()
                .map(item -> new CachedKey(item, storeName, date))
                .collect(Collectors.toConcurrentMap(item -> item.getKey(), item -> item));
        cache.putAll(newCacheItems);
    }

    public Set<String> getKeySet(String storeName) {
        return cache.keySet().stream().filter(string -> cache.get(string).getStoreName().equals(storeName))
                .collect(Collectors.toSet());
    }

    public String getCachedValue(String key) {
        return cache.get(key).getValue();
    }

    public String getCachedEtag(String key) {
        return cache.get(key).getEtag();
    }

    public List<String> findNonCachedKeys(Duration delay, String storeName) {
        ArrayList<String> storeRefreshKeys = new ArrayList<String>();
        Date date = new Date();

        for (String key : getKeySet(storeName)) {
            Date notCachedTime = DateUtils.addSeconds(cache.get(key).getLastUpdated(),
                    Math.toIntExact(delay.getSeconds()));
            if (date.after(notCachedTime)) {
                storeRefreshKeys.add(key);
            }
        }
        refreshKeys.put(storeName, storeRefreshKeys);
        return storeRefreshKeys;
    }

    public void updateRefreshCacheTime(String storeName, String filter, Duration delay) {
        Date date = new Date();
        if (refreshKeys.get(storeName) == null) {
            return;
        }
        refreshKeys.get(storeName).stream().filter(key -> key.contains(filter))
                .forEach(key -> cache.get(key).setLastUpdated(date));
        findNonCachedKeys(delay, storeName);
    }

    public List<String> updateRefreshCacheTimeForKey(String storeName, String key, Date date) {
        cache.get(key).setLastUpdated(date);
        refreshKeys.get(storeName).remove(key);
        return refreshKeys.get(storeName);
    }

    public void addContext(String storeName, String context) {
        contextLookup.put(storeName, context);
    }

    public String getContext(String storeName) {
        return contextLookup.get(storeName);
    }

}
