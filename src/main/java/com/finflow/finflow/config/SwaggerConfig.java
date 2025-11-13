package com.finflow.finflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI finflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinFlow API")
                        .description("API for management users, wallets and transactions")
                        .version("v1.0.0")
                        .license(new License().name("MIT License")));
    }
}
