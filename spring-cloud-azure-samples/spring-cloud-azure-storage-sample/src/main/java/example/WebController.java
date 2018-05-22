/*
 *  Copyright 2017 original author or authors.
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
