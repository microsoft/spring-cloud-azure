/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.storage.StorageAccount;
import org.springframework.util.Assert;

public class AzureAdmin {

    private final Azure azure;
    private final String resourceGroup;
    private final String region;

    public AzureAdmin(Azure azure, String resourceGroup, String region) {
        Assert.notNull(azure, "azure can't be null");
        Assert.hasText(resourceGroup, "resourceGroup can't be null or empty");
        Assert.hasText(region, "region can't be null or empty");
        this.azure = azure;
        this.resourceGroup = resourceGroup;
        this.region = region;
        createResourceGroupIfNotExisted();
    }

    public EventHub getOrCreateEventHub(String namespace, String name) {
        EventHub eventHub = getEventHub(namespace, name);
        if (eventHub == null) {
            return createEventHub(namespace, name);
        }

        return eventHub;
    }

    public EventHub getEventHub(String namespace, String name) {
        return azure.eventHubs().getByName(resourceGroup, namespace, name);
    }

    public EventHub createEventHub(String namespace, String name) {
        EventHubNamespace eventHubNamespace = getOrCreateEventHubNamespace(namespace);

        return azure.eventHubs().define(name).withExistingNamespace(eventHubNamespace).create();
    }

    public EventHubNamespace getOrCreateEventHubNamespace(String namespace) {
        try {
            return azure.eventHubNamespaces().getByResourceGroup(resourceGroup, namespace);
        } catch (NullPointerException e) {
            // azure management api has no way to determine whether an eventhub namespace exists
            // Workaround for this is by catching NPE
            return azure.eventHubNamespaces().define(namespace).withRegion(region)
                        .withExistingResourceGroup(resourceGroup).create();
        }
    }

    public StorageAccount getOrCreateStorageAccount(String name) {
        StorageAccount storageAccount = getStorageAccount(name);
        if (storageAccount == null) {
            return createStorageAccount(name);
        }

        return storageAccount;
    }

    public StorageAccount getStorageAccount(String name) {
        return azure.storageAccounts().getByResourceGroup(resourceGroup, name);
    }

    public StorageAccount createStorageAccount(String name) {
        return azure.storageAccounts().define(name).withRegion(region).withExistingResourceGroup(resourceGroup)
                    .create();
    }

    private void createResourceGroupIfNotExisted() {
        if (!azure.resourceGroups().contain(resourceGroup)) {
            azure.resourceGroups().define(resourceGroup).withRegion(region).create();
        }
    }

    public void createEventHubConsumerGroupIfNotExisted(String namespace, String name, String group) {
        if (!eventHubConsumerGroupExists(namespace, name, group)) {
            azure.eventHubs().consumerGroups().define(group).withExistingEventHub(resourceGroup, namespace, name)
                 .create();
        }
    }

    public boolean eventHubConsumerGroupExists(String namespace, String name, String group) {
        return azure.eventHubs().getByName(resourceGroup, namespace, name).listConsumerGroups().stream()
                    .anyMatch(c -> c.equals(group));
    }

    public void createSqlDatabaseIfNotExists(String sqlServerName, String databaseName) {
        if (!sqlDatabaseExists(sqlServerName, databaseName)) {
            azure.sqlServers().databases().define(databaseName)
                 .withExistingSqlServer(resourceGroup, sqlServerName, region).create();
        }
    }

    public SqlServer createSqlServer(String sqlServerName, String username, String password) {
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

    public boolean sqlDatabaseExists(String sqlServerName, String databaseName) {
        return azure.sqlServers().databases().getBySqlServer(resourceGroup, sqlServerName, databaseName) != null;
    }

    public boolean sqlServerExists(String sqlServerName) {
        return azure.sqlServers().getByResourceGroup(resourceGroup, sqlServerName) != null;
    }

    public ServiceBusNamespace getOrCreateServiceBusNamespace(String namespace) {
        ServiceBusNamespace serviceBusNamespace = getServiceBusNamespace(namespace);

        if (serviceBusNamespace != null) {
            return serviceBusNamespace;
        }

        return createServiceBusNamespace(namespace);
    }

    public ServiceBusNamespace getServiceBusNamespace(String namespace) {
        return azure.serviceBusNamespaces().getByResourceGroup(resourceGroup, namespace);
    }

    public ServiceBusNamespace createServiceBusNamespace(String namespace) {
        return azure.serviceBusNamespaces().define(namespace).withRegion(region)
                    .withExistingResourceGroup(resourceGroup).create();
    }

    public Topic getOrCreateServiceBusTopic(ServiceBusNamespace namespace, String name) {
        Topic topic = getServiceBusTopic(namespace, name);

        if (topic != null) {
            return topic;
        }

        return createServiceBusTopic(namespace, name);
    }

    public Queue getOrCreateServiceBusQueue(ServiceBusNamespace namespace, String name) {
        Queue queue = getServiceBusQueue(namespace, name);

        if (queue != null) {
            return queue;
        }

        return createServiceBusQueue(namespace, name);
    }

    public Topic getServiceBusTopic(ServiceBusNamespace namespace, String name) {
        return namespace.topics().getByName(name);
    }

    public Queue getServiceBusQueue(ServiceBusNamespace namespace, String name) {
        return namespace.queues().getByName(name);
    }

    public Topic createServiceBusTopic(ServiceBusNamespace namespace, String name) {
        return namespace.topics().define(name).create();
    }

    public Queue createServiceBusQueue(ServiceBusNamespace namespace, String name) {
        return namespace.queues().define(name).create();
    }

    public ServiceBusSubscription getOrCreateServiceBusTopicSubscription(Topic topic, String name) {
        ServiceBusSubscription subscription = getServiceBusTopicSubscription(topic, name);

        if (subscription != null) {
            return subscription;
        }

        return createServiceBusTopicSubscription(topic, name);
    }

    public ServiceBusSubscription getServiceBusTopicSubscription(Topic topic, String name) {
        return topic.subscriptions().getByName(name);
    }

    public ServiceBusSubscription createServiceBusTopicSubscription(Topic topic, String name) {
        return topic.subscriptions().define(name).create();
    }

}
