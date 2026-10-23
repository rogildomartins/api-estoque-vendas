package com.manoelrogildo.estoque.compartilhado.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {

    private static final String ESQUEMA_JWT = "bearer-jwt";

    @Bean
    OpenAPI openAPI() {
        // Adiciona o botão "Authorize" no Swagger para informar o token obtido em /api/auth/login.
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT))
                .info(new Info()
                .title("API de Controle de Estoque e Vendas")
                .version("1.0.0")
                .description("Cadastro de produtos, movimentação de estoque com histórico (kardex) "
                        + "e vendas com baixa automática de estoque.")
                .contact(new Contact().name("Manoel Rogildo Martins")
                        .url("https://www.linkedin.com/in/manoelrogildo")));
    }
}
