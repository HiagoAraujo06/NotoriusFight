package com.notoriusfight.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI notoriusFightOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NotoriusFight API")
                        .description("API didática para cadastro de artigos, categorias, fornecedores "
                                + "e movimentações de estoque.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NotoriusFight")))
                .externalDocs(new ExternalDocumentation()
                        .description("Material didático do projeto")
                        .url("https://github.com/jeffersonarpasserini/suporteos2026"));
    }
}
