/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroup;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.storage.StorageAccount;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.util.function.Function;

public class AzureAdmin {

    private final Azure azure;
    private final String resourceGroup;
    private final String region;

    public AzureAdmin(@NonNull Azure azure, String resourceGroup, String region) {
        Assert.hasText(resourceGroup, "resourceGroup can't be null or empty");
        Assert.hasText(region, "region can't be null or empty");
        this.azure = azure;
        this.resourceGroup = resourceGroup;
        this.region = region;
        this.getOrCreateResourceGroup(resourceGroup);
    }

    public EventHub getOrCreateEventHub(String namespace, String name) {
        return getOrCreate(this::getEventHub, this::createEventHub).apply(Tuple.of(namespace, name));
    }

    public EventHub getEventHub(Tuple<String, String> namespaceAndName) {
        return azure.eventHubs().getByName(resourceGroup, namespaceAndName.getFirst(), namespaceAndName.getSecond());
    }

    private EventHub createEventHub(Tuple<String, String> namespaceAndName) {
        EventHubNamespace eventHubNamespace = getOrCreateEventHubNamespace(namespaceAndName.getFirst());

        return azure.eventHubs().define(namespaceAndName.getSecond()).withExistingNamespace(eventHubNamespace).create();
    }

    public EventHubNamespace getOrCreateEventHubNamespace(String namespace) {
        return getOrCreate(this::getEventHubNamespace, this::createEventHubNamespace).apply(namespace);
    }

    private EventHubNamespace getEventHubNamespace(String namespace) {
        try {
            return azure.eventHubNamespaces().getByResourceGroup(resourceGroup, namespace);
        } catch (NullPointerException e) {
            // azure management api has no way to determine whether an eventhub namespace exists
            // Workaround for this is by catching NPE
            return null;
        }
    }

    private EventHubNamespace createEventHubNamespace(String namespace) {
        return azure.eventHubNamespaces().define(namespace).withRegion(region).withExistingResourceGroup(resourceGroup)
                    .create();
    }

    public StorageAccount getOrCreateStorageAccount(String name) {
        return getOrCreate(this::getStorageAccount, this::createStorageAccount).apply(name);
    }

    private StorageAccount getStorageAccount(String name) {
        return azure.storageAccounts().getByResourceGroup(resourceGroup, name);
    }

    private StorageAccount createStorageAccount(String name) {
        return azure.storageAccounts().define(name).withRegion(region).withExistingResourceGroup(resourceGroup)
                    .create();
    }

    private ResourceGroup getOrCreateResourceGroup(String resourceGroup) {
        return getOrCreate(this::getResourceGroup, this::createResourceGroup).apply(resourceGroup);
    }

    private ResourceGroup getResourceGroup(String resourceGroup) {
        return azure.resourceGroups().getByName(resourceGroup);
    }

    private ResourceGroup createResourceGroup(String resourceGroup) {
        return azure.resourceGroups().define(resourceGroup).withRegion(region).create();
    }

    public EventHubConsumerGroup getOrCreateEventHubConsumerGroup(String namespace, String name, String group) {
        EventHubConsumerGroup consumerGroup = getEventHubConsumerGroup(namespace, name, group);

        if (consumerGroup != null) {
            return consumerGroup;
        }

        return createEventHubConsumerGroup(namespace, name, group);
    }

    private EventHubConsumerGroup getEventHubConsumerGroup(String namespace, String name, String group) {
        return azure.eventHubs().getByName(resourceGroup, namespace, name).listConsumerGroups().stream()
                    .filter(c -> c.namespaceResourceGroupName().equals(group)).findAny().orElse(null);
    }

    private EventHubConsumerGroup createEventHubConsumerGroup(String namespace, String name, String group) {
        return azure.eventHubs().consumerGroups().define(group).withExistingEventHub(resourceGroup, namespace, name)
                    .create();
    }

    public SqlDatabase getOrCreateSqlDatabase(String sqlServerName, String databaseName) {
        SqlDatabase sqlDatabase = getSqlDatabase(sqlServerName, databaseName);
        if (sqlDatabase != null) {
            return sqlDatabase;
        }

        return azure.sqlServers().databases().define(databaseName)
                    .withExistingSqlServer(resourceGroup, sqlServerName, region).create();
    }

    private SqlServer createSqlServer(String sqlServerName, String username, String password) {
        return azure.sqlServers().define(sqlServerName).withRegion(region).withExistingResourceGroup(resourceGroup)
                    .withAdministratorLogin(username).withAdministratorPassword(password).create();
    }

