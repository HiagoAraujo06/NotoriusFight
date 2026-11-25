package com.notoriusfight.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI notoriusFightOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("NotoriusFight API")
                        .description("API da loja de artigos de luta: artigos, fornecedores, pessoas, clientes, "
                                + "colaboradores, vendas e movimentações de estoque.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NotoriusFight")))
                .externalDocs(new ExternalDocumentation()
                        .description("Projeto de referência da disciplina")
                        .url("https://github.com/jeffersonarpasserini/suporteos2026"));
    }
}
