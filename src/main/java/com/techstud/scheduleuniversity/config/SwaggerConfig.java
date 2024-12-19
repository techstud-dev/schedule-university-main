package com.techstud.scheduleuniversity.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "schedule-university-main",
                version = "0.0.1-SNAPSHOT"
        ),
        servers = @Server(url = "http://localhost:8080")
)
public class SwaggerConfig {
}
