/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Implements {@link WritableResource} for reading and writing objects in Azure
 * StorageAccount blob. An instance of this class represents a handle to a blob.
 *
 * @author Warren Zhu
 */
@Slf4j
public class BlobStorageResource extends AbstractResource implements WritableResource {

    private final CloudBlobClient blobClient;
    private final String location;
    private final CloudBlobContainer blobContainer;
    private final CloudBlockBlob blockBlob;
    private final boolean autoCreateFiles;

    public BlobStorageResource(CloudBlobClient blobClient, String location) {
        this(blobClient, location, false);
    }

    public BlobStorageResource(CloudBlobClient blobClient, String location, boolean autoCreateFiles) {
        this.autoCreateFiles = autoCreateFiles;
        AzureStorageUtils.isAzureStorageResource(location);
        this.blobClient = blobClient;
        this.location = location;

        try {
            this.blobContainer = blobClient.getContainerReference(AzureStorageUtils.getContainerName(location));
            this.blockBlob = blobContainer.getBlockBlobReference(AzureStorageUtils.getBlobName(location));
        } catch (URISyntaxException | StorageException e) {
            log.error("Failed to get cloud blob or container ", e);
            throw new RuntimeException("Failed to get cloud blob or container", e);
        }

    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            if (!exists()) {
                if (autoCreateFiles) {
                    create();
                } else {
                    throw new FileNotFoundException("The blob was not found: " + this.location);
                }
            }
            return this.blockBlob.openOutputStream();
        } catch (StorageException e) {
            log.error("Failed to open output stream of cloud blob", e);
            throw new IOException("Failed to open output stream of cloud blob");
        }
    }

    @Override
    public boolean exists() {
        try {
            return this.blobContainer.exists() && blockBlob.exists();
        } catch (StorageException e) {
            log.error("Failed to check existence of cloud blob or container", e);
            return false;
        }
    }

    @Override
    public URL getURL() throws IOException {
        return this.getURI().toURL();
    }

    @Override
    public URI getURI() throws IOException {
        return this.blockBlob.getStorageUri().getPrimaryUri();
    }

    @Override
    public File getFile() throws IOException {
        throw new UnsupportedOperationException(getDescription() + " cannot be resolved to absolute file path");
    }

    @Override
    public long contentLength() throws IOException {
        return this.blockBlob.getProperties().getLength();
    }

    @Override
    public long lastModified() throws IOException {
        return this.blockBlob.getProperties().getLastModified().getTime();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        String newLocation = this.location + "/" + relativePath;
        return new BlobStorageResource(this.blobClient, newLocation, autoCreateFiles);
    }

    @Override
    public String getFilename() {
        return this.blockBlob.getName();
    }

    @Override
    public String getDescription() {
        return String.format("Azure storage account block blob resource [container='%s', blob='%s']",
                this.blobContainer.getName(), this.blockBlob.getName());
    }

    @Override
    public InputStream getInputStream() throws IOException {

        try {
            assertExisted();
            return this.blockBlob.openInputStream();
        } catch (StorageException e) {
            log.error("Failed to open input stream of cloud blob", e);
            throw new IOException("Failed to open input stream of cloud blob");
        }
    }

    private void assertExisted() throws FileNotFoundException {
        if (!exists()) {
            throw new FileNotFoundException("Blob or container not existed.");
        }
    }

    private void create() throws StorageException {
        this.blobContainer.createIfNotExists();
    }

}
