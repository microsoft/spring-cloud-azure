package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;

@SpringBootApplication
@ComponentScan({"com.microsoft.azure.spring.cloud.feature.manager","com.example"})
public class ConsoleApplication implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(ConsoleApplication.class);

    @Autowired
    FeatureManager featureManager;

    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("EXECUTING : command line runner");

        if (featureManager.isEnabled("Beta")) {
            System.out.println("Running Beta");
        }
        else {
            System.out.println("Running Application");
        }
    }
    
    

}
