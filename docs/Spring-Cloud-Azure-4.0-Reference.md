# Spring Cloud Azure 4.0 Reference

[TOC]

## Preface

Spring Cloud Azure offers a convenient way to interact with **Azure** provided services using well-known Spring idioms and APIs for Spring developers.

## [[ What is New in 4.0 since 3.10.x ]]

## Introduction

This first part of the reference documentation is a high-level overview of Spring Cloud Azure and the underlying concepts and some code snippets that can help you get up and running as quickly as possible.

This project consists of:
- [[ Spring Boot Support ]]
- [[ Spring Data Support ]]
- [[ Spring Security Support ]]
- [[ Spring Integration Support ]]
- [[ Spring Cloud Stream Support ]]

## [[ Getting Started ]]

## Reference

### [[ Configuration ]]

### [[ Authentication ]] (xiaolu)

### Production Ready

If you choose to add Spring Cloud Azure Actuator, include actuator dependencies.

```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>spring-cloud-azure-starter-actuator</artifactId>
</dependency>
```

#### Enable Health Indicator

 for Cosmos DB, add `management.health.azure-cosmos.enabled=true` to application.yaml.

```yml
management.health.azure-cosmos.enabled: true
```

// TODO add table for all servcies

> Warning: Call `http://{hostname}:{port}/actuator/health/cosmos` to get the Cosmos DB health info. **Please note**: it will calculate [RUs](https://docs.microsoft.com/azure/cosmos-db/request-units).

#### Enable Sleuth

#### Integrate with Azure Monitor

### Auto-Configure Azure SDK Clients (@xiaolu)



### Resource Handling （@zhihao)

#### Dependency Setup

#### Configuration

for full configurations, check appendix

#### Basic Usage

#### Samples



### Secret Management (kv secrets @rujun)

spring-cloud-azure-starter-keyvault-secrets adds Azure Key Vault as one of the Spring PropertySource, so secrets stored in Azure Key Vault could be easily used and conveniently accessed like other externalized configuration property, e.g. properties in files.

#### Dependency Setup
```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
</dependency>
```

#### Configuration
```yml
spring:
  cloud:
    azure:
      tenant-id: put-your-tenant-id-here
      client-id: put-your-client-id-here
      client-key: put-your-client-key-here
      keyvault:
        secret:
          # TODO
```

#### Basic Usage

#### Samples
```java
@SpringBootApplication
public class KeyVaultSample implements CommandLineRunner {

    @Value("${your-property-name}")
    private String mySecretProperty;

    public static void main(String[] args) {
        SpringApplication.run(KeyVaultSample.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("property your-property-name value is: " + mySecretProperty);
    }
}
```

### Spring Data Support (@zhihao)

#### Cosmos DB

brief introduction of the project

#### Dependency Setup

#### Configuration

for full configurations, check appendix

#### Basic Usage

#### Samples



### Spring Security with Azure AD (@rujun)

#### Dependency Setup
```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>spring-cloud-azure-starter-active-directory</artifactId>
</dependency>
```

#### Configuration

This starter provides the following properties:

| Properties                                                                             | Description                                                                                    |
| -------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| **spring.cloud.azure.active-directory**.app-id-uri                                                   | It used in resource server, used to validate the audience in access_token. access_token is valid only when the audience in access_token equal to client-id or app-id-uri    |
| **spring.cloud.azure.active-directory**.authorization-clients                                        | A map configure the resource APIs the application is going to visit. Each item corresponding to one resource API the application is going to visit. In Spring code, each item corresponding to one OAuth2AuthorizedClient object|
| **spring.cloud.azure.active-directory**.authorization-clients.{client-name}.scopes                   | API permissions of a resource server that the application is going to acquire.                 |
| **spring.cloud.azure.active-directory**.authorization-clients.{client-name}.on-demand                | This is used for incremental consent. The default value is false. If it's true, it's not consent when user login, when application needs the additional permission, incremental consent is performed with one OAuth2 authorization code flow.|
| **spring.cloud.azure.active-directory**.authorization-clients.{client-name}.authorization-grant-type | Type of authorization client. Supported types are [authorization_code] (default type for webapp), [on_behalf_of] (default type for resource-server), [client_credentials]. |
| **spring.cloud.azure.active-directory**.application-type                                             | Refer to [Application type](#application-type).|
| **spring.cloud.azure.active-directory**.base-uri                                                     | Base uri for authorization server, the default value is `https://login.microsoftonline.com/`.  |
| **spring.cloud.azure.active-directory**.client-id                                                    | Registered application ID in Azure AD.                                                         |
| **spring.cloud.azure.active-directory**.client-secret                                                | client secret of the registered application.                                                   |
| **spring.cloud.azure.active-directory**.graph-membership-uri                                         | It's used to load users' groups. The default value is `https://graph.microsoft.com/v1.0/me/memberOf`, this uri just get direct groups. To get all transitive membership, set it to `https://graph.microsoft.com/v1.0/me/transitiveMemberOf`. The 2 uris are both Azure Global, check `Property example 1` if you want to use Azure China.|
| **spring.cloud.azure.active-directory**.post-logout-redirect-uri                                     | Redirect uri for posting log-out.                            |
| **spring.cloud.azure.active-directory**.resource-server.principal-claim-name                         | Principal claim name. Default value is "sub".                                                  |
| **spring.cloud.azure.active-directory**.resource-server.claim-to-authority-prefix-map                | Claim to authority prefix map. Default map is: "scp" -> "SCOPE_", "roles" -> "APPROLE_".       |
| **spring.cloud.azure.active-directory**.tenant-id                                                    | Azure Tenant ID.                                             |
| **spring.cloud.azure.active-directory**.user-group.allowed-group-names                               | Users' group name can be use in `@PreAuthorize("hasRole('ROLE_group_name_1')")`. Not all group name will take effect, only group names configured in this property will take effect. |
| **spring.cloud.azure.active-directory**.user-group.allowed-group-ids                                 | Users' group id can be use in `@PreAuthorize("hasRole('ROLE_group_id_1')")`. Not all group id will take effect, only group id configured in this property will take effect. If this property's value is `all`, then all group id will take effect.|
| **spring.cloud.azure.active-directory**.user-name-attribute                                          | Decide which claim to be principal's name. |

Here are some examples about how to use these properties:

#### Property example 1: Use [Azure China] instead of Azure Global.

* Step 1: Add property in application.yml
```yaml
spring:
  cloud:
    azure:
      active-directory:
        base-uri: https://login.partner.microsoftonline.cn
        graph-base-uri: https://microsoftgraph.chinacloudapi.cn
```

#### Property example 2: Use `group name` or `group id` to protect some method in web application.

* Step 1: Add property in application.yml
```yaml
spring:
  cloud:
    azure:
      active-directory:
        user-group:
          allowed-group-names: group1_name_1, group2_name_2
          # 1. If allowed-group-ids = all, then all group id will take effect.
          # 2. If "all" is used, we should not configure other group ids.
          # 3. "all" is only supported for allowed-group-ids, not supported for allowed-group-names.
          allowed-group-ids: group_id_1, group_id_2
```

* Step 2: Add `@EnableGlobalMethodSecurity(prePostEnabled = true)` in web application:
    
```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AADOAuth2LoginSecurityConfig extends AADWebSecurityConfigurerAdapter {

    /**
     * Add configuration logic as needed.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
                .anyRequest().authenticated();
        // Do some custom configuration
    }
}
```

  Then we can protect the method by `@PreAuthorize` annotation:
```java
@Controller
public class RoleController {
    @GetMapping("group1")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_group1')")
    public String group1() {
        return "group1 message";
    }

    @GetMapping("group2")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_group2')")
    public String group2() {
        return "group2 message";
    }

    @GetMapping("group1Id")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_<group1-id>')")
    public String group1Id() {
        return "group1Id message";
    }

    @GetMapping("group2Id")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_<group2-id>')")
    public String group2Id() {
        return "group2Id message";
    }
}
```

#### Property example 3: [Incremental consent] in Web application visiting resource servers.

* Step 1: Add property in application.yml
```yaml
spring:
  cloud:
    azure:
      active-directory:
        authorization-clients:
          graph:
            scopes: https://graph.microsoft.com/Analytics.Read, email
          arm: # client registration id
            on-demand: true  # means incremental consent
            scopes: https://management.core.windows.net/user_impersonation
```

* Step 2: Write Java code:
```java
@GetMapping("/arm")
@ResponseBody
public String arm(
    @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient armClient
) {
    // toJsonString() is just a demo.
    // oAuth2AuthorizedClient contains access_token. We can use this access_token to access resource server.
    return toJsonString(armClient);
}
```

  After these steps. `arm`'s scopes (https://management.core.windows.net/user_impersonation) doesn't
  need to be consented at login time. When user request `/arm` endpoint, user need to consent the
  scope. That's `incremental consent` means.

  After the scopes have been consented, AAD server will remember that this user has already granted
  the permission to the web application. So incremental consent will not happen anymore after user
  consented.

#### Property example 4: [Client credential flow] in resource server visiting resource servers.

* Step 1: Add property in application.yml
```yaml
spring:
  cloud:
    azure:
      active-directory:
        authorization-clients:
          webapiC:                          # When authorization-grant-type is null, on behalf of flow is used by default
            authorization-grant-type: client_credentials
            scopes:
                - <Web-API-C-app-id-url>/.default
```

* Step 2: Write Java code:
```java
@PreAuthorize("hasAuthority('SCOPE_Obo.WebApiA.ExampleScope')")
@GetMapping("webapiA/webapiC")
public String callClientCredential() {
    String body = webClient
        .get()
        .uri(CUSTOM_LOCAL_READ_ENDPOINT)
        .attributes(clientRegistrationId("webapiC"))
        .retrieve()
        .bodyToMono(String.class)
        .block();
    LOGGER.info("Response from Client Credential: {}", body);
    return "client Credential response " + (null != body ? "success." : "failed.");
}
```

#### Basic Usage

#### Samples

##### Accessing a web application

This scenario uses the [The OAuth 2.0 authorization code grant] flow to login in a user with a Microsoft account.

**System diagram**:

![Standalone Web Application](https://user-images.githubusercontent.com/13167207/142617664-f1704adb-db64-49e0-b1b6-078c62b6945b.png)


* Step 1: Make sure `redirect URI` has been set to `{application-base-uri}/login/oauth2/code/`, for
  example `http://localhost:8080/login/oauth2/code/`. Note the tailing `/` cannot be omitted.

  ![web-application-set-redirect-uri-1.png](https://user-images.githubusercontent.com/13167207/142617751-154c156c-9035-4641-9b79-b26380ddad72.png)
  ![web-application-set-redirect-uri-2.png](https://user-images.githubusercontent.com/13167207/142617785-b4ca1afc-79f6-48ae-b7a3-99fba5856689.png)

* Step 2: Add the following dependencies in your pom.xml.

```xml
<dependencies>
	<dependency>
		<groupId>com.azure.spring</groupId>
		<artifactId>spring-cloud-azure-starter-active-directory</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-oauth2-client</artifactId>
	</dependency>
</dependencies>
```

* Step 3: Add properties in application.yml. These values should be got in [prerequisite].
```yaml
spring:
  cloud:
    azure:
      active-directory:
        tenant-id: xxxxxx-your-tenant-id-xxxxxx
        client-id: xxxxxx-your-client-id-xxxxxx
        client-secret: xxxxxx-your-client-secret-xxxxxx
```

* Step 4: Write your Java code:

  The `AADWebSecurityConfigurerAdapter` contains necessary web security configuration for **aad-starter**.

  (A). `DefaultAADWebSecurityConfigurerAdapter` is configured automatically if you not provide one.

  (B). You can provide one by extending `AADWebSecurityConfigurerAdapter` and call `super.configure(http)` explicitly
  in the `configure(HttpSecurity http)` function. Here is an example:
```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AADOAuth2LoginSecurityConfig extends AADWebSecurityConfigurerAdapter {

    /**
     * Add configuration logic as needed.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
                .anyRequest().authenticated();
        // Do some custom configuration
    }
}
```

##### Web application accessing resource servers

**System diagram**:

![web-application-visiting-resource-servers.png](https://user-images.githubusercontent.com/13167207/142617853-0526205f-fdef-47f9-ac01-77963f8c34be.png)

* Step 1: Make sure `redirect URI` has been set, just like [Accessing a web application].

* Step 2: Add the following dependencies in you pom.xml.

```xml
<dependencies>
	<dependency>
		<groupId>com.azure.spring</groupId>
		<artifactId>spring-cloud-azure-starter-active-directory</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-oauth2-client</artifactId>
	</dependency>
</dependencies>
```

* Step 3: Add properties in application.yml:
```yaml
spring:
  cloud:
    azure:
      active-directory:
        tenant-id: xxxxxx-your-tenant-id-xxxxxx
        client-id: xxxxxx-your-client-id-xxxxxx
        client-secret: xxxxxx-your-client-secret-xxxxxx
        authorization-clients:
          graph:
            scopes: https://graph.microsoft.com/Analytics.Read, email
```
  Here, `graph` is the name of `OAuth2AuthorizedClient`, `scopes` means the scopes need to consent when login.

* Step 4: Write your Java code:
```java
@GetMapping("/graph")
@ResponseBody
public String graph(
    @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphClient
) {
    // toJsonString() is just a demo.
    // oAuth2AuthorizedClient contains access_token. We can use this access_token to access resource server.
    return toJsonString(graphClient);
}
```
  Here, `graph` is the client name configured in step 2. OAuth2AuthorizedClient contains access_token.
  access_token can be used to access resource server.

##### Accessing a resource server
This scenario doesn't support login, just protect the server by validating the access_token. If the access token is valid, the server serves the request.

**System diagram**:

![Standalone resource server usage](https://user-images.githubusercontent.com/13167207/142617910-1ee3eb6a-ddc7-4b85-af4e-71344c91b248.png)

To use **aad-starter** in this scenario, we need these steps:

* Step 1: Add the following dependencies in you pom.xml.

```xml
<dependencies>
	<dependency>
		<groupId>com.azure.spring</groupId>
		<artifactId>spring-cloud-azure-starter-active-directory</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
	</dependency>
</dependencies>
```

* Step 2: Add properties in application.yml:
```yaml
spring:
  cloud:
    azure:
      active-directory:
        client-id: <client-id>
        app-id-uri: <app-id-uri>
```
  Both `client-id` and `app-id-uri` can be used to verify access token. `app-id-uri` can be get in Azure Portal:

  ![get-app-id-uri-1.png](https://user-images.githubusercontent.com/13167207/142617979-167e7509-b82e-4475-99b7-91bcf0ec249c.png)
  ![get-app-id-uri-2.png](https://user-images.githubusercontent.com/13167207/142618069-074289df-11aa-4d2c-ac8e-9a8a61c96288.png)

* Step 3: Write Java code:

  The `AADResourceServerWebSecurityConfigurerAdapter` contains necessary web security configuration for resource server.

  (A). `DefaultAADResourceServerWebSecurityConfigurerAdapter` is configured automatically if you not provide one.

  (B). You can provide one by extending `AADResourceServerWebSecurityConfigurerAdapter` and call `super.configure(http)` explicitly
  in the `configure(HttpSecurity http)` function. Here is an example:
```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AADOAuth2ResourceServerSecurityConfig extends AADResourceServerWebSecurityConfigurerAdapter {
    /**
     * Add configuration logic as needed.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests((requests) -> requests.anyRequest().authenticated());
    }
}
```

##### Resource server visiting other resource servers

This scenario support visit other resource servers in resource servers.

**System diagram**:

![resource-server-visiting-other-resource-servers.png](https://user-images.githubusercontent.com/13167207/142618294-aa546ced-d241-4fbd-97ac-fb06881503b1.png)

To use **aad-starter** in this scenario, we need these steps:

* Step 1: Add the following dependencies in you pom.xml.

```xml
<dependencies>
	<dependency>
		<groupId>com.azure.spring</groupId>
		<artifactId>spring-cloud-azure-starter-active-directory</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-oauth2-client</artifactId>
	</dependency>
</dependencies>
```

* Step 2: Add properties in application.yml:
```yaml
spring:
  cloud:
    azure:
      active-directory:
        tenant-id: <Tenant-id-registered-by-application>
        client-id: <Web-API-A-client-id>
        client-secret: <Web-API-A-client-secret>
        app-id-uri: <Web-API-A-app-id-url>
        authorization-clients:
          graph:
            scopes:
              - https://graph.microsoft.com/User.Read
```

* Step 3: Write Java code:

  Using `@RegisteredOAuth2AuthorizedClient` to access related resource server:

```java
    @PreAuthorize("hasAuthority('SCOPE_Obo.Graph.Read')")
    @GetMapping("call-graph")
    public String callGraph(@RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graph) {
        return callMicrosoftGraphMeEndpoint(graph);
    }
```

##### Web application and Resource server in one application

This scenario supports `Web application` and `Resource server` in one application.

To use **aad-starter** in this scenario, we need these steps:

* Step 1: Add the following dependencies in you pom.xml.

```xml
<dependencies>
	<dependency>
		<groupId>com.azure.spring</groupId>
		<artifactId>spring-cloud-azure-starter-active-directory</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-oauth2-client</artifactId>
	</dependency>
</dependencies>
```

* Step 2: Add properties in application.yml:

  Set property `spring.cloud.azure.active-directory.application-type` to `web_application_and_resource_server`, and specify the authorization type for each authorization client.

```yaml
spring:
  cloud:
    azure:
      active-directory:
        tenant-id: <Tenant-id-registered-by-application>
        client-id: <Web-API-C-client-id>
        client-secret: <Web-API-C-client-secret>
        app-id-uri: <Web-API-C-app-id-url>
        application-type: web_application_and_resource_server  # This is required.
        authorization-clients:
          graph:
            authorizationGrantType: authorization_code # This is required.
            scopes:
              - https://graph.microsoft.com/User.Read
              - https://graph.microsoft.com/Directory.Read.All
```

* Step 3: Write Java code:

  Configure multiple HttpSecurity instances, `AADOAuth2SecurityMultiConfig` contain two security configurations for resource server and web application.

    1. The class `ApiWebSecurityConfigurationAdapter` has a high priority to configure the `Resource Server` security adapter.

    1. The class `HtmlWebSecurityConfigurerAdapter` has a low priority to config the `Web Application` security adapter.

  Here is an example:

```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AADWebApplicationAndResourceServerConfig {

    @Order(1)
    @Configuration
    public static class ApiWebSecurityConfigurationAdapter extends AADResourceServerWebSecurityConfigurerAdapter {
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            // All the paths that match `/api/**`(configurable) work as `Resource Server`, other paths work as `Web application`.
            http.antMatcher("/api/**")
                .authorizeRequests().anyRequest().authenticated();
        }
    }

    @Configuration
    public static class HtmlWebSecurityConfigurerAdapter extends AADWebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            // @formatter:off
            http.authorizeRequests()
                    .antMatchers("/login").permitAll()
                    .anyRequest().authenticated();
            // @formatter:on
        }
    }
}
```

### Application type

This property(`spring.cloud.azure.active-directory.application-type`) is optional, its value can be inferred by dependencies, only `web_application_and_resource_server` must be configured manually: `spring.cloud.azure.active-directory.application-type=web_application_and_resource_server`.

| Has dependency: spring-security-oauth2-client | Has dependency: spring-security-oauth2-resource-server |                  Valid values of application type                                                     | Default value               |
|-----------------------------------------------|--------------------------------------------------------|-------------------------------------------------------------------------------------------------------|-----------------------------|
|                      Yes                      |                          No                            |                       `web_application`                                                               |       `web_application`     |
|                      No                       |                          Yes                           |                       `resource_server`                                                               |       `resource_server`     |
|                      Yes                      |                          Yes                           | `web_application`,`resource_server`,`resource_server_with_obo`, `web_application_and_resource_server` | `resource_server_with_obo`  |




### Spring Security with Azure AD B2C (@rujun)

#### Dependency Setup
```xml
<dependencies>
	<dependency>
		<groupId>com.azure.spring</groupId>
		<artifactId>spring-cloud-azure-starter-active-directory-b2c</artifactId>
	</dependency>
</dependencies>
```

#### Configuration

| Parameter                                                            | Description                                                                                                                     |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `spring.cloud.azure.active-directory.b2c.base-uri`                   | Base uri for authorization server, if both `tenant` and `baseUri` are configured at the same time, only `baseUri` takes effect. |
| `spring.cloud.azure.active-directory.b2c.client-id`                  | The registered application ID in Azure AD B2C.                                                                                  |
| `spring.cloud.azure.active-directory.b2c.client-secret`              | The client secret of a registered application.                                                                                  |
| `spring.cloud.azure.active-directory.b2c.authorization-clients`      | A map to list all authorization clients created on Azure Portal.                                                                |
| `spring.cloud.azure.active-directory.b2c.login-flow`                 | The key name of sign in user flow.                                                                                              |
| `spring.cloud.azure.active-directory.b2c.logout-success-url`         | The target URL after a successful logout.                                                                                       |   
| `spring.cloud.azure.active-directory.b2c.tenant(Deprecated)`         | The Azure AD B2C's tenant name, this is only suitable for Global cloud.                                                         |
| `spring.cloud.azure.active-directory.b2c.tenant-id`                  | The Azure AD B2C's tenant id.                                                                                                   |
| `spring.cloud.azure.active-directory.b2c.user-flows`                 | A map to list all user flows defined on Azure Portal.                                                                           |
| `spring.cloud.azure.active-directory.b2c.user-name-attribute-name`   | The the attribute name of the user name.                                                                                        |

For full configurations, check appendix.

#### Basic Usage

#### Key concepts

A `web application` is any web based application that allows user to login Azure AD, whereas a `resource server` will either
accept or deny access after validating access_token obtained from Azure AD. We will cover 4 scenarios in this guide:

1. Accessing a web application.
1. Web application accessing resource servers.
1. Accessing a resource server.
1. Resource server accessing other resource servers.

![B2C Web application & Web Api Overall](https://user-images.githubusercontent.com/13167207/142620440-f970b572-2646-4f50-9f77-db62d6e965f1.png)

##### Accessing a web application

This scenario uses the [The OAuth 2.0 authorization code grant] flow to login in a user with your Azure AD B2C user.

1. Select **Azure AD B2C** from the portal menu, click **Applications**, and then click **Add**.

1. Specify your application **Name**, we call it `webapp`, add `http://localhost:8080/login/oauth2/code/` for the **Reply URL**, record the
   **Application ID** as your `${your-webapp-client-id}` and then click **Save**.

1. Select **Keys** from your application, click **Generate key** to generate `${your-webapp-client-secret}` and then **Save**.

1. Select **User flows** on your left, and then Click **New user flow**.

1. Choose **Sign up or in**, **Profile editing** and **Password reset** to create user flows
   respectively. Specify your user flow **Name** and **User attributes and claims**, click **Create**.

1. Select **API permissions** > **Add a permission** > **Microsoft APIs**, select ***Microsoft Graph***,
   select **Delegated permissions**, check **offline_access** and **openid** permissions, select **Add permission** to complete the process.

1. Grant admin consent for ***Graph*** permissions.
   ![Add Graph permissions](https://user-images.githubusercontent.com/13167207/142620491-8c8a82ea-c920-43a8-aa0a-dd028f1b8553.png)

1. Add the following dependencies in your *pom.xml*.

```xml
<dependencies>
	<dependency>
		<groupId>com.azure.spring</groupId>
		<artifactId>azure-spring-boot-starter-active-directory-b2c</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-web</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-thymeleaf</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-security</artifactId>
	</dependency>
	<dependency>
		<groupId>org.thymeleaf.extras</groupId>
		<artifactId>thymeleaf-extras-springsecurity5</artifactId>
	</dependency>
</dependencies>
```

1. Add properties in *application.yml* using the values you created earlier, for example:

```yaml
spring:
  cloud:
   azure:
     active-directory:
       b2c:
         authenticate-additional-parameters: 
           domain_hint: xxxxxxxxx         # optional
           login_hint: xxxxxxxxx          # optional
           prompt: [login,none,consent]   # optional
         base-uri: ${your-tenant-authorization-server-base-uri}
         client-id: ${your-webapp-client-id}
         client-secret: ${your-webapp-client-secret}
         login-flow: ${your-login-user-flow-key}               # default to sign-up-or-sign-in, will look up the user-flows map with provided key.
         logout-success-url: ${you-logout-success-url}
         user-flows:
           ${your-user-flow-key}: ${your-user-flow-name-defined-on-azure-portal}
         user-name-attribute-name: ${your-user-name-attribute-name}
```

1. Write your Java code.

   Controller code can refer to the following:
```java
@Controller
public class WebController {

    private void initializeModel(Model model, OAuth2AuthenticationToken token) {
        if (token != null) {
            final OAuth2User user = token.getPrincipal();

            model.addAllAttributes(user.getAttributes());
            model.addAttribute("grant_type", user.getAuthorities());
            model.addAttribute("name", user.getName());
        }
    }

    @GetMapping(value = { "/", "/home" })
    public String index(Model model, OAuth2AuthenticationToken token) {
        initializeModel(model, token);
        return "home";
    }
}
```

   Security configuration code can refer to the following:
```java
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AADB2COidcLoginConfigurer configurer;

    public WebSecurityConfiguration(AADB2COidcLoginConfigurer configurer) {
        this.configurer = configurer;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .apply(configurer);
        // @formatter:off
    }
}
```

   Copy the *home.html* from [Azure AD B2C Spring Boot Sample](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc/src/main/resources/templates), and replace the `${your-profile-edit-user-flow}` and `${your-password-reset-user-flow}` with your user flow name respectively that completed earlier.

1. Build and test your app

   Let `Webapp` run on port *8080*.

    1. After your application is built and started by Maven, open `http://localhost:8080/` in a web browser, you should be redirected to login page.

    1. Click link with the login user flow, you should be redirected Azure AD B2C to start the authentication process.

    1. After you have logged in successfully, you should see the sample `home page` from the browser.

##### Web application accessing resource servers

This scenario is based on **Accessing a web application** scenario to allow application to access other resources, that is [The OAuth 2.0 client credentials grant] flow.

1. Select **Azure AD B2C** from the portal menu, click **Applications**, and then click **Add**.

1. Specify your application **Name**, we call it `webApiA`, record the **Application ID** as your `${your-web-api-a-client-id}` and then click **Save**.

1. Select **Keys** from your application, click **Generate key** to generate `${your-web-api-a-client-secret}` and then **Save**.

1. Select **Expose an API** on your left, and then Click the **Set** link, specify your resource app id url suffix, such as *web-api-a*,
   record the **Application ID URI** as your `${your-web-api-a-app-id-url}`, then **Save**.

1. Select **Manifest** on your left, and then paste the below json segment into `appRoles` array,
   record the **Application ID URI** as your `${your-web-api-a-app-id-url}`, record the value of the app role as your `${your-web-api-a-role-value}`, then **save**.

```json
{
  "allowedMemberTypes": [
    "Application"
  ],
  "description": "WebApiA.SampleScope",
  "displayName": "WebApiA.SampleScope",
  "id": "04989db0-3efe-4db6-b716-ae378517d2b7",
  "isEnabled": true,
  "value": "WebApiA.SampleScope"
}
```

   ![Configure WebApiA appRoles](https://user-images.githubusercontent.com/13167207/142620567-59a91df7-7a97-4027-b525-1f422f25fb22.png)

1. Select **API permissions** > **Add a permission** > **My APIs**, select ***WebApiA*** application name,
   select **Application Permissions**, select **WebApiA.SampleScope** permission, select **Add permission** to complete the process.

1. Grant admin consent for ***WebApiA*** permissions.
   ![Add WebApiA permission](https://user-images.githubusercontent.com/13167207/142620601-660400fa-7cff-4989-9d7f-2b32a9aa1244.png)

1. Add the following dependency on the basis of **Accessing a web application** scenario.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

1. Add the following configuration on the basis of **Accessing a web application** scenario.

```yaml
spring:
  cloud:
   azure:
     active-directory:
       b2c:
         base-uri: ${your-base-uri}             # Such as: https://xxxxb2c.b2clogin.com
         tenant-id: ${your-tenant-id}
         authorization-clients:
           ${your-resource-server-a-name}:
             authorization-grant-type: client_credentials
             scopes: ${your-web-api-a-app-id-url}/.default
```

1. Write your `Webapp` Java code.

   Controller code can refer to the following:
```java
/**
 * Access to protected data from Webapp to WebApiA through client credential flow. The access token is obtained by webclient, or
 * <p>@RegisteredOAuth2AuthorizedClient("webApiA")</p>. In the end, these two approaches will be executed to
 * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
 *
 * @return Respond to protected data from WebApi A.
 */
@GetMapping("/webapp/webApiA")
public String callWebApiA() {
    String body = webClient
        .get()
        .uri(LOCAL_WEB_API_A_SAMPLE_ENDPOINT)
        .attributes(clientRegistrationId("webApiA"))
        .retrieve()
        .bodyToMono(String.class)
        .block();
    LOGGER.info("Call callWebApiA(), request '/webApiA/sample' returned: {}", body);
    return "Request '/webApiA/sample'(WebApi A) returned a " + (body != null ? "success." : "failure.");
}
```

   Security configuration code is the same with **Accessing a web application** scenario, another bean `webClient`is added as follows:
```java
@Bean
public WebClient webClient(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
    ServletOAuth2AuthorizedClientExchangeFilterFunction function =
        new ServletOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager);
    return WebClient.builder()
                    .apply(function.oauth2Configuration())
                    .build();
}
```

1. Please refer to **Accessing a resource server** section to write your `WebApiA` Java code.

1. Build and test your app

   Let `Webapp` and `WebApiA` run on port *8080* and *8081* respectively.
   Start `Webapp` and `WebApiA` application, return to the home page after logging successfully, you can access `http://localhost:8080/webapp/webApiA` to get **WebApiA** resource response.

##### Accessing a resource server

This scenario not support login. Just protect the server by validating the access token, and if valid, serves the request.

1. Refer to [Web application accessing resource servers][web_application_accessing_resource_servers] to build your `WebApiA` permission.

1. Add `WebApiA` permission and grant admin consent for your web application.

1. Add the following dependencies in your *pom.xml*.

```xml
<dependency>
 <groupId>com.azure.spring</groupId>
 <artifactId>azure-spring-boot-starter-active-directory-b2c</artifactId>
</dependency>

<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

1. Add the following configuration.

```yaml
spring:
  cloud:
   azure:
     active-directory:
       b2c:
         base-uri: ${your-base-uri}             # Such as: https://xxxxb2c.b2clogin.com
         tenant-id: ${your-tenant-id}
         app-id-uri: ${your-app-id-uri}         # If you are using v1.0 token, please configure app-id-uri for `aud` verification
         client-id: ${your-client-id}           # If you are using v2.0 token, please configure client-id for `aud` verification
```

1. Write your Java code.

   Controller code can refer to the following:
```java
/**
 * webApiA resource api for web app
 * @return test content
 */
@PreAuthorize("hasAuthority('APPROLE_WebApiA.SampleScope')")
@GetMapping("/webApiA/sample")
public String webApiASample() {
    LOGGER.info("Call webApiASample()");
    return "Request '/webApiA/sample'(WebApi A) returned successfully.";
}
```

   Security configuration code can refer to the following:
```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests((requests) -> requests.anyRequest().authenticated())
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(new AADJwtBearerTokenAuthenticationConverter());
    }
}
```

1. Build and test your app

   Let `WebApiA` run on port *8081*.
   Get the access token for `webApiA` resource and access `http://localhost:8081/webApiA/sample`
   as the Bearer authorization header.

### Resource server accessing other resource servers

This scenario is an upgrade of **Accessing a resource server**, supports access to other application resources, based on OAuth2 client credentials flow.

1. Referring to the previous steps, we create a `WebApiB` application and expose an application permission `WebApiB.SampleScope`.

```json
{
    "allowedMemberTypes": [
        "Application"
    ],
    "description": "WebApiB.SampleScope",
    "displayName": "WebApiB.SampleScope",
    "id": "04989db0-3efe-4db6-b716-ae378517d2b7",
    "isEnabled": true,
    "lang": null,
    "origin": "Application",
    "value": "WebApiB.SampleScope"
}
```

   ![Configure WebApiB appRoles](https://user-images.githubusercontent.com/13167207/142620648-cfbf5220-9736-4050-a3ef-1370c522e672.png)

2. Grant admin consent for ***WebApiB*** permissions.
   ![Add WebApiB permission](https://user-images.githubusercontent.com/13167207/142620691-b1a7fcda-fc92-41af-9515-812139f26ee0.png)

3. On the basis of **Accessing a resource server**, add a dependency in your *pom.xml*.

```xml
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

4. Add the following configuration on the basis of **Accessing a resource server** scenario configuration.

```yaml
spring:
  cloud:
   azure:
     active-directory:
       b2c:
         client-secret: ${your-web-api-a-client-secret}
         authorization-clients:
           ${your-resource-server-b-name}:
             authorization-grant-type: client_credentials
             scopes: ${your-web-api-b-app-id-url}/.default
```

5. Write your Java code.

   WebApiA controller code can refer to the following:
```java
/**
 * Access to protected data from WebApiA to WebApiB through client credential flow. The access token is obtained by webclient, or
 * <p>@RegisteredOAuth2AuthorizedClient("webApiA")</p>. In the end, these two approaches will be executed to
 * DefaultOAuth2AuthorizedClientManager#authorize method, get the access token.
 *
 * @return Respond to protected data from WebApi B.
 */
@GetMapping("/webApiA/webApiB/sample")
@PreAuthorize("hasAuthority('APPROLE_WebApiA.SampleScope')")
public String callWebApiB() {
    String body = webClient
        .get()
        .uri(LOCAL_WEB_API_B_SAMPLE_ENDPOINT)
        .attributes(clientRegistrationId("webApiB"))
        .retrieve()
        .bodyToMono(String.class)
        .block();
    LOGGER.info("Call callWebApiB(), request '/webApiB/sample' returned: {}", body);
    return "Request 'webApiA/webApiB/sample'(WebApi A) returned a " + (body != null ? "success." : "failure.");
}
```

   WebApiB controller code can refer to the following:
```java
/**
 * webApiB resource api for other web application
 * @return test content
 */
@PreAuthorize("hasAuthority('APPROLE_WebApiB.SampleScope')")
@GetMapping("/webApiB/sample")
public String webApiBSample() {
    LOGGER.info("Call webApiBSample()");
    return "Request '/webApiB/sample'(WebApi B) returned successfully.";
}
```

   Security configuration code is the same with **Accessing a resource server** scenario, another bean `webClient`is added as follows

6. Build and test your app

   Let `WebApiA` and `WebApiB` run on port *8081* and *8082* respectively.
   Start `WebApiA` and `WebApiB` application, get the access token for `webApiA` resource and access `http://localhost:8081/webApiA/webApiB/sample`
   as the Bearer authorization header.


#### Samples

##### Accessing a web application
Please refer to [azure-spring-boot-sample-active-directory-b2c-oidc]. (todo: @chenrujun update this link)

##### Accessing a resource server
Please refer to [azure-spring-boot-sample-active-directory-b2c-resource-server].

### Spring Integration Support (@yiliu)

Provide Spring Integration support for these Azure serices: eh, sb, storage q

#### Spring Integration with Azure Event Hubs

##### Dependency Setup

```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>azure-spring-integration-eventhubs</artifactId>
</dependency>
```

##### Configuration


for full configurations, check appendix

##### Basic Usage

##### Samples
Please refer to this [sample project](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/eventhubs/azure-spring-integration-sample-eventhubs) to learn how to use Event Hubs integration. (todo: @chenrujun, update this link)

#### Spring Integration with Azure Service Bus

##### Dependency Setup

```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>azure-spring-integration-servicebus</artifactId>
</dependency>
```

##### Configuration

for full configurations, check appendix

##### Basic Usage

##### Samples

**Example: Manually set the partition key for the message**

This example demonstrates how to manually set the partition key for the message in the application.

**Way 1:**
This example requires that `spring.cloud.stream.default.producer.partitionKeyExpression` be set `"'partitionKey-' + headers[<message-header-key>]"`.
```yaml
spring:
  cloud:
    azure:
      servicebus:
        connection-string: [servicebus-namespace-connection-string]
    stream:
      default:
        producer:
          partitionKeyExpression:  "'partitionKey-' + headers[<message-header-key>]"
```
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader("<message-header-key>", "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

> **NOTE:** When using `application.yml` to configure the partition key, its priority will be the lowest.
> It will take effect only when the `ServiceBusMessageHeaders.SESSION_ID`, `ServiceBusMessageHeaders.PARTITION_KEY`, `AzureHeaders.PARTITION_KEY` are not configured.
**Way 2:**
Manually add the partition Key in the message header by code.

*Recommended:* Use `ServiceBusMessageHeaders.PARTITION_KEY` as the key of the header.
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(ServiceBusMessageHeaders.PARTITION_KEY, "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

*Not recommended but currently supported:* `AzureHeaders.PARTITION_KEY` as the key of the header.
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(AzureHeaders.PARTITION_KEY, "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```
> **NOTE:** When both `ServiceBusMessageHeaders.PARTITION_KEY` and `AzureHeaders.PARTITION_KEY` are set in the message headers,
> `ServiceBusMessageHeaders.PARTITION_KEY` is preferred.
**Example: Set the session id for the message**

This example demonstrates how to manually set the session id of a message in the application.

```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(ServiceBusMessageHeaders.SESSION_ID, "Customize session id")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

> **NOTE:** When the `ServiceBusMessageHeaders.SESSION_ID` is set in the message headers, and a different `ServiceBusMessageHeaders.PARTITION_KEY` (or `AzureHeaders.PARTITION_KEY`) header is also set,
> the value of the session id will eventually be used to overwrite the value of the partition key.


#### Spring Integration with Azure Event Hubs

#### Dependency Setup

```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>azure-spring-integration-storage-queue</artifactId>
</dependency>
```

#### Configuration

for full configurations, check appendix

#### Basic Usage

#### Samples

Please refer to this [sample project](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/storage/azure-spring-integration-sample-storage-queue) illustrating how to use Storage Queue integration.

### Spring Cloud Stream Support (@gary)

#### Spring Cloud Stream Binder for Azure Event Hubs

##### Dependency Setup
```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>spring-cloud-azure-stream-binder-eventhubs</artifactId>
</dependency>
```

##### Configuration

The binder provides the following configuration options in `application.properties`.

###### Spring Cloud Azure Properties ######

|Name | Description | Required | Default
|:---|:---|:---|:---
spring.cloud.azure.auto-create-resources | If enable auto-creation for Azure resources |  | false
spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes if spring.cloud.azure.auto-create-resources is enabled. |
spring.cloud.azure.environment | Azure Cloud name for Azure resources, supported values are  `azure`, `azurechina`, `azure_germany` and `azureusgovernment` which are case insensitive | |azure | 
spring.cloud.azure.client-id | Client (application) id of a service principal or Managed Service Identity (MSI) | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.client-secret | Client secret of a service principal | Yes if service principal is used as credential configuration. |
spring.cloud.azure.msi-enabled | If enable MSI as credential configuration | Yes if MSI is used as credential configuration. | false
spring.cloud.azure.resource-group | Name of Azure resource group | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.subscription-id | Subscription id of an MSI | Yes if MSI is used as credential configuration. |
spring.cloud.azure.tenant-id | Tenant id of a service principal | Yes if service principal is used as credential configuration. |
spring.cloud.azure.eventhub.connection-string | Event Hubs Namespace connection string | Yes if connection string is used as Event Hubs credential configuration |
spring.cloud.azure.eventhub.checkpoint-storage-account | StorageAccount name for message checkpoint | Yes
spring.cloud.azure.eventhub.checkpoint-access-key | StorageAccount access key for message checkpoint | Yes if StorageAccount access key is used as StorageAccount credential configuration
spring.cloud.azure.eventhub.checkpoint-container | StorageAccount container name for message checkpoint | Yes
spring.cloud.azure.eventhub.namespace | Event Hub Namespace. Auto creating if missing | Yes if service principal or MSI is used as credential configuration. |

###### Common Producer Properties ######

You can use the producer configurations of **Spring Cloud Stream**,
it uses the configuration with the format of `spring.cloud.stream.bindings.<channelName>.producer`.

####### Partition configuration

The system will obtain the parameter `PartitionSupply` to send the message,
the following is the process of obtaining the priority of the partition ID and key:

![Create PartitionSupply parameter process](https://user-images.githubusercontent.com/13167207/142611562-38dfd834-47e6-4b8c-ba7d-b811f88a2821.png)

The following are configuration items related to the producer:

**_partition-count_**

The number of target partitions for the data, if partitioning is enabled.

Default: 1

**_partition-key-extractor-name_**

The name of the bean that implements `PartitionKeyExtractorStrategy`.
The partition handler will first use the `PartitionKeyExtractorStrategy#extractKey` method to obtain the partition key value.

Default: null

**_partition-key-expression_**

A SpEL expression that determines how to partition outbound data.
When interface `PartitionKeyExtractorStrategy` is not implemented, it will be called in the method `PartitionHandler#extractKey`.

Default: null

For more information about setting partition for the producer properties, please refer to the [Producer Properties of Spring Cloud Stream][spring_cloud_stream_current_producer_properties].

###### Event Hub Producer Properties ######

It supports the following configurations with the format of `spring.cloud.stream.eventhubs.bindings.<channelName>.producer`.

**_sync_**

Whether the producer should act in a synchronous manner with respect to writing messages into a stream. If true, the
producer will wait for a response from Event Hub after a send operation.

Default: `false`

**_send-timeout_**

Effective only if `sync` is set to true. The amount of time to wait for a response from Event Hub after a send operation, in milliseconds.

Default: `10000`

###### Common Consumer Properties ######

You can use the below consumer configurations of **Spring Cloud Stream**,
it uses the configuration with the format of `spring.cloud.stream.bindings.<channelName>.consumer`.

####### Batch Consumer

When `spring.cloud.stream.bindings.<binding-name>.consumer.batch-mode` is set to `true`, all of the received events
will be presented as a `List<?>` to the consumer function. Otherwise, the function will be called with one event at a time.
The size of the batch is controlled by Event Hubs consumer properties `max-size`(required) and `max-wait-time`
(optional); refer to the [below section](#event-hub-consumer-properties) for more information.

**_batch-mode_**

Whether to enable the entire batch of messages to be passed to the consumer function in a `List`.

Default: `False`

###### Event Hub Consumer Properties ######

It supports the following configurations with the format of `spring.cloud.stream.eventhubs.bindings.<channelName>.consumer`.

**_start-position_**

Whether the consumer receives messages from the beginning or end of event hub. if `EARLIEST`, from beginning. If
`LATEST`, from end.

Default: `LATEST`

**_checkpoint-mode_**

The mode in which checkpoints are updated.

`RECORD`, `default` mode. Checkpoints occur after each record is successfully processed by user-defined message
handler without any exception. If you use `StorageAccount` as checkpoint store, this might become bottleneck.

`BATCH`, checkpoints occur after each batch of messages successfully processed by user-defined message handler
without any exception. Be aware that batch size could be any value and `BATCH` mode is only supported when consume
batch
mode is set true.

`MANUAL`, checkpoints occur on demand by the user via the `Checkpointer`. You can do checkpoints after the message has been successfully processed. `Message.getHeaders.get(AzureHeaders.CHECKPOINTER)`callback can get you the `Checkpointer` you need. Please be aware all messages in the corresponding Event Hub partition before this message will be considered as successfully processed.

`PARTITION_COUNT`, checkpoints occur after the count of messages defined by `checkpoint_count` successfully processed for each partition. You may experience reprocessing at most `checkpoint_count` of  when message processing fails.

`Time`, checkpoints occur at fixed time interval specified by `checkpoint_interval`. You may experience reprocessing of messages during this time interval when message processing fails.

Default: `RECORD`

    Notes: when consume batch mode is false(default value), `BATCH` checkpoint mode is not invalid.

**_checkpoint-count_**

Effectively only when `checkpoint-mode` is `PARTITION_COUNT`. Decides the amount of message for each partition to do one checkpoint.

Default: `10`

**_checkpoint-interval_**

Effectively only when `checkpoint-mode` is `Time`. Decides The time interval to do one checkpoint.

Default: `5s`

**_max-size_**

The maximum number of events that will be in the list of a message payload when the consumer callback is invoked.

Default: `10`

**_max-wait-time_**

The max time `Duration` to wait to receive a batch of events up to the max batch size before invoking the consumer callback.

Default: `null`

for full configurations, check appendix

##### Basic Usage

##### Samples

####### Error Channels
**_consumer error channel_**

this channel is open by default, you can handle the error message in this way:
```
    // Replace destination with spring.cloud.stream.bindings.input.destination
    // Replace group with spring.cloud.stream.bindings.input.group
    @ServiceActivator(inputChannel = "{destination}.{group}.errors")
    public void consumerError(Message<?> message) {
        LOGGER.error("Handling customer ERROR: " + message);
    }
```

**_producer error channel_**

this channel is not open by default, if you want to open it. You need to add a configuration in your application.properties, like this:
```
spring.cloud.stream.default.producer.errorChannelEnabled=true
```

you can handle the error message in this way:
```
    // Replace destination with spring.cloud.stream.bindings.output.destination
    @ServiceActivator(inputChannel = "{destination}.errors")
    public void producerError(Message<?> message) {
        LOGGER.error("Handling Producer ERROR: " + message);
    }
```

####### Batch Consumer Sample

######## Configuration Options
To enable the batch consumer mode, you should add below configuration
```yaml
spring:
  cloud:
    stream:
      bindings:
        consume-in-0:
          destination: {event-hub-name}
          group: [consumer-group-name]
          consumer:
            batch-mode: true 
      eventhubs:
        bindings:
          consume-in-0:
            consumer:
              checkpoint:
                mode: BATCH # or MANUAL as needed
              batch:
                max-size: 2 # The default value is 10
                max-wait-time: 1m # Optional, the default value is null
```

######## Consume messages in batches
For checkpointing mode as BATCH, you can use below code to send messages and consume in batches.
```java
    @Bean
    public Consumer<List<String>> consume() {
        return list -> list.forEach(event -> LOGGER.info("New event received: '{}'",event));
    }
    @Bean
    public Supplier<Message<String>> supply() {
        return () -> {
            LOGGER.info("Sending message, sequence " + i);
            return MessageBuilder.withPayload("\"test"+ i++ +"\"").build();
        };
    }
```

For checkpointing mode as MANUAL, you can use below code to send messages and consume/checkpoint in batches.
```java
    @Bean
    public Consumer<Message<List<String>>> consume() {
        return message -> {
            for (int i = 0; i < message.getPayload().size(); i++) {
                LOGGER.info("New message received: '{}', partition key: {}, sequence number: {}, offset: {}, enqueued time: {}",
                    message.getPayload().get(i),
                    ((List<Object>) message.getHeaders().get(EventHubsHeaders.PARTITION_KEY)).get(i),
                    ((List<Object>) message.getHeaders().get(EventHubsHeaders.SEQUENCE_NUMBER)).get(i),
                    ((List<Object>) message.getHeaders().get(EventHubsHeaders.OFFSET)).get(i),
                    ((List<Object>) message.getHeaders().get(EventHubsHeaders.ENQUEUED_TIME)).get(i));
            }
        
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            checkpointer.success()
                        .doOnSuccess(success -> LOGGER.info("Message '{}' successfully checkpointed", message.getPayload()))
                        .doOnError(error -> LOGGER.error("Exception found", error))
                        .subscribe();
        };
    }
    @Bean
    public Supplier<Message<String>> supply() {
        return () -> {
            LOGGER.info("Sending message, sequence " + i);
            return MessageBuilder.withPayload("\"test"+ i++ +"\"").build();
        };
    }
```

#### Spring Cloud Stream Binder for Azure Service Bus

##### Dependency Setup
```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>spring-cloud-azure-stream-binder-servicebus</artifactId>
</dependency>
```

##### Configuration

The binder provides the following configuration options:

###### Spring Cloud Azure Properties

|Name | Description | Required | Default
|:---|:---|:---|:---
spring.cloud.azure.auto-create-resources | If enable auto-creation for Azure resources |  | false
spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes if spring.cloud.azure.auto-create-resources is enabled. |
spring.cloud.azure.environment | Azure Cloud name for Azure resources, supported values are  `azure`, `azurechina`, `azure_germany` and `azureusgovernment` which are case insensitive | |azure | 
spring.cloud.azure.client-id | Client (application) id of a service principal or Managed Service Identity (MSI) | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.client-secret | Client secret of a service principal | Yes if service principal is used as credential configuration. |
spring.cloud.azure.msi-enabled | If enable MSI as credential configuration | Yes if MSI is used as credential configuration. | false
spring.cloud.azure.resource-group | Name of Azure resource group | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.subscription-id | Subscription id of an MSI | Yes if MSI is used as credential configuration. |
spring.cloud.azure.tenant-id | Tenant id of a service principal | Yes if service principal is used as credential configuration. |
spring.cloud.azure.servicebus.connection-string | Service Bus Namespace connection string | Yes if connection string is used as credential configuration |
spring.cloud.azure.servicebus.namespace | Service Bus Namespace. Auto creating if missing | Yes if service principal or MSI is used as credential configuration. |
spring.cloud.azure.servicebus.transportType | Service Bus transportType, supported value of `AMQP` and `AMQP_WEB_SOCKETS` | No | `AMQP`
spring.cloud.azure.servicebus.retry-Options | Service Bus retry options | No | Default value of AmqpRetryOptions

###### Partition configuration

The system will obtain the parameter `PartitionSupply` to send the message.

The following are configuration items related to the producer:

**_partition-count_**

The number of target partitions for the data, if partitioning is enabled.

Default: 1

**_partition-key-extractor-name_**

The name of the bean that implements `PartitionKeyExtractorStrategy`.
The partition handler will first use the `PartitionKeyExtractorStrategy#extractKey` method to obtain the partition key value.

Default: null

**_partition-key-expression_**

A SpEL expression that determines how to partition outbound data.
When interface `PartitionKeyExtractorStrategy` is not implemented, it will be called in the method `PartitionHandler#extractKey`.

Default: null

For more information about setting partition for the producer properties, please refer to the [Producer Properties of Spring Cloud Stream][spring_cloud_stream_current_producer_properties].

###### Serivce Bus Queue Producer Properties

It supports the following configurations with the format of `spring.cloud.stream.servicebus.queue.bindings.<channelName>.producer`.

**_sync_**

Whether the producer should act in a synchronous manner with respect to writing messages into a stream. If true, the
producer will wait for a response after a send operation.

Default: `false`

**_send-timeout_**

Effective only if `sync` is set to true. The amount of time to wait for a response after a send operation, in milliseconds.

Default: `10000`

###### Service Bus Queue Consumer Properties

It supports the following configurations with the format of `spring.cloud.stream.servicebus.queue.bindings.<channelName>.consumer`.

**_checkpoint-mode_**

The mode in which checkpoints are updated.

`RECORD`, checkpoints occur after each record successfully processed by user-defined message handler without any exception.

`MANUAL`, checkpoints occur on demand by the user via the `Checkpointer`. You can get `Checkpointer` by `Message.getHeaders.get(AzureHeaders.CHECKPOINTER)`callback.

Default: `RECORD`

**_prefetch-count_**

Prefetch count of underlying service bus client.

Default: `1`

**_maxConcurrentCalls_**

Controls the max concurrent calls of service bus message handler and the size of fixed thread pool that handles user's business logic

Default: `1`

**_maxConcurrentSessions_**

Controls the maximum number of concurrent sessions to process at any given time.

Default: `1`

**_concurrency_**

When `sessionsEnabled` is true, controls the maximum number of concurrent sessions to process at any given time.
When `sessionsEnabled` is false, controls the max concurrent calls of service bus message handler and the size of fixed thread pool that handles user's business logic.

Deprecated, replaced with `maxConcurrentSessions` when `sessionsEnabled` is true and `maxConcurrentCalls` when `sessionsEnabled` is false

Default: `1`

**_sessionsEnabled_**

Controls if is a session aware consumer. Set it to `true` if is a queue with sessions enabled.

Default: `false`

**_requeueRejected_**

Controls if is a message that trigger any exception in consumer will be force to DLQ.
Set it to `true` if a message that trigger any exception in consumer will be force to DLQ.
Set it to `false` if a message that trigger any exception in consumer will be re-queued.

Default: `false`

**_receiveMode_**

The modes for receiving messages.

`PEEK_LOCK`, received message is not deleted from the queue or subscription, instead it is temporarily locked to the receiver, making it invisible to other receivers.

`RECEIVE_AND_DELETE`, received message is removed from the queue or subscription and immediately deleted.

Default: `PEEK_LOCK`

**_enableAutoComplete_**

Enable auto-complete and auto-abandon of received messages.
'enableAutoComplete' is not needed in for RECEIVE_AND_DELETE mode.

Default: `false`
###### Support for Service Bus Message Headers and Properties
The following table illustrates how Spring message headers are mapped to Service Bus message headers and properties.
When create a message, developers can specify the header or property of a Service Bus message by below constants.

For some Service Bus headers that can be mapped to multiple Spring header constants, the priority of different Spring headers is listed.

Service Bus Message Headers and Properties | Spring Message Header Constants | Type | Priority Number (Descending priority)
---|---|---|---
**MessageId** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.MESSAGE_ID | String | 1
**MessageId** | com.azure.spring.integration.core.AzureHeaders.RAW_ID | String | 2
**MessageId** | org.springframework.messaging.MessageHeaders.ID | UUID | 3
ContentType | org.springframework.messaging.MessageHeaders.CONTENT_TYPE | String | N/A
ReplyTo | org.springframework.messaging.MessageHeaders.REPLY_CHANNEL | String | N/A
**ScheduledEnqueueTimeUtc** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME | OffsetDateTime | 1
**ScheduledEnqueueTimeUtc** | com.azure.spring.integration.core.AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE | Integer | 2
TimeToLive | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TIME_TO_LIVE | Duration | N/A
SessionID | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.SESSION_ID | String | N/A
CorrelationId | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.CORRELATION_ID | String | N/A
To | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.TO | String | N/A
ReplyToSessionId | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.REPLY_TO_SESSION_ID | String | N/A
**PartitionKey** | com.azure.spring.integration.servicebus.converter.ServiceBusMessageHeaders.PARTITION_KEY | String | 1
**PartitionKey** | com.azure.spring.integration.core.AzureHeaders.PARTITION_KEY | String | 2

For full configurations, check appendix

##### Basic Usage

##### Samples
**Example: Manually set the partition key for the message**

This example demonstrates how to manually set the partition key for the message in the application.

**Way 1:**
This example requires that `spring.cloud.stream.default.producer.partitionKeyExpression` be set `"'partitionKey-' + headers[<message-header-key>]"`.
```yaml
spring:
  cloud:
    azure:
      servicebus:
        connection-string: [servicebus-namespace-connection-string]
    stream:
      default:
        producer:
          partitionKeyExpression:  "'partitionKey-' + headers[<message-header-key>]"
```
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader("<message-header-key>", "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

> **NOTE:** When using `application.yml` to configure the partition key, its priority will be the lowest.
> It will take effect only when the `ServiceBusMessageHeaders.SESSION_ID`, `ServiceBusMessageHeaders.PARTITION_KEY`, `AzureHeaders.PARTITION_KEY` are not configured.
**Way 2:**
Manually add the partition Key in the message header by code.

*Recommended:* Use `ServiceBusMessageHeaders.PARTITION_KEY` as the key of the header.
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(ServiceBusMessageHeaders.PARTITION_KEY, "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

*Not recommended but currently supported:* `AzureHeaders.PARTITION_KEY` as the key of the header.
```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(AzureHeaders.PARTITION_KEY, "Customize partirion key")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```
> **NOTE:** When both `ServiceBusMessageHeaders.PARTITION_KEY` and `AzureHeaders.PARTITION_KEY` are set in the message headers,
> `ServiceBusMessageHeaders.PARTITION_KEY` is preferred.
**Example: Set the session id for the message**

This example demonstrates how to manually set the session id of a message in the application.

```java
@PostMapping("/messages")
public ResponseEntity<String> sendMessage(@RequestParam String message) {
    LOGGER.info("Going to add message {} to Sinks.Many.", message);
    many.emitNext(MessageBuilder.withPayload(message)
                                .setHeader(ServiceBusMessageHeaders.SESSION_ID, "Customize session id")
                                .build(), Sinks.EmitFailureHandler.FAIL_FAST);
    return ResponseEntity.ok("Sent!");
}
```

> **NOTE:** When the `ServiceBusMessageHeaders.SESSION_ID` is set in the message headers, and a different `ServiceBusMessageHeaders.PARTITION_KEY` (or `AzureHeaders.PARTITION_KEY`) header is also set,
> the value of the session id will eventually be used to overwrite the value of the partition key.
Please use this `sample` as a reference to learn more about how to use this binder in your project.
- [Service Bus Queue][spring_cloud_stream_binder_service_bus_queue]


### Kafka Support (@moary)

Connect to Azure Event Hubs using Kafka libraries

### Redis Support (@moary)

### Resource Manager (@moary)



## Known Issues

1. scary Identity error log 

## Appendix 



### A. Configuration Properties

| Name                                                             | Required? | Default Value | Description                                                                                                              |
| ---------------------------------------------------------------- | --------- | ------------- | ------------------------------------------------------------------------------------------------------------------------ |
| spring.cloud.azure.client.application-id                         |           |               | Represents current application and is used for telemetry/monitoring purposes.                                            |
| spring.cloud.azure.client.amqp.transport-type                    |           |               | Transport type for AMQP-based client.                                                                                                  |
| spring.cloud.azure.client.headers                                |           |               | Comma-delimited list of headers applied to each request sent with client.                                            |
| spring.cloud.azure.client.http.connect-timeout                   |           |               | Amount of time the request attempts to connect to the remote host and the connection is resolved.                                            |
| spring.cloud.azure.client.http.connection-idle-timeout           |           |               | Amount of time before an idle connection.                                                                                              |
| spring.cloud.azure.client.http.logging.allowed-header-names      |           |               | Comma-delimited list of whitelisted headers that should be logged.                                            |
| spring.cloud.azure.client.http.logging.allowed-query-param-names |           |               | Comma-delimited list of whitelisted query parameters.                                                                                  |
| spring.cloud.azure.client.http.logging.level                     |           |               | The level of detail to log on HTTP messages.                                                                                           |
| spring.cloud.azure.client.http.logging.prettyPrintBody           |           |               | Whether to pretty print the message bodies.                                                                                            |
| spring.cloud.azure.client.http.maximum-connection-pool-size      |           |               | Maximum connection pool size used by the underlying HTTP client.                                                                       |
| spring.cloud.azure.client.http.read-timeout                      |           |               | Amount of time used when reading the server response.                                                                                  |
| spring.cloud.azure.client.http.write-timeout                     |           |               | Amount of time each request being sent over the w                                                                                      |
| spring.cloud.azure.credential.client-certificate-password        |           |               | Password of the certificate file.                                                                                                      |
| spring.cloud.azure.credential.client-certificate-path            |           |               | Path of a PEM certificate file to use when performing service principal authentication with Azure.                                     |
| spring.cloud.azure.credential.client-id                          |           |               | Client id to use when performing service principal authentication with Azure.                                                          |
| spring.cloud.azure.credential.client-secret                      |           |               | Client secret to use when performing service principal authentication with Azure.                                                      |
| spring.cloud.azure.credential.managed-identity-client-id         |           |               | Client id to use when using managed identity to authenticate with Azure.                                                               |
| spring.cloud.azure.credential.username                           |           |               | Username to use when performing username/password authentication with Azure.                                                           |
| spring.cloud.azure.credential.password                           |           |               | Password to use when performing username/password authentication.                                                                      |
| spring.cloud.azure.profile.cloud                                 |           |               | Name of the Azure cloud to connect to.                                                                                                 |
| spring.cloud.azure.profile.environment.active-directory-endpoint |           |               |                                                                                                                                        |
| spring.cloud.azure.profile.subscription                          |           |               | Subscription id to use when connecting to Azure resources.                                                                             |
| spring.cloud.azure.profile.tenant-id                             |           |               | Tenant id for Azure resources.                                                                                                         |
| spring.cloud.azure.proxy.authentication-type                     |           |               | Authentication type used against the proxy.                                                                                            |
| spring.cloud.azure.proxy.hostname                                |           |               | The host of the proxy.                                                                                                                 |
| spring.cloud.azure.proxy.password                                |           |               | Password used to authenticate with the proxy.                                                                                          |
| spring.cloud.azure.proxy.port                                    |           |               | The port of the proxy.                                                                                                                 |
| spring.cloud.azure.proxy.type                                    |           |               | Type of the proxy.                                                                                                                     |
| spring.cloud.azure.proxy.username                                |           |               | Username used to authenticate with the proxy.                                                                                          |
| spring.cloud.azure.proxy.http.non-proxy-hosts                    |           |               | A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.                                                               |
| spring.cloud.azure.retry.backoff.delay                           |           |               | Amount of time to wait between retry attempts.                                                                                         |
| spring.cloud.azure.retry.backoff.max-delay                       |           |               | Maximum permissible amount of time between retry attempts.                                                                             |
| spring.cloud.azure.retry.backoff.multiplier                      |           |               | Multiplier used to calculate the next backoff delay. If positive, then used as a multiplier for generating the next delay for backoff. |
| spring.cloud.azure.retry.http.retry-after-header                 |           |               | HTTP header, such as Retry-After or x-ms-retry-after-ms, to lookup for the retry delay.                                                |
| spring.cloud.azure.retry.http.retry-after-time-unit              |           |               | Time unit to use when applying the retry delay.                                                                                        |
| spring.cloud.azure.retry.max-attempts                            |           |               | The maximum number of attempts.                                                                                                        |
| spring.cloud.azure.retry.timeout                                 |           |               | Amount of time to wait until a timeout.                                                                                                |

### B. Migration Guide from Spring Cloud Azure 3.x to 4.x

















<!-- Link -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/spring-cloud-azure-stream-binder-eventhubs/src
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-cloud-stream-binder-eventhubs
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-stream-binder-eventhubs
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-azure-event-hub
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/eventhubs/azure-spring-cloud-stream-binder-eventhubs
[spring logging document]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[eventhubs_multibinders_sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/eventhubs/azure-spring-cloud-stream-binder-eventhubs/eventhubs-multibinders
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[azure_event_hub]: https://azure.microsoft.com/services/event-hubs/
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[spring_cloud_stream_current_producer_properties]: https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_producer_properties
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies
[kafka_sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/eventhubs/azure-spring-cloud-starter-eventhubs-kafka/eventhubs-kafka
[azure_service_bus]: https://azure.microsoft.com/services/service-bus/
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-cloud-stream-binder-java-app-with-service-bus
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-cloud-stream-binder-servicebus-queue
[refdocs]: https://azure.github.io/azure-sdk-for-java/springcloud.html#azure-spring-cloud-stream-binder-servicebus-queue
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-queue
[spring logging document]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[spring_cloud_stream_binder_service_bus_multiple_binders]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-queue/servicebus-queue-multibinders
[spring_cloud_stream_binder_service_bus_queue]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-queue
[spring_cloud_stream_binder_service_bus_topic]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/main/servicebus/azure-spring-cloud-stream-binder-servicebus-topic/servicebus-topic-binder
[spring_integration]: https://spring.io/projects/spring-integration
[src_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/spring-cloud-azure-stream-binder-servicebus
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-cloud-dependencies]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-cloud-dependencies
[spring_cloud_stream_current_producer_properties]: https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream.html#_producer_properties
[Azure Portal]: https://ms.portal.azure.com/#home
[The OAuth 2.0 authorization code grant]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow
[azure-spring-boot-sample-active-directory-webapp]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-webapp
[azure-spring-boot-sample-active-directory-resource-server]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server/README.md
[azure-spring-boot-sample-active-directory-resource-server-obo]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-obo
[azure-spring-boot-sample-active-directory-resource-server-by-filter]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-by-filter
[AAD App Roles feature]: https://docs.microsoft.com/azure/architecture/multitenant-identity/app-roles#roles-using-azure-ad-app-roles
[client credentials grant flow]: https://docs.microsoft.com/azure/active-directory/develop/v1-oauth2-client-creds-grant-flow
[configured in your manifest]: https://docs.microsoft.com/azure/active-directory/develop/howto-add-app-roles-in-azure-ad-apps#examples
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-active-directory
[graph-api-list-member-of]: https://docs.microsoft.com/graph/api/user-list-memberof?view=graph-rest-1.0
[graph-api-list-transitive-member-of]: https://docs.microsoft.com/graph/api/user-list-transitivememberof?view=graph-rest-1.0
[instructions here]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md
[spring logging document]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[OAuth 2.0 implicit grant flow]: https://docs.microsoft.com/azure/active-directory/develop/v1-oauth2-implicit-grant-flow
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-active-directory
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples
[set up in the manifest of your application registration]: https://docs.microsoft.com/azure/active-directory/develop/howto-add-app-roles-in-azure-ad-apps
[Azure China]: https://docs.microsoft.com/azure/china/resources-developer-guide#check-endpoints-in-azure
[Incremental consent]: https://docs.microsoft.com/azure/active-directory/azuread-dev/azure-ad-endpoint-comparison#incremental-and-dynamic-consent
[register_an_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/quickstart-register-app
[prerequisite]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-active-directory#prerequisites
[Accessing a web application]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-active-directory#accessing-a-web-application
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
[Conditional Access]: https://docs.microsoft.com/azure/active-directory/conditional-access
[Grant Access]: https://docs.microsoft.com/azure/active-directory/conditional-access/concept-conditional-access-grant
[Block Access]: https://docs.microsoft.com/azure/active-directory/conditional-access/howto-conditional-access-policy-block-access
[Resource server visiting other resource server]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-active-directory#resource-server-visiting-other-resource-servers
[multi-factor authentication]: https://docs.microsoft.com/azure/active-directory/authentication/concept-mfa-howitworks
[Require MFA for all users]: https://docs.microsoft.com/azure/active-directory/conditional-access/howto-conditional-access-policy-all-users-mfa
[configure webapiA]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server-obo#configure-your-middle-tier-web-api-a
[configure webapiB]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-resource-server/README.md#configure-web-api
[configure webapp]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-webapp/README.md#configure-access-other-resources-server
[ms-identity-java-spring-tutorial]:https://github.com/Azure-Samples/ms-identity-java-spring-tutorial
[authorization_code]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow
[on_behalf_of]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow
[client_credentials]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-active-directory-b2c-oidc
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-active-directory-b2c
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples
[spring logging document]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
[tutorial_create_tenant]: https://docs.microsoft.com/azure/active-directory-b2c/tutorial-create-tenant
[The OAuth 2.0 authorization code grant]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow
[The OAuth 2.0 client credentials grant]: https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow
[web_application_accessing_resource_servers]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-active-directory-b2c#web-application-accessing-resource-servers
[azure-spring-boot-sample-active-directory-b2c-oidc]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc
[azure-spring-boot-sample-active-directory-b2c-resource-server]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-resource-server
[ms-identity-java-spring-tutorial]:https://github.com/Azure-Samples/ms-identity-java-spring-tutorial


