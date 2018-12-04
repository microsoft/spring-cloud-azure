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
public class BlobStorageResource extends AzureStorageResource {
    private static final String MSG_FAIL_GET = "Failed to get blob or container";
    private static final String MSG_FAIL_OPEN_OUTPUT = "Failed to open output stream of cloud blob";
    private static final String MSG_FAIL_CHECK_EXIST = "Failed to check existence of blob or container";
    private static final String MSG_FAIL_OPEN_INPUT = "Failed to open input stream of blob";
    private final CloudBlobClient blobClient;
    private final String location;
    private final CloudBlobContainer blobContainer;
    private final CloudBlockBlob blockBlob;
    private final boolean autoCreateFiles;

    BlobStorageResource(CloudBlobClient blobClient, String location) {
        this(blobClient, location, false);
    }

    BlobStorageResource(CloudBlobClient blobClient, String location, boolean autoCreateFiles) {
        assertIsAzureStorageLocation(location);
        this.autoCreateFiles = autoCreateFiles;
        this.blobClient = blobClient;
        this.location = location;

        try {
            this.blobContainer = blobClient.getContainerReference(getContainerName(location));
            this.blockBlob = blobContainer.getBlockBlobReference(getFileName(location));
        } catch (URISyntaxException | StorageException e) {
            log.error(MSG_FAIL_GET, e);
            throw new StorageRuntimeException(MSG_FAIL_GET, e);
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
            log.error(MSG_FAIL_OPEN_OUTPUT, e);
            throw new IOException(MSG_FAIL_OPEN_OUTPUT, e);
        }
    }

    @Override
    public boolean exists() {
        try {
            return this.blobContainer.exists() && blockBlob.exists();
        } catch (StorageException e) {
            log.error(MSG_FAIL_CHECK_EXIST, e);
            throw new StorageRuntimeException(MSG_FAIL_CHECK_EXIST, e);
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
        return String
                .format("Azure storage account blob resource [container='%s', blob='%s']", this.blobContainer.getName(),
                        this.blockBlob.getName());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            assertExisted();
            return this.blockBlob.openInputStream();
        } catch (StorageException e) {
            log.error(MSG_FAIL_OPEN_INPUT, e);
            throw new IOException(MSG_FAIL_OPEN_INPUT);
        }
    }

    @Override
    StorageType getStorageType() {
        return StorageType.BLOB;
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
