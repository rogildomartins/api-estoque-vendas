package com.manoelrogildo.estoque;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.jayway.jsonpath.JsonPath;

/**
 * Testa a API de ponta a ponta (HTTP → controller → service → banco H2 com Flyway).
 */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoEstoqueVendaIntegrationTest {

    /** JWT de administrador simulado; o fluxo real de login é coberto em SegurancaIntegrationTest. */
    private static final RequestPostProcessor ADMIN = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));

    @Autowired
    private MockMvc mvc;

    @Test
    void fluxoCompletoDeEntradaVendaECancelamento() throws Exception {
        long produtoId = cadastrarProduto("49.90");

        postJson("/api/produtos/" + produtoId + "/movimentacoes", """
                {"tipo": "ENTRADA", "quantidade": 10, "motivo": "NF de compra 1234"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoAnterior").value(0))
                .andExpect(jsonPath("$.saldoPosterior").value(10));

        String vendaJson = postJson("/api/vendas", """
                {"itens": [{"produtoId": %d, "quantidade": 3}]}
                """.formatted(produtoId))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.valorTotal").value(149.70))
                .andReturn().getResponse().getContentAsString();
        long vendaId = ((Number) JsonPath.read(vendaJson, "$.id")).longValue();

        mvc.perform(get("/api/produtos/" + produtoId).with(ADMIN))
                .andExpect(jsonPath("$.quantidadeEstoque").value(7));

        postJson("/api/vendas/" + vendaId + "/cancelamento", """
                {"motivo": "Cliente desistiu da compra"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mvc.perform(get("/api/produtos/" + produtoId).with(ADMIN))
                .andExpect(jsonPath("$.quantidadeEstoque").value(10));

        mvc.perform(get("/api/produtos/" + produtoId + "/movimentacoes").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].motivo").value("Cancelamento da venda #" + vendaId))
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    void vendaSemEstoqueSuficienteNaoDeveAlterarNenhumSaldo() throws Exception {
        long comEstoque = cadastrarProduto("10.00");
        long semEstoque = cadastrarProduto("20.00");
        postJson("/api/produtos/" + comEstoque + "/movimentacoes", """
                {"tipo": "ENTRADA", "quantidade": 5, "motivo": "Compra"}
                """);

        postJson("/api/vendas", """
                {"itens": [{"produtoId": %d, "quantidade": 2}, {"produtoId": %d, "quantidade": 1}]}
                """.formatted(comEstoque, semEstoque))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));

        // A transação inteira sofreu rollback: o primeiro item também não foi baixado.
        mvc.perform(get("/api/produtos/" + comEstoque).with(ADMIN))
                .andExpect(jsonPath("$.quantidadeEstoque").value(5));
    }

    @Test
    void cadastroComDadosInvalidosRetornaErrosPorCampo() throws Exception {
        postJson("/api/produtos", """
                {"sku": "", "nome": "Produto", "ncm": "123", "precoVenda": 0, "estoqueMinimo": -1}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.sku").exists())
                .andExpect(jsonPath("$.erros.ncm").value("deve conter exatamente 8 dígitos"))
                .andExpect(jsonPath("$.erros.precoVenda").exists())
                .andExpect(jsonPath("$.erros.estoqueMinimo").exists());
    }

    @Test
    void skuDuplicadoDeveSerRejeitado() throws Exception {
        String json = """
                {"sku": "dup-001", "nome": "Produto A", "ncm": "12345678", "precoVenda": 5, "estoqueMinimo": 0}
                """;
        postJson("/api/produtos", json).andExpect(status().isCreated());
        postJson("/api/produtos", json).andExpect(status().isUnprocessableContent());
    }

    @Test
    void produtoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/produtos/999999").with(ADMIN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Produto com id 999999 não encontrado(a)"));
    }

    @Test
    void dadosDeExemploAparecemNoRelatorioDeEstoqueBaixo() throws Exception {
        mvc.perform(get("/api/produtos/estoque-baixo").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].sku", hasItem("OLE-900ML")));
    }

    private long cadastrarProduto(String preco) throws Exception {
        String sku = "TST-" + UUID.randomUUID().toString().substring(0, 8);
        String json = postJson("/api/produtos", """
                {"sku": "%s", "nome": "Produto de teste", "ncm": "12345678", "precoVenda": %s, "estoqueMinimo": 2}
                """.formatted(sku, preco))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).with(ADMIN).contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
