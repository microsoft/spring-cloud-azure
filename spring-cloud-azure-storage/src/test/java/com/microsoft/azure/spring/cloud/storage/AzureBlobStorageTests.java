/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Warren Zhu
 */
@SpringBootTest(properties = "spring.main.banner-mode=off")
@RunWith(SpringRunner.class)
public class AzureBlobStorageTests {

    private static final String containerName = "container";
    private static final String nonExisting = "non-existing";
    private static final String blobName = "blob";

    @Value("azure-blob://container/blob")
    private Resource remoteResource;

    @Autowired
    private CloudBlobClient blobClient;

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPath() {
        new BlobStorageResource(this.blobClient, "azure-blob://");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlashPath() {
        new BlobStorageResource(this.blobClient, "azure-blob:///");
    }

    @Test
    public void testValidObject() throws Exception {
        Assert.assertTrue(this.remoteResource.exists());
        Assert.assertEquals(4096L, this.remoteResource.contentLength());
    }

    @Test
    public void testWritable() throws Exception {
        Assert.assertTrue(this.remoteResource instanceof WritableResource);
        WritableResource writableResource = (WritableResource) this.remoteResource;
        Assert.assertTrue(writableResource.isWritable());
        writableResource.getOutputStream();
    }

    @Test
    public void testWritableOutputStream() throws Exception {
        String location = "azure-blob://container/blob";

        BlobStorageResource resource = new BlobStorageResource(blobClient, location);
        OutputStream os = resource.getOutputStream();
        Assert.assertNotNull(os);
    }

    @Test(expected = FileNotFoundException.class)
    public void testWritableOutputStreamNoAutoCreateOnNullBlob() throws Exception {
        String location = "azure-blob://container/non-existing";

        BlobStorageResource resource = new BlobStorageResource(this.blobClient, location);
        resource.getOutputStream();
    }

    @Test(expected = FileNotFoundException.class)
    public void testGetInputStreamOnNullBlob() throws Exception {
        String location = "azure-blob://container/non-existing";

        BlobStorageResource resource = new BlobStorageResource(blobClient, location);
        resource.getInputStream();
    }

    @Test
    public void testGetFilenameOnNonExistingBlob() throws Exception {
        String location = "azure-blob://container/non-existing";
        BlobStorageResource resource = new BlobStorageResource(blobClient, location);
        Assert.assertEquals(nonExisting, resource.getFilename());
    }

    @Test
    public void testContainerDoesNotExist() {
        BlobStorageResource resource = new BlobStorageResource(this.blobClient, "azure-blob://non-existing/blob");
        Assert.assertFalse(resource.exists());
    }

    @Test
    public void testContainerExistsButResourceDoesNot() {
        BlobStorageResource resource = new BlobStorageResource(this.blobClient, "azure-blob://container/non-existing");
        Assert.assertFalse(resource.exists());
    }

    @Configuration
    @Import(AzureStorageProtocolResolver.class)
    static class StorageApplication {

        @Bean
        public static CloudBlobClient cloudBlobClient() throws Exception {
            return mockStorageAccount().createCloudBlobClient();
        }

        @Bean
        public static CloudStorageAccount mockStorageAccount() throws Exception {
            CloudStorageAccount storageAccount = mock(CloudStorageAccount.class);
            CloudBlobClient blobClient = mock(CloudBlobClient.class);
            CloudBlobContainer blobContainer = mock(CloudBlobContainer.class);
            CloudBlobContainer nonExistingContainer = mock(CloudBlobContainer.class);
            CloudBlockBlob blockBlob = mock(CloudBlockBlob.class);
            CloudBlockBlob nonExistingBlob = mock(CloudBlockBlob.class);
            BlobProperties blobProperties = mock(BlobProperties.class);

            when(storageAccount.createCloudBlobClient()).thenReturn(blobClient);
            when(blockBlob.exists()).thenReturn(true);
            when(nonExistingBlob.exists()).thenReturn(false);
            when(blobContainer.exists()).thenReturn(true);
            when(nonExistingContainer.exists()).thenReturn(false);
            when(blockBlob.getProperties()).thenReturn(blobProperties);
            when(blobProperties.getLength()).thenReturn(4096L);
            when(blobClient.getContainerReference(eq(containerName))).thenReturn(blobContainer);
            when(blobClient.getContainerReference(eq(nonExisting))).thenReturn(nonExistingContainer);
            when(blobContainer.getBlockBlobReference(eq(blobName))).thenReturn(blockBlob);
            when(blobContainer.getBlockBlobReference(eq(nonExisting))).thenReturn(nonExistingBlob);
            when(blockBlob.getName()).thenReturn(blobName);
            when(blockBlob.openInputStream()).thenReturn(mock(BlobInputStream.class));
            when(blockBlob.openOutputStream()).thenReturn(mock(BlobOutputStream.class));
            when(nonExistingBlob.getName()).thenReturn(nonExisting);

            return storageAccount;
        }

    }

}
