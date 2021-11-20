# Secret Management

spring-cloud-azure-starter-keyvault-secrets adds Azure Key Vault as one of the Spring PropertySource, so secrets stored in Azure Key Vault could be easily used and conveniently accessed like other externalized configuration property, e.g. properties in files.

## Dependency Setup
```xml
<dependency>
	<groupId>com.azure.spring</groupId>
	<artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
</dependency>
```

## Configuration
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

## Basic Usage

## Samples
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