package com.br.infnet.pb_barpesujo.historico;

import static org.assertj.core.api.Assertions.assertThat;

import com.br.infnet.pb_barpesujo.cardapio.domain.CategoriaProduto;
import com.br.infnet.pb_barpesujo.cardapio.dto.ProdutoRequest;
import com.br.infnet.pb_barpesujo.cardapio.dto.ProdutoResponse;
import com.br.infnet.pb_barpesujo.cardapio.service.ProdutoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("persistence-eval")
@SpringBootTest
class HistoricoServiceIntegrationTest {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private HistoricoService historicoService;

    @Test
    void registraCriacaoAlteracaoEExclusaoDeProduto() {
        ProdutoResponse produto = produtoService.criar(new ProdutoRequest(
                "Produto auditado", "Teste de histórico", CategoriaProduto.BEBIDA, new BigDecimal("12.00"), true));
        produtoService.alterarDisponibilidade(produto.id(), false);
        produtoService.remover(produto.id());

        List<HistoricoResponse> historico = historicoService.consultar(TipoEntidade.PRODUTO, produto.id());

        assertThat(historico).extracting(HistoricoResponse::operacao)
                .containsExactly(TipoOperacao.CRIACAO, TipoOperacao.ALTERACAO, TipoOperacao.EXCLUSAO);
        assertThat(historico.get(1).anterior()).containsEntry("disponivel", true);
        assertThat(historico.get(1).posterior()).containsEntry("disponivel", false);
        assertThat(historico.get(2).posterior()).isNull();
    }
}
