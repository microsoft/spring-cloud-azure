/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A {@link ProtocolResolver} implementation for the {@code blob://} protocol.
 *
 * @author Warren Zhu
 */
public class AzureStorageProtocolResolver implements ProtocolResolver, BeanFactoryPostProcessor, ResourceLoaderAware {

    private static final Log logger = LogFactory.getLog(AzureStorageProtocolResolver.class);

    private ConfigurableListableBeanFactory beanFactory;
    private CloudBlobClient blobClient;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private CloudBlobClient getBlobClient() {
        if (this.blobClient == null) {
            this.blobClient = this.beanFactory.getBean(CloudStorageAccount.class).createCloudBlobClient();
        }
        return this.blobClient;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (DefaultResourceLoader.class.isAssignableFrom(resourceLoader.getClass())) {
            ((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
        } else {
            logger.warn("The provided delegate resource loader is not an implementation " +
                    "of DefaultResourceLoader. Custom Protocol using blob:// prefix will not be enabled.");
        }
    }

    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        if (AzureStorageUtils.isAzureStorageResource(location)) {
            return new BlobStorageResource(getBlobClient(), location, true);
        }

        return null;
    }
}
