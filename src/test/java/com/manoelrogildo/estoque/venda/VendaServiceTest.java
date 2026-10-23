package com.manoelrogildo.estoque.venda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.manoelrogildo.estoque.compartilhado.exception.RegraDeNegocioException;
import com.manoelrogildo.estoque.estoque.EstoqueService;
import com.manoelrogildo.estoque.estoque.TipoMovimentacao;
import com.manoelrogildo.estoque.produto.Produto;
import com.manoelrogildo.estoque.produto.ProdutoService;
import com.manoelrogildo.estoque.venda.dto.CancelamentoRequest;
import com.manoelrogildo.estoque.venda.dto.VendaRequest;
import com.manoelrogildo.estoque.venda.dto.VendaResponse;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository repository;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private EstoqueService estoqueService;

    @InjectMocks
    private VendaService service;

    private final Produto arroz = new Produto("ARZ-5KG", "Arroz 5kg", "10063021", new BigDecimal("27.90"), 5);
    private final Produto feijao = new Produto("FEJ-1KG", "Feijão 1kg", "07133399", new BigDecimal("8.49"), 5);

    @Test
    void registrarDeveCalcularTotalEDarBaixaEmCadaItem() {
        when(produtoService.buscarEntidade(1L)).thenReturn(arroz);
        when(produtoService.buscarEntidade(2L)).thenReturn(feijao);

        VendaResponse venda = service.registrar(new VendaRequest(List.of(
                new VendaRequest.Item(1L, 2),
                new VendaRequest.Item(2L, 3))));

        // 2 x 27,90 + 3 x 8,49 = 81,27
        assertThat(venda.valorTotal()).isEqualByComparingTo("81.27");
        assertThat(venda.status()).isEqualTo(StatusVenda.CONCLUIDA);
        assertThat(venda.itens()).hasSize(2);
        verify(repository).save(any(Venda.class));
        verify(estoqueService).movimentar(arroz, TipoMovimentacao.SAIDA, 2, "Venda #null");
        verify(estoqueService).movimentar(feijao, TipoMovimentacao.SAIDA, 3, "Venda #null");
    }

    @Test
    void registrarComProdutoRepetidoDeveSerRejeitado() {
        VendaRequest request = new VendaRequest(List.of(
                new VendaRequest.Item(1L, 1),
                new VendaRequest.Item(1L, 2)));

        assertThatThrownBy(() -> service.registrar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("mais de uma vez");
        verify(repository, never()).save(any());
    }

    @Test
    void cancelarDeveDevolverItensAoEstoque() {
        Venda venda = new Venda();
        venda.adicionarItem(arroz, 2);
        when(repository.findComItensById(10L)).thenReturn(Optional.of(venda));

        VendaResponse cancelada = service.cancelar(10L, new CancelamentoRequest("Cliente desistiu"));

        assertThat(cancelada.status()).isEqualTo(StatusVenda.CANCELADA);
        assertThat(cancelada.motivoCancelamento()).isEqualTo("Cliente desistiu");
        verify(estoqueService).movimentar(arroz, TipoMovimentacao.ENTRADA, 2, "Cancelamento da venda #null");
    }

    @Test
    void cancelarVendaJaCanceladaDeveFalhar() {
        Venda venda = new Venda();
        venda.adicionarItem(arroz, 1);
        venda.cancelar("Primeiro cancelamento");
        when(repository.findComItensById(10L)).thenReturn(Optional.of(venda));

        assertThatThrownBy(() -> service.cancelar(10L, new CancelamentoRequest("De novo")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já está cancelada");
        verify(estoqueService, never()).movimentar(any(), any(), anyInt(), any());
    }
}
