package com.example.gradlecoding.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springBootAPI() {

        Info info = new Info()
            .title("새록새록 API Documentation")
            .description("새록새록 서버 API 문서")
            .version("0.1");

        return new OpenAPI()
            .addServersItem(new Server().url("/"))
            .info(info);
    }

}

