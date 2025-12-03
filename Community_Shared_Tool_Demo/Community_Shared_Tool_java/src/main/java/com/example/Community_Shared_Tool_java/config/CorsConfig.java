package com.example.Community_Shared_Tool_java.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 使用allowedOriginPatterns，这样更灵活，支持所有本地端口
                .allowedOriginPatterns(
                        "http://localhost:5173",  // Vue默认开发端口
                        "http://localhost:*",      // 所有本地端口
                        "http://127.0.0.1:*"       // 本地IP地址
                )
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }
}