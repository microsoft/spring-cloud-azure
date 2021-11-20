# Spring Security Support

## Spring Security with Azure AD

### Dependency Setup
```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>spring-cloud-azure-starter-active-directory</artifactId>
</dependency>
```

### Configuration

This starter provides the following properties:

| Properties                                                                             | Description                                                                                    |
| -------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| **spring.cloud.azure.active-directory**.app-id-uri                                                   | It used in resource server, used to validate the audience in access_token. access_token is valid only when the audience in access_token equal to client-id or app-id-uri    |
| **spring.cloud.azure.active-directory**.authorization-clients                                        | A map configure the resource APIs the application is going to visit. Each item corresponding to one resource API the application is going to visit. In Spring code, each item corresponding to one OAuth2AuthorizedClient object|
| **spring.cloud.azure.active-directory**.authorization-clients.{client-name}.scopes                   | API permissions of a resource server that the application is going to acquire.                 |
| **spring.cloud.azure.active-directory**.authorization-clients.{client-name}.on-demand                | This is used for incremental consent. The default value is false. If it's true, it's not consent when user login, when application needs the additional permission, incremental consent is performed with one OAuth2 authorization code flow.|
| **spring.cloud.azure.active-directory**.authorization-clients.{client-name}.authorization-grant-type | Type of authorization client. Supported types are [authorization_code](https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow) (default type for webapp), [on_behalf_of](https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow) (default type for resource-server), [client_credentials](https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-client-creds-grant-flow). |
| **spring.cloud.azure.active-directory**.application-type                                             | Refer to [Application type](#property-example-1--application-type).|
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

#### Property example 1: Application type

This property(`spring.cloud.azure.active-directory.application-type`) is optional, its value can be inferred by dependencies, only `web_application_and_resource_server` must be configured manually: `spring.cloud.azure.active-directory.application-type=web_application_and_resource_server`.

| Has dependency: spring-security-oauth2-client | Has dependency: spring-security-oauth2-resource-server |                  Valid values of application type                                                     | Default value               |
|-----------------------------------------------|--------------------------------------------------------|-------------------------------------------------------------------------------------------------------|-----------------------------|
|                      Yes                      |                          No                            |                       `web_application`                                                               |       `web_application`     |
|                      No                       |                          Yes                           |                       `resource_server`                                                               |       `resource_server`     |
|                      Yes                      |                          Yes                           | `web_application`,`resource_server`,`resource_server_with_obo`, `web_application_and_resource_server` | `resource_server_with_obo`  |


#### Property example 2: Use [Azure China](https://docs.microsoft.com/azure/china/resources-developer-guide#check-endpoints-in-azure) instead of Azure Global.

* Step 1: Add property in application.yml
```yaml
spring:
  cloud:
    azure:
      active-directory:
        base-uri: https://login.partner.microsoftonline.cn
        graph-base-uri: https://microsoftgraph.chinacloudapi.cn
```

#### Property example 3: Use `group name` or `group id` to protect some method in web application.

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

#### Property example 4: [Incremental consent](https://docs.microsoft.com/azure/active-directory/azuread-dev/azure-ad-endpoint-comparison#incremental-and-dynamic-consent) in Web application visiting resource servers.

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
public class SampleController {
    @GetMapping("/arm")
    @ResponseBody
    public String arm(
        @RegisteredOAuth2AuthorizedClient("arm") OAuth2AuthorizedClient armClient
    ) {
        // toJsonString() is just a demo.
        // oAuth2AuthorizedClient contains access_token. We can use this access_token to access resource server.
        return toJsonString(armClient);
    }
}
```

After these steps. `arm`'s scopes (https://management.core.windows.net/user_impersonation) doesn't
need to be consented at login time. When user request `/arm` endpoint, user need to consent the
scope. That's `incremental consent` means.

After the scopes have been consented, AAD server will remember that this user has already granted
the permission to the web application. So incremental consent will not happen anymore after user
consented.

#### Property example 5: [Client credential flow] in resource server visiting resource servers.

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
public class SampleController {
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
}
```

### Basic Usage

#### Accessing a web application

This scenario uses the [The OAuth 2.0 authorization code grant](https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow) flow to login in a user with a Microsoft account.

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

* Step 3: Add properties in application.yml. These values should be got in [prerequisite](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-active-directory#prerequisites).
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

#### Web application accessing resource servers

**System diagram**:

![web-application-visiting-resource-servers.png](https://user-images.githubusercontent.com/13167207/142617853-0526205f-fdef-47f9-ac01-77963f8c34be.png)

* Step 1: Make sure `redirect URI` has been set, just like [Accessing a web application](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-active-directory#accessing-a-web-application).

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
public class SampleController {
    @GetMapping("/graph")
    @ResponseBody
    public String graph(
        @RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graphClient
    ) {
        // toJsonString() is just a demo.
        // oAuth2AuthorizedClient contains access_token. We can use this access_token to access resource server.
        return toJsonString(graphClient);
    }
}
```
Here, `graph` is the client name configured in step 2. OAuth2AuthorizedClient contains access_token.
access_token can be used to access resource server.

#### Accessing a resource server
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
Both `client-id` and `app-id-uri` can be used to verify access token. `app-id-uri` can be got in Azure Portal:

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

#### Resource server visiting other resource servers

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
public class SampleController {
    @PreAuthorize("hasAuthority('SCOPE_Obo.Graph.Read')")
    @GetMapping("call-graph")
    public String callGraph(@RegisteredOAuth2AuthorizedClient("graph") OAuth2AuthorizedClient graph) {
        return callMicrosoftGraphMeEndpoint(graph);
    }
}
```

#### Web application and Resource server in one application

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

### Samples
(todo: @chenrujun Add link here.)



## Spring Security with Azure AD B2C

### Dependency Setup
```xml
<dependencies>
	<dependency>
		<groupId>com.azure.spring</groupId>
		<artifactId>spring-cloud-azure-starter-active-directory-b2c</artifactId>
	</dependency>
</dependencies>
```

### Configuration

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

### Basic Usage

A `web application` is any web based application that allows user to login Azure AD, whereas a `resource server` will either
accept or deny access after validating access_token obtained from Azure AD. We will cover 4 scenarios in this guide:

1. Accessing a web application.
1. Web application accessing resource servers.
1. Accessing a resource server.
1. Resource server accessing other resource servers.

![B2C Web application & Web Api Overall](https://user-images.githubusercontent.com/13167207/142620440-f970b572-2646-4f50-9f77-db62d6e965f1.png)

#### Accessing a web application

This scenario uses the [The OAuth 2.0 authorization code grant](https://docs.microsoft.com/azure/active-directory/develop/v2-oauth2-auth-code-flow) flow to login in a user with your Azure AD B2C user.

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

#### Web application accessing resource servers

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
public class SampleController {
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
}
```

Security configuration code is the same with **Accessing a web application** scenario, another bean `webClient`is added as follows:
```java
public class SampleConfiguration {
		@Bean
		public WebClient webClient(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
				ServletOAuth2AuthorizedClientExchangeFilterFunction function =
						new ServletOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager);
				return WebClient.builder()
												.apply(function.oauth2Configuration())
												.build();
		}
}
```

1. Please refer to **Accessing a resource server** section to write your `WebApiA` Java code.

1. Build and test your app

	 Let `Webapp` and `WebApiA` run on port *8080* and *8081* respectively.
	 Start `Webapp` and `WebApiA` application, return to the home page after logging successfully, you can access `http://localhost:8080/webapp/webApiA` to get **WebApiA** resource response.

#### Accessing a resource server

This scenario not support login. Just protect the server by validating the access token, and if valid, serves the request.

1. Refer to [Web application accessing resource servers](#web-application-accessing-resource-servers) to build your `WebApiA` permission.

1. Add `WebApiA` permission and grant admin consent for your web application.

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
</dependencies>
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
public class SampleController {
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

#### Resource server accessing other resource servers

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
public class SampleController {
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
}
```

WebApiB controller code can refer to the following:
```java
public class SampleController {
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
}
```

Security configuration code is the same with **Accessing a resource server** scenario, another bean `webClient`is added as follows

6. Build and test your app

	 Let `WebApiA` and `WebApiB` run on port *8081* and *8082* respectively.
	 Start `WebApiA` and `WebApiB` application, get the access token for `webApiA` resource and access `http://localhost:8081/webApiA/webApiB/sample`
	 as the Bearer authorization header.


### Samples

#### Accessing a web application
Please refer to [azure-spring-boot-sample-active-directory-b2c-oidc](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-oidc). (todo: @chenrujun. update this link)

#### Accessing a resource server
Please refer to [azure-spring-boot-sample-active-directory-b2c-resource-server](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/aad/azure-spring-boot-sample-active-directory-b2c-resource-server).