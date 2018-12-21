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
        // Subscription -> Azure -> GenericResources -> [GenericResource, ...]
        // Test data: Three resources under three different subscriptions and resource groups
        Subscription subscription1 = mock(Subscription.class);
        GenericResource resource1 = mock(GenericResource.class);
        Azure azure1 = mock(Azure.class);
        GenericResources genericResources1 = mock(GenericResources.class);

        Subscription subscription2 = mock(Subscription.class);
        GenericResource resource2 = mock(GenericResource.class);
        Azure azure2 = mock(Azure.class);
        GenericResources genericResources2 = mock(GenericResources.class);

        Subscription subscription3 = mock(Subscription.class);
        GenericResource resource3 = mock(GenericResource.class);
        Azure azure3 = mock(Azure.class);
        GenericResources genericResources3 = mock(GenericResources.class);

        when(subscription1.subscriptionId()).thenReturn(TEST_SUBSCRIPTION_1);
        when(subscription2.subscriptionId()).thenReturn(TEST_SUBSCRIPTION_2);
        when(subscription3.subscriptionId()).thenReturn(TEST_SUBSCRIPTION_3);

        // Only one resource has correct resource type
        when(resource1.type()).thenReturn(TEST_NON_CONFIG_TYPE);
        when(resource2.type()).thenReturn(TEST_NON_CONFIG_TYPE);
        when(resource3.type()).thenReturn(TEST_CONFIG_TYPE);

        // Two resource has correct store name, but only the third one has correct resource type as configured above
        when(resource1.name()).thenReturn(TEST_STORE_NAME_1);
        when(resource2.name()).thenReturn(expectedStoreName);
        when(resource3.name()).thenReturn(expectedStoreName);

        when(resource1.resourceGroupName()).thenReturn(TEST_RESOURCE_GROUP_1);
        when(resource2.resourceGroupName()).thenReturn(TEST_RESOURCE_GROUP_2);
        when(resource3.resourceGroupName()).thenReturn(TEST_RESOURCE_GROUP_3);

        PagedList<Subscription> subsList = new TestPagedList<>();
        subsList.addAll(Arrays.asList(subscription1, subscription2, subscription3));

        when(genericResources1.list()).thenReturn(TestPagedList.of(resource1));
        when(genericResources2.list()).thenReturn(TestPagedList.of(resource2));
        when(genericResources3.list()).thenReturn(TestPagedList.of(resource3));

        when(subscriptions.list()).thenReturn(subsList);
        when(azure1.genericResources()).thenReturn(genericResources1);
        when(azure2.genericResources()).thenReturn(genericResources2);
        when(azure3.genericResources()).thenReturn(genericResources3);

        when(authenticated.subscriptions()).thenReturn(subscriptions);
        when(authenticated.withSubscription(subscription1.subscriptionId())).thenReturn(azure1);
        when(authenticated.withSubscription(subscription2.subscriptionId())).thenReturn(azure2);
        when(authenticated.withSubscription(subscription3.subscriptionId())).thenReturn(azure3);

        PowerMockito.mockStatic(Azure.class);
        when(Azure.authenticate(any(AzureTokenCredentials.class))).thenReturn(authenticated);
        ConfigResourceManager resourceManager = new ConfigResourceManager(msiCredentials);

        // Only resource3 has correct store name and resource type
        Tuple<String, String> resourceInfo = resourceManager.findStore(expectedStoreName);
        assertThat(resourceInfo).isNotNull();
        assertThat(resourceInfo.getFirst()).isEqualTo(TEST_SUBSCRIPTION_3);
        assertThat(resourceInfo.getSecond()).isEqualTo(TEST_RESOURCE_GROUP_3);
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
