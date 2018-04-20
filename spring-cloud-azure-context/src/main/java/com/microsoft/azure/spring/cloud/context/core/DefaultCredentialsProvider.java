/*
 *  Copyright 2018 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.microsoft.azure.spring.cloud.context.core;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Strings;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.ClassPathResource;

/**
 * A {@link CredentialsProvider} implementation that provides credentials based on
 * user-provided properties and defaults.
 *
 * @author Warren Zhu
 */
public class DefaultCredentialsProvider implements CredentialsProvider {

    private static final Log LOGGER = LogFactory.getLog(DefaultCredentialsProvider.class);

    private ApplicationTokenCredentials credentials;

    public DefaultCredentialsProvider(CredentialSupplier supplier) {
        initCredentials(supplier);
    }

    private void initCredentials(CredentialSupplier supplier) {
        if (!Strings.isNullOrEmpty(supplier.getCredentialFilePath())) {
            try {
                File credentialFile = new ClassPathResource(supplier.getCredentialFilePath()).getFile();
                this.credentials = ApplicationTokenCredentials.fromFile(credentialFile);
            }
            catch (IOException e) {
                LOGGER.error("Credential file path not found.");
            }
        }
        else {
            throw new RuntimeException("No credentials provided.");
        }
    }

    @Override
    public ApplicationTokenCredentials getCredentials() {
        return this.credentials;
    }
}
