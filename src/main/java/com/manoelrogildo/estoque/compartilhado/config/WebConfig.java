package com.manoelrogildo.estoque.compartilhado.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("API de Controle de Estoque e Vendas")
                .version("1.0.0")
                .description("Cadastro de produtos, movimentação de estoque com histórico (kardex) "
                        + "e vendas com baixa automática de estoque.")
                .contact(new Contact().name("Manoel Rogildo Martins")
                        .url("https://www.linkedin.com/in/manoelrogildo")));
    }
}
