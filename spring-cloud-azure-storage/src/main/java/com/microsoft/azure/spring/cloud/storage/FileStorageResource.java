/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Implements {@link WritableResource} for reading and writing objects in Azure
 * StorageAccount file. An instance of this class represents a handle to a file.
 *
 * @author Warren Zhu
 */
@Slf4j
public class FileStorageResource extends AzureStorageResource {
    private static final String MSG_FAIL_GET = "Failed to get file or container";
    private static final String MSG_FAIL_OPEN_OUTPUT = "Failed to open output stream of file";
    private static final String MSG_FAIL_CHECK_EXIST = "Failed to check existence of file or container";
    private static final String MSG_FAIL_OPEN_INPUT = "Failed to open input stream of file";
    private final CloudFileClient fileClient;
    private final String location;
    private final CloudFile cloudFile;
    private final boolean autoCreateFiles;
    private final CloudFileShare fileShare;

    public FileStorageResource(CloudFileClient fileClient, String location) {
        this(fileClient, location, false);
    }

    FileStorageResource(CloudFileClient fileClient, String location, boolean autoCreateFiles) {
        assertIsAzureStorageLocation(location);
        this.autoCreateFiles = autoCreateFiles;
        this.fileClient = fileClient;
        this.location = location;

        try {
            this.fileShare = fileClient.getShareReference(getContainerName(location));
            this.cloudFile = fileShare.getRootDirectoryReference().getFileReference(getFileName(location));
        } catch (URISyntaxException | StorageException e) {
            log.error(MSG_FAIL_GET, e);
            throw new RuntimeException(MSG_FAIL_GET, e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            if (!exists()) {
                if (autoCreateFiles) {
                    create();
                } else {
                    throw new FileNotFoundException("The file was not found: " + this.location);
                }
            }
            return this.cloudFile.openWriteExisting();
        } catch (URISyntaxException | StorageException e) {
            log.error(MSG_FAIL_OPEN_OUTPUT, e);
            throw new IOException(MSG_FAIL_OPEN_OUTPUT, e);
        }
    }

    @Override
    public boolean exists() {
        try {
            return this.fileShare.exists() && cloudFile.exists();
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
        return this.cloudFile.getStorageUri().getPrimaryUri();
    }

    @Override
    public File getFile() throws IOException {
        throw new UnsupportedOperationException(getDescription() + " cannot be resolved to absolute file path");
    }

    @Override
    public long contentLength() throws IOException {
        return this.cloudFile.getProperties().getLength();
    }

    @Override
    public long lastModified() throws IOException {
        return this.cloudFile.getProperties().getLastModified().getTime();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        String newLocation = this.location + "/" + relativePath;
        return new FileStorageResource(this.fileClient, newLocation, autoCreateFiles);
    }

    @Override
    public String getFilename() {
        return this.cloudFile.getName();
    }

    @Override
    public String getDescription() {
        return String.format("Azure storage account file resource [container='%s', file='%s']",
                this.fileShare.getName(), this.cloudFile.getName());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            assertExisted();
            return this.cloudFile.openRead();
        } catch (StorageException e) {
            log.error("Failed to open input stream of cloud file", e);
            throw new IOException("Failed to open input stream of cloud file");
        }
    }

    @Override
    StorageType getStorageType() {
        return StorageType.FILE;
    }

    private void assertExisted() throws FileNotFoundException {
        if (!exists()) {
            throw new FileNotFoundException("File or container not existed.");
        }
    }

    private void create() throws StorageException, URISyntaxException {
        this.fileShare.createIfNotExists();
        //TODO: create method must provide file length, but we don't know actual
        //file size when creating. Pending on github issue feedback.
        this.cloudFile.create(1024);
    }
}
