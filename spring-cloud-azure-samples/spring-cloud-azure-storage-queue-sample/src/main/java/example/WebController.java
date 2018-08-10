/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

    private StorageQueueApplication.StorageQueueOutboundGateway storageQueueOutboundGateway;

    public WebController(StorageQueueApplication.StorageQueueOutboundGateway storageQueueOutboundGateway) {
        this.storageQueueOutboundGateway = storageQueueOutboundGateway;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void send(@RequestBody String data) {
        storageQueueOutboundGateway.send(data);
    }

}
