package com.manoelrogildo.estoque.seguranca;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

/**
 * Testa autenticação e autorização com tokens reais emitidos por /api/auth/login.
 * O administrador inicial (admin/admin123) é criado pelo AdminInicial ao subir a aplicação.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SegurancaIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void requisicaoSemTokenRetorna401() throws Exception {
        mvc.perform(get("/api/produtos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Não autenticado"));
    }

    @Test
    void tokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/api/produtos").header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        login("admin", "senha-errada")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Login ou senha inválidos"));
    }

    @Test
    void tokenDoLoginDaAcessoAApi() throws Exception {
        String token = obterToken("admin", "admin123");

        mvc.perform(get("/api/produtos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void operadorConsultaEVendeMasNaoAlteraCadastroNemCancela() throws Exception {
        String admin = obterToken("admin", "admin123");
        String login = novoUsuario(admin, "OPERADOR");
        String operador = obterToken(login, "senha-forte-123");

        mvc.perform(get("/api/produtos").header(HttpHeaders.AUTHORIZATION, "Bearer " + operador))
                .andExpect(status().isOk());
        // Passa pela segurança e chega na validação do corpo.
        postJson("/api/vendas", operador, "{\"itens\": []}")
                .andExpect(status().isBadRequest());

        postJson("/api/produtos", operador, """
                {"sku": "OP-001", "nome": "Produto", "ncm": "12345678", "precoVenda": 5, "estoqueMinimo": 0}
                """)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"));
        postJson("/api/produtos/1/movimentacoes", operador, """
                {"tipo": "ENTRADA", "quantidade": 1, "motivo": "Teste"}
                """)
                .andExpect(status().isForbidden());
        postJson("/api/vendas/1/cancelamento", operador, "{\"motivo\": \"Teste\"}")
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + operador))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioInativadoNaoConsegueFazerLogin() throws Exception {
        String admin = obterToken("admin", "admin123");
        String login = novoUsuario(admin, "OPERADOR");
        long id = idDoUsuario(admin, login);

        mvc.perform(delete("/api/usuarios/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isNoContent());

        login(login, "senha-forte-123")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Login ou senha inválidos"));
    }

    @Test
    void respostaDeUsuarioNaoExpoeASenha() throws Exception {
        String admin = obterToken("admin", "admin123");

        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senha").doesNotExist())
                .andExpect(jsonPath("$[0].senhaHash").doesNotExist());
    }

    private String novoUsuario(String tokenAdmin, String papel) throws Exception {
        String login = "user-" + UUID.randomUUID().toString().substring(0, 8);
        postJson("/api/usuarios", tokenAdmin, """
                {"login": "%s", "nome": "Usuário de teste", "senha": "senha-forte-123", "papel": "%s"}
                """.formatted(login, papel))
                .andExpect(status().isCreated());
        return login;
    }

    private long idDoUsuario(String tokenAdmin, String login) throws Exception {
        String json = mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.<List<Object>>read(json, "$[?(@.login == '" + login + "')].id")
                .get(0)).longValue();
    }

    private String obterToken(String login, String senha) throws Exception {
        String json = login(login, senha)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.accessToken");
    }

    private ResultActions login(String login, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"%s\", \"senha\": \"%s\"}".formatted(login, senha)));
    }

    private ResultActions postJson(String url, String token, String body) throws Exception {
        return mvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
