/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Warren Zhu
 */
@RestController
public class WebController {

    @Autowired
    private CloudStorageAccount storageAccount;

    @GetMapping("/{container}/{blob}")
    public String getValue(@PathVariable String container, @PathVariable String blob)
            throws IOException, StorageException, URISyntaxException {
        CloudBlobClient blobClient = this.storageAccount.createCloudBlobClient();
        CloudBlobContainer blobContainer = blobClient.getContainerReference(container);
        CloudBlockBlob blockBlob = blobContainer.getBlockBlobReference(blob);

        return blockBlob.downloadText();
    }
}
