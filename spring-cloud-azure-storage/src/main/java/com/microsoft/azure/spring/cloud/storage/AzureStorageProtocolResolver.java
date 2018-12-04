/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A {@link ProtocolResolver} implementation for the {@code azure-blob://} or {@code azure-file://} protocol.
 *
 * @author Warren Zhu
 */
@Slf4j
public class AzureStorageProtocolResolver implements ProtocolResolver, BeanFactoryPostProcessor, ResourceLoaderAware {

    private ConfigurableListableBeanFactory beanFactory;
    private CloudStorageAccount cloudStorageAccount;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (DefaultResourceLoader.class.isAssignableFrom(resourceLoader.getClass())) {
            ((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
        } else {
            log.warn("Custom Protocol using azure-blob:// or azure-file:// prefix will not be enabled.");
        }
    }

    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        if (AzureStorageUtils.isAzureStorageResource(location, StorageType.BLOB)) {
            return new BlobStorageResource(getCloudStorageAccount().createCloudBlobClient(), location, true);
        } else if (AzureStorageUtils.isAzureStorageResource(location, StorageType.FILE)) {
            return new FileStorageResource(getCloudStorageAccount().createCloudFileClient(), location, true);
        }

        return null;
    }

    private CloudStorageAccount getCloudStorageAccount() {
        if (cloudStorageAccount == null) {
            this.cloudStorageAccount = this.beanFactory.getBean(CloudStorageAccount.class);
        }

        return cloudStorageAccount;
    }
}