    public SqlServer getOrCreateSqlServer(String sqlServerName, String username, String password) {
        SqlServer sqlServer = getSqlServer(sqlServerName);

        if (sqlServer != null) {
            return sqlServer;
        }

        return createSqlServer(sqlServerName, username, password);
    }

    public SqlServer getSqlServer(String sqlServerName) {
        return azure.sqlServers().getByResourceGroup(resourceGroup, sqlServerName);
    }

    private SqlDatabase getSqlDatabase(String sqlServerName, String databaseName) {
        return azure.sqlServers().databases().getBySqlServer(resourceGroup, sqlServerName, databaseName);
    }

    public ServiceBusNamespace getOrCreateServiceBusNamespace(String namespace) {
        return getOrCreate(this::getServiceBusNamespace, this::createServiceBusNamespace).apply(namespace);
    }

    private ServiceBusNamespace getServiceBusNamespace(String namespace) {
        try {
            return azure.serviceBusNamespaces().getByResourceGroup(resourceGroup, namespace);
        } catch (NullPointerException ignore) {
            // azure management api has no way to determine whether an service bus namespace exists
            // Workaround for this is by catching NPE
            return null;
        }
    }

    private ServiceBusNamespace createServiceBusNamespace(String namespace) {
        return azure.serviceBusNamespaces().define(namespace).withRegion(region)
                    .withExistingResourceGroup(resourceGroup).create();
    }

    public Topic getOrCreateServiceBusTopic(ServiceBusNamespace namespace, String name) {
        return getOrCreate(this::getServiceBusTopic, this::createServiceBusTopic).apply(Tuple.of(namespace, name));
    }

    public Queue getOrCreateServiceBusQueue(ServiceBusNamespace namespace, String name) {
        return getOrCreate(this::getServiceBusQueue, this::createServiceBusQueue).apply(Tuple.of(namespace, name));
    }

    public Topic getServiceBusTopic(Tuple<ServiceBusNamespace, String> namespaceAndTopicName) {
        return namespaceAndTopicName.getFirst().topics().getByName(namespaceAndTopicName.getSecond());
    }

    private Queue getServiceBusQueue(Tuple<ServiceBusNamespace, String> namespaceAndQueueName) {
        return namespaceAndQueueName.getFirst().queues().getByName(namespaceAndQueueName.getSecond());
    }

    private Topic createServiceBusTopic(Tuple<ServiceBusNamespace, String> namespaceAndTopicName) {
        return namespaceAndTopicName.getFirst().topics().define(namespaceAndTopicName.getSecond()).create();
    }

    private Queue createServiceBusQueue(Tuple<ServiceBusNamespace, String> namespaceAndQueueName) {
        return namespaceAndQueueName.getFirst().queues().define(namespaceAndQueueName.getSecond()).create();
    }

    public ServiceBusSubscription getOrCreateServiceBusTopicSubscription(Topic topic, String name) {
        return getOrCreate(this::getServiceBusTopicSubscription, this::createServiceBusTopicSubscription)
                .apply(Tuple.of(topic, name));
    }

    private ServiceBusSubscription getServiceBusTopicSubscription(Tuple<Topic, String> topicAndSubscriptionName) {
        return topicAndSubscriptionName.getFirst().subscriptions().getByName(topicAndSubscriptionName.getSecond());
    }

    private ServiceBusSubscription createServiceBusTopicSubscription(Tuple<Topic, String> topicAndSubscriptionName) {
        return topicAndSubscriptionName.getFirst().subscriptions().define(topicAndSubscriptionName.getSecond())
                                       .create();
    }

    private RedisCache getRedisCache(String name) {
        return azure.redisCaches().getByResourceGroup(resourceGroup, name);
    }

    private RedisCache createRedisCache(String name) {
        return azure.redisCaches().define(name).withRegion(region).withExistingResourceGroup(resourceGroup)
                    .withBasicSku().create();
    }

    public RedisCache getOrCreateRedisCache(String name) {
        return getOrCreate(this::getRedisCache, this::createRedisCache).apply(name);
    }

    private <T, R> Function<T, R> getOrCreate(Function<T, R> getter, Function<T, R> creator) {
        return t -> {
            R result = getter.apply(t);
            if (result != null) {
                return result;
            }

            return creator.apply(t);
        };
    }

}
