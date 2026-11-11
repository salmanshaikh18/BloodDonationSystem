package com.group2.blooddonation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Starts the Blood Donation Management System.
 *
 * <p>Run it with {@code ./mvnw spring-boot:run} or {@code java -jar target/app.war}.
 * Extending {@link SpringBootServletInitializer} also lets the same WAR file run
 * inside an external Tomcat server.
 */
@SpringBootApplication
public class BloodDonationApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BloodDonationApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BloodDonationApplication.class);
    }
}
