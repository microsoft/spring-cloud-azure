/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package example;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Warren Zhu
 */
@RestController
public class WebController {

    private final ServiceBusQueueApplication.ServiceBusOutboundGateway messagingGateway;

    public WebController(ServiceBusQueueApplication.ServiceBusOutboundGateway messagingGateway) {
        this.messagingGateway = messagingGateway;
    }

    /**
     * Posts a message to a Azure Service Bus queue, through Spring's
     * service bus queue gateway, and redirects the user to the home page.
     */
    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        this.messagingGateway.sendToServiceBusQueue(message);
        return message;
    }
}
