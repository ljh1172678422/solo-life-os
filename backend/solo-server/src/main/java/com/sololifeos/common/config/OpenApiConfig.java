package com.sololifeos.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc / OpenAPI configuration. Exposes Swagger UI at
 * {@code /swagger-ui.html} and the OpenAPI spec at {@code /v3/api-docs}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI soloLifeOsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Solo Life OS API")
                        .version("1.0")
                        .description("Solo Life OS backend API documentation"));
    }

}
