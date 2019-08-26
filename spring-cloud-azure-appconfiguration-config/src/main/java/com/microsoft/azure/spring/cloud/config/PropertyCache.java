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

import com.azure.data.appconfiguration.models.ConfigurationSetting;

public class PropertyCache {

    private ConcurrentMap<String, CachedKey> cache;

    private ConcurrentMap<String, List<String>> refreshKeys;

    private ConcurrentHashMap<String, String> contextLookup;
    
    private static PropertyCache propertyCache;

    private static PropertyCache propertyCache;

    private PropertyCache() {
        cache = new ConcurrentHashMap<String, CachedKey>();
        refreshKeys = new ConcurrentHashMap<String, List<String>>();
        contextLookup = new ConcurrentHashMap<String, String>();
    }
    
    public static PropertyCache getPropertyCache() {
        if (propertyCache == null) {
            propertyCache = new PropertyCache();
        }
        return propertyCache;
    }
    
    public static PropertyCache resetPropertyCache() {
        propertyCache = new PropertyCache();
        return propertyCache;
    }

    /**
     * Gets a List of keys that need to refreshed by the given store
     * 
     * @param storeName Store of which the refreshed keys are from
     * @return the refreshKeys
     */
    public List<String> getRefreshKeys(String storeName) {
        return refreshKeys.get(storeName);
    }
    
    public boolean hasRefreshKeys(String storeName) {
        return refreshKeys.get(storeName) != null && refreshKeys.get(storeName).size() > 0;
    }
    
    /**
     * Gets a List of keys that need to refreshed by the given store, and there key starts with the filtered value
     * 
     * @param storeName Store of which the refreshed keys are from
     * @param filter Filter used to find a specific set of keys
     * @return
     */
    public List<String> getRefreshKeys(String storeName, String filter) {
        return refreshKeys.get(storeName).stream().filter(key -> key.startsWith(filter)).collect(Collectors.toList());
    }

    /**
     * @return the cache
     */
    public ConcurrentMap<String, CachedKey> getCache() {
        return cache;
    }

    /**
     * Adds a new KeyValueItem to the cache.
     * 
     * @param item KeyValueItem to be added to the cache
     * @param storeName Store the key is from
     * @param date current date, used for checking next refresh time
     */
    public void addToCache(ConfigurationSetting item, String storeName, Date date) {
        cache.put(item.key(), new CachedKey(item, storeName, date));
    }

    /**
     * Adds new KeyValueItems to the cache.
     * 
     * @param items KeyValueItem to be added to the cache
     * @param storeName Store the key is from
     * @param date current date, used for checking next refresh time
     */
    public void addKeyValuesToCache(List<ConfigurationSetting> items, String storeName, Date date) {
        ConcurrentMap<String, CachedKey> newCacheItems = items.stream()
                .map(item -> new CachedKey(item, storeName, date))
                .collect(Collectors.toConcurrentMap(item -> item.getKey(), item -> item));
        cache.putAll(newCacheItems);
    }

    /**
     * Returns a list of the cached keys from the cache from the given store.
     * 
     * @param storeName Store from which the keys are from.
     * @return the List of keys from the given store
     */
    public Set<String> getKeySet(String storeName) {
        return cache.keySet().stream().filter(string -> cache.get(string).getStoreName().equals(storeName))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the value of the cached key.
     * 
     * @param key cached key.
     * @return the cached key. Null if key isn't found.
     */
    public String getCachedValue(String key) {
        CachedKey cachedKey = cache.get(key);
        if (cachedKey == null) {
            return null;
        }
        return cachedKey.getValue();
    }

    /**
     * Returns the etag of the cached key.
     * 
     * @param key cached key.
     * @return the etag of the cached key. Null if key isn't found.
     */
    public String getCachedEtag(String key) {
        CachedKey cachedKey = cache.get(key);
        if (cachedKey == null) {
            return null;
        }
        return cachedKey.getEtag();
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

    public List<String> updateRefreshCacheTime(String storeName, String filter, Duration delay) {
        Date date = new Date();
        if (refreshKeys.get(storeName) == null) {
            return findNonCachedKeys(delay, storeName);
        }
        refreshKeys.get(storeName).stream().filter(key -> key.contains(filter))
                .forEach(key -> cache.get(key).setLastUpdated(date));
        return findNonCachedKeys(delay, storeName);
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
