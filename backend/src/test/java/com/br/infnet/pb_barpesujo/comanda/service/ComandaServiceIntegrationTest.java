package com.br.infnet.pb_barpesujo.comanda.service;

import com.br.infnet.pb_barpesujo.cardapio.domain.CategoriaProduto;
import com.br.infnet.pb_barpesujo.cardapio.dto.ProdutoRequest;
import com.br.infnet.pb_barpesujo.cardapio.dto.ProdutoResponse;
import com.br.infnet.pb_barpesujo.cardapio.service.ProdutoService;
import com.br.infnet.pb_barpesujo.comanda.dto.AbrirComandaRequest;
import com.br.infnet.pb_barpesujo.comanda.dto.AdicionarItemComandaRequest;
import com.br.infnet.pb_barpesujo.comanda.dto.ComandaResponse;
import com.br.infnet.pb_barpesujo.estoque.EstoqueClient;
import com.br.infnet.pb_barpesujo.estoque.dto.ItemMovimentacaoRequest;
import com.br.infnet.pb_barpesujo.estoque.dto.MovimentacaoEstoqueRequest;
import com.br.infnet.pb_barpesujo.mesa.domain.StatusMesa;
import com.br.infnet.pb_barpesujo.mesa.dto.MesaRequest;
import com.br.infnet.pb_barpesujo.mesa.dto.MesaResponse;
import com.br.infnet.pb_barpesujo.mesa.service.MesaService;
import com.br.infnet.pb_barpesujo.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ComandaServiceIntegrationTest {

    private static final AtomicInteger NUMERO_MESA = new AtomicInteger(100);

    @Autowired
    private MesaService mesaService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ComandaService comandaService;

    @MockitoBean
    private EstoqueClient estoqueClient;

    @Test
    void abrirComandaOcupaMesaEImpedeOutraComandaAbertaNaMesmaMesa() {
        MesaResponse mesa = criarMesaLivre();

        ComandaResponse comanda = comandaService.abrir(new AbrirComandaRequest(mesa.id()));

        assertThat(comanda.mesa().status()).isEqualTo(StatusMesa.OCUPADA);
        assertThat(mesaService.buscarPorId(mesa.id()).status()).isEqualTo(StatusMesa.OCUPADA);
        assertThatThrownBy(() -> comandaService.abrir(new AbrirComandaRequest(mesa.id())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mesa")
                .hasMessageContaining("comanda aberta");
    }

    @Test
    void adicionarItemCalculaTotalComBaseNaQuantidadeEPrecoUnitario() {
        MesaResponse mesa = criarMesaLivre();
        ProdutoResponse pastel = criarProdutoDisponivel("Pastel de carne teste", "9.50");
        ComandaResponse comanda = comandaService.abrir(new AbrirComandaRequest(mesa.id()));

        ComandaResponse atualizada = comandaService.adicionarItem(
                comanda.id(), new AdicionarItemComandaRequest(pastel.id(), 3));

        assertThat(atualizada.itens()).hasSize(1);
        assertThat(atualizada.total()).isEqualByComparingTo("28.50");
        assertThat(comandaService.calcularTotal(comanda.id()).total()).isEqualByComparingTo("28.50");
        verify(estoqueClient).baixar(new MovimentacaoEstoqueRequest(List.of(new ItemMovimentacaoRequest(pastel.id(), 3))));
    }

    @Test
    void naoAdicionaProdutoIndisponivelNaComanda() {
        MesaResponse mesa = criarMesaLivre();
        ProdutoResponse produto = produtoService.criar(new ProdutoRequest(
                "Cerveja sem estoque teste", "Produto indisponível", CategoriaProduto.BEBIDA, new BigDecimal("7.00"), false));
        ComandaResponse comanda = comandaService.abrir(new AbrirComandaRequest(mesa.id()));

        assertThatThrownBy(() -> comandaService.adicionarItem(
                comanda.id(), new AdicionarItemComandaRequest(produto.id(), 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    void naoFechaComandaVazia() {
        MesaResponse mesa = criarMesaLivre();
        ComandaResponse comanda = comandaService.abrir(new AbrirComandaRequest(mesa.id()));

        assertThatThrownBy(() -> comandaService.fechar(comanda.id()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vazia");
    }

    @Test
    void fecharComandaComItensLiberaMesaEImpedeNovosItens() {
        MesaResponse mesa = criarMesaLivre();
        ProdutoResponse torresmo = criarProdutoDisponivel("Torresmo teste", "18.00");
        ComandaResponse comanda = comandaService.abrir(new AbrirComandaRequest(mesa.id()));
        comandaService.adicionarItem(comanda.id(), new AdicionarItemComandaRequest(torresmo.id(), 1));

        ComandaResponse fechada = comandaService.fechar(comanda.id());

        assertThat(fechada.status().name()).isEqualTo("FECHADA");
        assertThat(fechada.mesa().status()).isEqualTo(StatusMesa.LIVRE);
        assertThat(mesaService.buscarPorId(mesa.id()).status()).isEqualTo(StatusMesa.LIVRE);
        assertThatThrownBy(() -> comandaService.adicionarItem(
                comanda.id(), new AdicionarItemComandaRequest(torresmo.id(), 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aberta");
    }

    @Test
    void removerOuCancelarComandaEstornaOsItensNoEstoque() {
        MesaResponse mesa = criarMesaLivre();
        ProdutoResponse produto = criarProdutoDisponivel("Bolinho teste", "12.00");
        ComandaResponse comanda = comandaService.abrir(new AbrirComandaRequest(mesa.id()));
        ComandaResponse comItem = comandaService.adicionarItem(
                comanda.id(), new AdicionarItemComandaRequest(produto.id(), 2));

        comandaService.removerItem(comanda.id(), comItem.itens().getFirst().id());
        comandaService.adicionarItem(comanda.id(), new AdicionarItemComandaRequest(produto.id(), 1));
        comandaService.cancelar(comanda.id());

        verify(estoqueClient).repor(new MovimentacaoEstoqueRequest(List.of(new ItemMovimentacaoRequest(produto.id(), 2))));
        verify(estoqueClient).repor(new MovimentacaoEstoqueRequest(List.of(new ItemMovimentacaoRequest(produto.id(), 1))));
    }

    private MesaResponse criarMesaLivre() {
        return mesaService.criar(new MesaRequest(NUMERO_MESA.incrementAndGet(), StatusMesa.LIVRE));
    }

    private ProdutoResponse criarProdutoDisponivel(String nome, String preco) {
        return produtoService.criar(new ProdutoRequest(
                nome, "Produto para teste", CategoriaProduto.SALGADO, new BigDecimal(preco), true));
    }
}
