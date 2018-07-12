/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core;

import com.google.common.base.Strings;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link CredentialsProvider} implementation that provides credentials based on
 * user-provided properties and defaults.
 *
 * @author Warren Zhu
 */
public class DefaultCredentialsProvider implements CredentialsProvider {

    private static final String TEMP_CREDENTIAL_FILE_PREFIX = "azure";

    private static final String TEMP_CREDENTIAL_FILE_SUFFIX = "credential";

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCredentialsProvider.class);

    private ApplicationTokenCredentials credentials;

    public DefaultCredentialsProvider(CredentialSupplier supplier) {
        initCredentials(supplier);
    }

    private File createTempCredentialFile(@NonNull InputStream inputStream) throws IOException {
        File tempCredentialFile = File.createTempFile(TEMP_CREDENTIAL_FILE_PREFIX, TEMP_CREDENTIAL_FILE_SUFFIX);

        tempCredentialFile.deleteOnExit();
        FileUtils.copyInputStreamToFile(inputStream, tempCredentialFile);

        return tempCredentialFile;
    }

    private void initCredentials(CredentialSupplier supplier) {
        if (!Strings.isNullOrEmpty(supplier.getCredentialFilePath())) {
            try {
                DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
                InputStream inputStream = resourceLoader.getResource(supplier.getCredentialFilePath()).getInputStream();
                File credentialFile = this.createTempCredentialFile(inputStream);

                this.credentials = ApplicationTokenCredentials.fromFile(credentialFile);
            } catch (IOException e) {
                LOGGER.error("Credential file path not found.", e);
            }
        } else {
            throw new RuntimeException("No credentials provided.");
        }
    }

    @Override
    public ApplicationTokenCredentials getCredentials() {
        return this.credentials;
    }
}
