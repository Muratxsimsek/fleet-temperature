package com.fleet.temperature.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI fleetTemperatureOpenAPI() {
        Info info = new Info()
            .title("Fleet Temperature Dashboard API")
            .version("1.0.0")
            .description("Real-time aircraft cabin temperature monitoring system API");
        
        return new OpenAPI().info(info);
    }
}
