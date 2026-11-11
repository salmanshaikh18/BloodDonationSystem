package com.group2.blooddonation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

/**
 * Web settings that properties files can't express.
 */
@Configuration
public class WebConfig {

    /**
     * Rewrites links written with {@code <c:url value="/css/app.css"/>} to
     * {@code /css/app-<content-hash>.css}. The name changes whenever the file changes,
     * so browsers can cache it for a long time and still get every update.
     */
    @Bean
    public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
        return new ResourceUrlEncodingFilter();
    }
}
