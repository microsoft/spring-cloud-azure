/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration;

/**
 * Azure event hub internal headers for Spring Messaging messages.
 *
 * @author Warren Zhu
 */
public class EventHubHeaders {

    private static final String PREFIX = "azure_event_hub_";

    public static final String PARTITION_ID = PREFIX + "partition_id";

    public static final String PARTITION_KEY = PREFIX + "partition_key";

    public static final String NAME = PREFIX + "name";
}
