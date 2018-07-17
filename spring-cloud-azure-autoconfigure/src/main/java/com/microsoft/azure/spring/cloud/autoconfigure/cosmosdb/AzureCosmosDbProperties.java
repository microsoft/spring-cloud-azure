package com.microsoft.azure.spring.cloud.autoconfigure.cosmosdb;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("spring.cloud.azure.cosmosdb")
public class AzureCosmosDbProperties {
    String accountName;
    String kind;

}
