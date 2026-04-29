package com.edusphere.progress.config;
 
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;
 
@Configuration
public class AppConfig {
 
    @Bean
    public WebMvcConfigurer corsConfigurer() {
 
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
 
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*");
            }
        };
    }
}