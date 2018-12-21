/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.spring.cloud.config.msi.ConfigMSICredentials;
import com.microsoft.azure.spring.cloud.config.msi.ConfigResourceManager;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.rest.RestException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

import static com.microsoft.azure.spring.cloud.config.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Azure.class)
@PowerMockIgnore({"javax.net.ssl.*"})
public class ConfigResourceManagerTest {
    @Mock
    private Azure.Authenticated authenticated;

    @Mock
    private ConfigMSICredentials msiCredentials;

    @Mock
    private Subscriptions subscriptions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void configStoreCanBeSearchedIfExist() {
        String expectedStoreName = TEST_STORE_NAME_2;
        // Data hierarchy: Subscriptions -> [Subscription, ...]
        // Test data: Three resources under three different subscriptions and resource groups
        Subscription subscription1 = initSubscription(TEST_SUBSCRIPTION_1, TEST_NON_CONFIG_TYPE, TEST_STORE_NAME_1,
                TEST_RESOURCE_GROUP_1);
        // Two resource has correct store name, but only the third one has correct resource type
        Subscription subscription2 = initSubscription(TEST_SUBSCRIPTION_2, TEST_NON_CONFIG_TYPE, expectedStoreName,
                TEST_RESOURCE_GROUP_2);
        // Only one resource has correct resource type
        Subscription subscription3 = initSubscription(TEST_SUBSCRIPTION_3, TEST_CONFIG_TYPE, expectedStoreName,
                TEST_RESOURCE_GROUP_3);

        PagedList<Subscription> subsList = new TestPagedList<>();
        subsList.addAll(Arrays.asList(subscription1, subscription2, subscription3));

        when(subscriptions.list()).thenReturn(subsList);
        when(authenticated.subscriptions()).thenReturn(subscriptions);

        PowerMockito.mockStatic(Azure.class);
        when(Azure.authenticate(any(AzureTokenCredentials.class))).thenReturn(authenticated);
        ConfigResourceManager resourceManager = new ConfigResourceManager(msiCredentials);

        // Only resource3 has correct store name and resource type
        Tuple<String, String> resourceInfo = resourceManager.findStore(expectedStoreName);
        assertThat(resourceInfo).isNotNull();
        assertThat(resourceInfo.getFirst()).isEqualTo(TEST_SUBSCRIPTION_3);
        assertThat(resourceInfo.getSecond()).isEqualTo(TEST_RESOURCE_GROUP_3);
    }

    private Subscription initSubscription(String subscriptionId, String resourceType, String resourceName,
                                          String resourceGroup) {
        // Data hierarchy: Subscription -> Azure -> GenericResources -> [GenericResource, ...]
        Subscription subscription = mock(Subscription.class);
        Azure azure = mock(Azure.class);
        GenericResources genericResources = mock(GenericResources.class);
        GenericResource resource = mock(GenericResource.class);

        when(subscription.subscriptionId()).thenReturn(subscriptionId);
        when(resource.type()).thenReturn(resourceType);
        when(resource.name()).thenReturn(resourceName);
        when(resource.resourceGroupName()).thenReturn(resourceGroup);

        when(genericResources.list()).thenReturn(TestPagedList.of(resource));
        when(azure.genericResources()).thenReturn(genericResources);

        when(authenticated.withSubscription(subscription.subscriptionId())).thenReturn(azure);

        return subscription;
    }

    static class TestPagedList<E> extends PagedList<E> {
        @Override
        public Page<E> nextPage(String nextPageLink) throws RestException {
            return null;
        }

        public static <E> TestPagedList<E> of(E element) {
            TestPagedList<E> list = new TestPagedList<>();
            list.add(element);
            return list;
        }
    }
}
