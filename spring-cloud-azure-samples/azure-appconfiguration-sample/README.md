# Spring Cloud Azure Config Sample

This sample describes how to use [spring-cloud-starter-azure-appconfiguration-config](../../spring-cloud-azure-starters/spring-cloud-starter-azure-appconfiguration-config/) to load configuration properties from Azure Configuration Service to Spring Environment.

## Prerequisite
 - Java 8
 - Maven 3

## How to run

### Prepare data

1. Create a Configuration Store if not exist.

2. Import the data file src/main/resources/data/sample-data.json into the Configuration Store created above. Keep the default options as-is when importing json data file.

### Configure the bootstrap.yaml

Change the connection-string value with the Access Key value of the Configuration Store created above.

### Run the application

Start the application and access http://localhost:8080 to check the returned value. Different commands for different scenarios are listed below.

1. Load properties similar with from application.properties, i.e., keys starting with /application/
```
$ mvn spring-boot:run
```

2. Load properties similar with from application_dev.properties, i.e., keys starting with /application_dev
```
$ mvn -Dspring.profiles.active=dev spring-boot:run
```

3. Load properties similar with from foo.properties, i.e., keys starting with /foo/
```
$ mvn -Dspring.application.name=foo spring-boot:run
```

4. Load properties similar with from foo_dev.properties, i.e., keys starting with /foo_dev/
```
$ mvn -Dspring.application.name=foo -Dspring.profiles.active=dev spring-boot:run
```

## More details

Please refer to this [README](../../spring-cloud-azure-starters/spring-cloud-starter-azure-appconfiguration-config/) about more usage details. 