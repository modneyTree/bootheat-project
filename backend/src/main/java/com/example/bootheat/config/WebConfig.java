// src/main/java/com/example/bootheat/config/WebConfig.java
package com.example.bootheat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ~/bootheat/uploads → /uploads/** 로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/home/ubuntu/bootheat/uploads/");
    }
}
