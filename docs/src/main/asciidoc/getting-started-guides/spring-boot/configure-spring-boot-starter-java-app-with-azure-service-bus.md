---
title: How to use the Spring Boot Starter for Azure Service Bus JMS
description: This article demonstrates how to use the Spring JMS Starter to send messages to and receive messages from Azure Service Bus.
manager: kyliel
ms.author: seal
ms.date: 03/30/2022
ms.topic: article
ms.custom: devx-track-java
---

# How to use the Spring Boot Starter for Azure Service Bus JMS

This article demonstrates how to use Spring Boot Starter for Azure Service Bus JMS to send messages to and receive messages from Service Bus `queues` and `topics`.

Azure provides an asynchronous messaging platform called [Azure Service Bus](/azure/service-bus-messaging/service-bus-messaging-overview) ("Service Bus") that is based on the [Advanced Message Queueing Protocol 1.0](http://www.amqp.org/) ("AMQP 1.0") standard. Service Bus can be used across the range of supported Azure platforms.

The Spring Boot Starter for Azure Service Bus JMS provides Spring JMS integration with Service Bus.

The following video describes how to integrate Spring JMS applications with Azure Service Bus using JMS 2.0.

<br>

> [!VIDEO https://www.youtube.com/embed/9O3CALyoZHE?list=PLPeZXlCR7ew8LlhnSH63KcM0XhMKxT1k_]

## Prerequisites

The following prerequisites are required for this article:

1. An Azure subscription; if you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/credit-for-visual-studio-subscribers/) or sign up for a [free account](https://azure.microsoft.com/free/).

1. A supported Java Development Kit (JDK), version 8 or later. For more information about the JDKs available for use when developing on Azure, see [Java support on Azure and Azure Stack](../fundamentals/java-support-on-azure.md).

1. [Apache Maven](http://maven.apache.org/), version 3.2 or later.

1. If you already have a configured Service Bus queue or topic, ensure that the Service Bus namespace meets the following requirements:

    1. Allows access from all networks
    1. Is Premium (or higher)
    1. Has an access policy with read/write access for your queue and topic

1. If you don't have a configured Service Bus queue or topic, use the Azure portal to [create a Service Bus queue](/azure/service-bus-messaging/service-bus-quickstart-portal) or [create a Service Bus topic](/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal). Ensure that the namespace meets the requirements specified in the previous step. Also, make note of the connection string in the namespace as you need it for this tutorial's test app.

1. If you don't have a Spring Boot application, create a **Maven** project with the [Spring Initializr](https://start.spring.io/). Remember to select **Maven Project** and, under **Dependencies**, add the **Web** dependency, select **8** or **11** Java version.

> [!IMPORTANT]
> Spring Boot version 2.5 or 2.6 is required to complete the steps in this article.

## Use the Azure Service Bus JMS starter

1. Locate the *pom.xml* file in the parent directory of your app; for example:

    *C:\SpringBoot\servicebus\pom.xml*

    -or-

    */users/example/home/servicebus/pom.xml*

1. Open the *pom.xml* file in a text editor.

1. Add the Spring Boot Azure Service Bus JMS starter to the list of `<dependencies>`:

    ```xml
    <dependency>
      <groupId>com.azure.spring</groupId>
      <artifactId>spring-cloud-azure-starter-servicebus-jms</artifactId>
      <version>4.0.0</version>
    </dependency>
    ```


1. Save and close the *pom.xml* file.

## Configure the app for your service bus 

In this section, you see how to configure your app to use either a Service Bus queue or topic.

### Use a Service Bus queue

1. Locate the *application.properties* in the *resources* directory of your app; for example:

    *C:\SpringBoot\servicebus\application.properties*

    -or-

    */users/example/home/servicebus/application.properties*

1. Open the *application.properties* file in a text editor.

1. Append the following code to the end of the *application.properties* file. Replace the placeholder values with the appropriate values for your service bus, and do not put quotes around the values.

    ```yml
    spring.jms.servicebus.connection-string=<ServiceBusNamespaceConnectionString>
    spring.jms.servicebus.idle-timeout=<IdleTimeout>
    spring.jms.servicebus.pricing-tier=<ServiceBusPricingTier> 
    ```

    **Field descriptions**

    | Field                                     | Description                               |
    |-------------------------------------------|-------------------------------------------------------------------------------------------------|
    | `spring.jms.servicebus.connection-string` | Specify the connection string you obtained in your Service Bus namespace from the Azure portal. |
    | `spring.jms.servicebus.idle-timeout`      | Specify the duration for idle.       |
    | `spring.jms.servicebus.pricing-tier`       | Specify the pricing tier of your service bus. Supported values are *premium*, *standard*, and *basic*. Premium uses Java Message Service (JMS) 2.0, while standard and basic use JMS 1.0 to interact with Azure Service Bus. |

1. Save and close the *application.properties* file.

### Use Service Bus topic

1. Locate the *application.properties* in the *resources* directory of your app; for example:

    *C:\SpringBoot\servicebus\application.properties*

    -or-

    */users/example/home/servicebus/application.properties*

1. Open the *application.properties* file in a text editor.

1. Append the following code to the end of the *application.properties* file. Replace the placeholder values with the appropriate values for your service bus, and do not put quotes around the values.

    ```yml
    spring.jms.servicebus.connection-string=<ServiceBusNamespaceConnectionString>
    spring.jms.servicebus.topic-client-id=<ServiceBusSubscriptionID>
    spring.jms.servicebus.idle-timeout=<IdleTimeout>
    spring.jms.servicebus.pricing-tier=<ServiceBusPricingTier> 
    ```

    **Field descriptions**

    | Field                                     | Description                                                                                       |
    |-------------------------------------------|---------------------------------------------------------------------------------------------------|
    | `spring.jms.servicebus.connection-string` | Specify the connection string you obtained in your Service Bus namespace from the Azure portal.   |
    | `spring.jms.servicebus.topic-client-id`   | Specify the JMS client ID, which is your Service Bus Subscription ID in the Azure portal.                | 
    | `spring.jms.servicebus.idle-timeout`      | Specify the duration for idle.     |
    | `spring.jms.servicebus.pricing-tier`       | Specify the pricing tier of your service bus. Supported values are *premium*, *standard*, and *basic*. Premium uses Java Message Service (JMS) 2.0, while standard and basic use JMS 1.0 to interact with Azure Service Bus. |

1. Save and close the *application.properties* file.

## Implement basic Service Bus functionality

In this section, you create the necessary Java classes for sending messages to your Service Bus queue or topic and receive messages from your corresponding queue or topic subscription.

### Modify the main application class

1. Locate the main application Java file in the package directory of your app; for example:

    *C:\SpringBoot\servicebus\src\main\java\com\wingtiptoys\servicebus\ServiceBusJmsStarterApplication.java*

    -or-

    */users/example/home/servicebus/src/main/java/com/wingtiptoys/servicebus/ServiceBusJmsStarterApplication.java*

1. Open the main application Java file in a text editor.

1. Add the following code to the file:

   ```java
    package com.wingtiptoys.servicebus;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    @SpringBootApplication
    public class ServiceBusJmsStarterApplication {

        public static void main(String[] args) {
            SpringApplication.run(ServiceBusJmsStarterApplication.class, args);
        }
    }
    ```

1. Save and close the file.

### Define a test Java class

1. Using a text editor, create a Java file named *User.java* in the package directory of your app.

1. Define a generic user class that stores and retrieves user's name:

    ```java
    package com.wingtiptoys.servicebus;

    import java.io.Serializable;

    // Define a generic User class.
    public class User implements Serializable {

        private static final long serialVersionUID = -295422703255886286L;

        private String name;

        public User() {
        }

        public User(String name) {
            setName(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
    ```

    `Serializable` is implemented to use the `send` method in `JmsTemplate` in the Spring framework. Otherwise, a customized `MessageConverter` bean should be defined to serialize the content to json in text format. For more information about `MessageConverter`, see the official [Spring JMS starter project](https://spring.io/guides/gs/messaging-jms/).

1. Save and close the *User.java* file.

### Create a new class for the message send controller

1. Using a text editor, create a Java file named *SendController.java* in the package directory of your app

1. Add the following code to the new file:

    ```java
    package com.wingtiptoys.servicebus;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.jms.core.JmsTemplate;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    public class SendController {

        private static final String DESTINATION_NAME = "<DestinationName>";

        private static final Logger logger = LoggerFactory.getLogger(SendController.class);

        @Autowired
        private JmsTemplate jmsTemplate;

        @PostMapping("/messages")
        public String postMessage(@RequestParam String message) {
            logger.info("Sending message");
            jmsTemplate.convertAndSend(DESTINATION_NAME, new User(message));
            return message;
        }
    }
    ```

    > [!NOTE]
    > Replace `<DestinationName>` with your own queue name or topic name configured in your Service Bus namespace.

1. Save and close the *SendController.java*.

### Create a class for the message receive controller

#### Receive messages from a Service Bus queue

1. Use a text editor to create a Java file named *QueueReceiveController.java* in the package directory of your app

1. Add the following code to the new file:

    ```java
    package com.wingtiptoys.servicebus;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.jms.annotation.JmsListener;
    import org.springframework.stereotype.Component;

    @Component
    public class QueueReceiveController {

        private static final String QUEUE_NAME = "<ServiceBusQueueName>";

        private final Logger logger = LoggerFactory.getLogger(QueueReceiveController.class);

        @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
        public void receiveMessage(User user) {
            logger.info("Received message: {}", user.getName());
        }
    }
    ```

    > [!NOTE]
    > Replace `<ServiceBusQueueName>` with your own queue name configured in your Service Bus namespace.

1. Save and close the *QueueReceiveController.java* file.

#### Receive messages from a Service Bus subscription

1. Using a text editor, create a Java file named *TopicReceiveController.java* in the package directory of your app. 

1. Add the following code to the new file. Replace the `<ServiceBusTopicName>` placeholder with your own topic name configured in your Service Bus namespace. Replace the `<ServiceBusSubscriptionName>` placeholder with your own subscription name for your Service Bus topic.

    ```java
    package com.wingtiptoys.servicebus;

    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.jms.annotation.JmsListener;
    import org.springframework.stereotype.Component;

    @Component
    public class TopicReceiveController {

        private static final String TOPIC_NAME = "<ServiceBusTopicName>";

        private static final String SUBSCRIPTION_NAME = "<ServiceBusSubscriptionName>";

        private final Logger logger = LoggerFactory.getLogger(TopicReceiveController.class);

        @JmsListener(destination = TOPIC_NAME, containerFactory = "topicJmsListenerContainerFactory",
                subscription = SUBSCRIPTION_NAME)
        public void receiveMessage(User user) {
            logger.info("Received message: {}", user.getName());
        }
    }
    ```

1. Save and close the *TopicReceiveController.java* file.

## Optional Service Bus Functionality

You can use a customized `MessageConverter` bean to convert between Java objects and JMS messages.

### Set the content-type of messages

The following code example sets the `BytesMessage` content-type to `application/json`. For more information, see [Messages, payloads, and serialization](/azure/service-bus-messaging/service-bus-messages-payloads).

```java
package com.wingtiptoys.servicebus;

import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.provider.amqp.message.AmqpJmsMessageFacade;
import org.apache.qpid.proton.amqp.Symbol;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.IOException;

@Component
public class CustomMessageConverter extends MappingJackson2MessageConverter {

    private static final String TYPE_ID_PROPERTY = "_type";
    private static final Symbol CONTENT_TYPE = Symbol.valueOf("application/json");

    public CustomMessageConverter() {
        this.setTargetType(MessageType.BYTES);
        this.setTypeIdPropertyName(TYPE_ID_PROPERTY);
    }

    @Override
    protected BytesMessage mapToBytesMessage(Object object, Session session, ObjectWriter objectWriter)
        throws JMSException, IOException {
        final BytesMessage bytesMessage = super.mapToBytesMessage(object, session, objectWriter);
        JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) bytesMessage;
        AmqpJmsMessageFacade facade = (AmqpJmsMessageFacade) jmsBytesMessage.getFacade();
        facade.setContentType(CONTENT_TYPE);
        return jmsBytesMessage;
    }
}
```

For more information about `MessageConverter`, see the official [Spring JMS guide](https://spring.io/guides/gs/messaging-jms/).

### Set session-id in JmsTemplate

Entities that have session support enabled, such as a session-enabled Service Bus queue, can only receive messages that have the `SessionId` set to a valid value. To send messages to such entities, use the `JmsTemplate.convertAndSend` method to set the string property "JMSXGroupID", which is mapped to the `SessionId` property, as shown in the following example:

```java
@RestController
public class QueueSendController {

    private static final String QUEUE_NAME = "<DestinationName>";

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueSendController.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping("/queue")
    public String postMessage(@RequestParam String message) {

        LOGGER.info("Sending message");

        jmsTemplate.convertAndSend(QUEUE_NAME, new User(message), jmsMessage -> {
            jmsMessage.setStringProperty("JMSXGroupID", "xxxeee");
            return jmsMessage;
        });
        return message;
    }
}
```

## Build and test your application

1. Open a command prompt and change directory to the location of your *pom.xml*; for example:

    ```cmd
    cd C:\SpringBoot\servicebus 
    ```

    -or-

    ```bash
    cd /users/example/home/servicebus 
    ```

1. Build your Spring Boot application with Maven and run it:

    ```shell
    mvn clean spring-boot:run
    ```

1. Once your application is running, you can use *curl* to test your application:

    ```shell
    curl -X POST localhost:8080/messages?message=hello
    ```

    You should see "Sending message" and "hello" posted to your application log:

    ```shell
    [nio-8080-exec-1] com.wingtiptoys.servicebus.SendController : Sending message
    [enerContainer-1] com.wingtiptoys.servicebus.ReceiveController : Received message: hello
    ```

## Clean up resources

When no longer needed, use the [Azure portal](https://portal.azure.com/) to delete the resources created in this article to avoid unexpected charges.

## Next steps

> [!div class="nextstepaction"]
> [How to use JMS API with Service Bus and AMQP 1.0](/azure/service-bus-messaging/service-bus-java-how-to-use-jms-api-amqp)
