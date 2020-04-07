/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.context.core.util;

public class Constants {

    // for the User-Agent header set in track2 SDKs
    public static final String SPRINGCLOUD_VERSION = "snapshot";
    public static final String SPRINGCLOUD_STORAGE_BLOB_APPLICATION_ID = "az-sc-sb/"
            + SPRINGCLOUD_VERSION;
    public static final String SPRINGCLOUD_STORAGE_FILE_SHARE_APPLICATION_ID = "az-sc-sf/"
            + SPRINGCLOUD_VERSION;
    public static final String SPRINGCLOUD_STORAGE_QUEUE_APPLICATION_ID = "az-si-sq/"
            + SPRINGCLOUD_VERSION;

}
