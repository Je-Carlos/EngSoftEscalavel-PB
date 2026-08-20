package com.br.infnet.pesujo.estoque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.br.infnet.pesujo.estoque.dto.ItemMovimentacaoRequest;
import com.br.infnet.pesujo.estoque.exception.EstoqueInsuficienteException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EstoqueServiceIntegrationTest {

    @Autowired
    private EstoqueService estoqueService;

    @Test
    void baixaLoteReduzSaldoDosProdutos() {
        estoqueService.definirSaldo(101L, 5);
        estoqueService.definirSaldo(102L, 3);

        estoqueService.baixar(List.of(
                new ItemMovimentacaoRequest(101L, 2),
                new ItemMovimentacaoRequest(102L, 1)));

        assertThat(estoqueService.buscarPorProduto(101L).quantidade()).isEqualTo(3);
        assertThat(estoqueService.buscarPorProduto(102L).quantidade()).isEqualTo(2);
    }

    @Test
    void baixaComSaldoInsuficienteNaoAlteraNenhumProdutoDoLote() {
        estoqueService.definirSaldo(201L, 2);
        estoqueService.definirSaldo(202L, 1);

        assertThatThrownBy(() -> estoqueService.baixar(List.of(
                new ItemMovimentacaoRequest(201L, 1),
                new ItemMovimentacaoRequest(202L, 2))))
                .isInstanceOf(EstoqueInsuficienteException.class);

        assertThat(estoqueService.buscarPorProduto(201L).quantidade()).isEqualTo(2);
        assertThat(estoqueService.buscarPorProduto(202L).quantidade()).isEqualTo(1);
    }

    @Test
    void reposicaoDevolveSaldoReservado() {
        estoqueService.definirSaldo(301L, 1);
        estoqueService.baixar(List.of(new ItemMovimentacaoRequest(301L, 1)));

        estoqueService.repor(List.of(new ItemMovimentacaoRequest(301L, 1)));

        assertThat(estoqueService.buscarPorProduto(301L).quantidade()).isEqualTo(1);
    }
}
