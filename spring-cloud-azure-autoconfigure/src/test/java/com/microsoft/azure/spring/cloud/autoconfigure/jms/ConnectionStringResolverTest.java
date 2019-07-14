/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import org.junit.Assert;
import org.junit.Test;
import java.util.Hashtable;

public class ConnectionStringResolverTest {
    @Test
    public void testConnectionStringResolver(){
        String connectionString = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;SharedAccessKey=sasKey";

        ConnectionStringResolver csr = new ConnectionStringResolver(connectionString);

        Hashtable hashtable = csr.getResolvedKeysAndValues();

        Assert.assertEquals("sb://host/", (String) hashtable.get("Endpoint"));
        Assert.assertEquals("host", (String) hashtable.get("host"));
        Assert.assertEquals("sasKeyName", (String) hashtable.get("SharedAccessKeyName"));
        Assert.assertEquals("sasKey", (String) hashtable.get("SharedAccessKey"));
    }
}
