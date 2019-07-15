/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class ConnectionStringResolverTest {
    @Test
    public void testConnectionStringResolver(){
        String connectionString = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;SharedAccessKey=sasKey";

        ConnectionStringResolver csr = new ConnectionStringResolver(connectionString);

        HashMap<String, String> hashMap = csr.getResolvedKeysAndValues();

        Assert.assertEquals("sb://host/", hashMap.get("Endpoint"));
        Assert.assertEquals("host", hashMap.get("host"));
        Assert.assertEquals("sasKeyName", hashMap.get("SharedAccessKeyName"));
        Assert.assertEquals("sasKey", hashMap.get("SharedAccessKey"));
    }
}
