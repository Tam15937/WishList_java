package org.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wishlist API")
                        .version("1.0")
                        .description("API микросервиса для управления списками и элементами")
                        .contact(new Contact()
                                .name("Автор проекта")
                                .email("email@example.com")
                        )
                );
    }
}
