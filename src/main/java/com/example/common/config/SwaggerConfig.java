package com.example.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// swagger 접속 주소: http://localhost:8080/swagger-ui/index.html
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swagger() {
        Info info = new Info();
        info.title("spring boot lecture api")
                .version("1.0.0")
                .description("안녕~!");

        return new OpenAPI().info(info);
    }
}
