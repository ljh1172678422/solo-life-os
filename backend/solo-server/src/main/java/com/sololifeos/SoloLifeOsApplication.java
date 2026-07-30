package com.sololifeos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point of the Solo Life OS backend server.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SoloLifeOsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoloLifeOsApplication.class, args);
    }

}
