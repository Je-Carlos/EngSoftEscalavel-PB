package com.br.infnet.pb_barpesujo.comanda.service;

import com.br.infnet.pb_barpesujo.cardapio.domain.Produto;
import com.br.infnet.pb_barpesujo.cardapio.service.ProdutoService;
import com.br.infnet.pb_barpesujo.comanda.domain.Comanda;
import com.br.infnet.pb_barpesujo.comanda.domain.ItemComanda;
import com.br.infnet.pb_barpesujo.comanda.domain.StatusComanda;
import com.br.infnet.pb_barpesujo.comanda.dto.AbrirComandaRequest;
import com.br.infnet.pb_barpesujo.comanda.dto.AdicionarItemComandaRequest;
import com.br.infnet.pb_barpesujo.comanda.dto.ComandaResponse;
import com.br.infnet.pb_barpesujo.comanda.dto.ItemComandaResponse;
import com.br.infnet.pb_barpesujo.comanda.dto.TotalComandaResponse;
import com.br.infnet.pb_barpesujo.comanda.repository.ComandaRepository;
import com.br.infnet.pb_barpesujo.estoque.EstoqueClient;
import com.br.infnet.pb_barpesujo.estoque.dto.ItemMovimentacaoRequest;
import com.br.infnet.pb_barpesujo.estoque.dto.MovimentacaoEstoqueRequest;
import com.br.infnet.pb_barpesujo.mesa.domain.Mesa;
import com.br.infnet.pb_barpesujo.mesa.domain.StatusMesa;
import com.br.infnet.pb_barpesujo.mesa.service.MesaService;
import com.br.infnet.pb_barpesujo.shared.exception.BusinessException;
import com.br.infnet.pb_barpesujo.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.br.infnet.pb_barpesujo.shared.dto.PaginaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComandaService {

    private final ComandaRepository comandaRepository;
    private final MesaService mesaService;
    private final ProdutoService produtoService;
    private final EstoqueClient estoqueClient;

    public ComandaService(ComandaRepository comandaRepository, MesaService mesaService, ProdutoService produtoService, EstoqueClient estoqueClient) {
        this.comandaRepository = comandaRepository;
        this.mesaService = mesaService;
        this.produtoService = produtoService;
        this.estoqueClient = estoqueClient;
    }

    @Transactional
    public ComandaResponse abrir(AbrirComandaRequest request) {
        Mesa mesa = mesaService.buscarEntidade(request.mesaId());
        if (comandaRepository.existsByMesaIdAndStatus(mesa.getId(), StatusComanda.ABERTA)) {
            throw new BusinessException("Esta mesa já possui comanda aberta.");
        }
        mesa.setStatus(StatusMesa.OCUPADA);
        Comanda comanda = comandaRepository.save(new Comanda(mesa));
        return toResponse(comanda);
    }

    @Transactional(readOnly = true)
    public List<ComandaResponse> listar() {
        return comandaRepository.findAll().stream().map(ComandaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ComandaResponse> pesquisar(StatusComanda status, Pageable pageable) {
        return PaginaResponse.of(comandaRepository.pesquisar(status, pageable), ComandaService::toResponse);
    }

    @Transactional(readOnly = true)
    public ComandaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public ComandaResponse adicionarItem(Long id, AdicionarItemComandaRequest request) {
        Comanda comanda = buscarEntidade(id);
        validarAberta(comanda);
        Produto produto = produtoService.buscarEntidade(request.produtoId());
        if (!produto.isDisponivel()) {
            throw new BusinessException("Produto indisponível para adicionar à comanda.");
        }
        estoqueClient.baixar(movimentacao(List.of(new ItemMovimentacaoRequest(produto.getId(), request.quantidade()))));
        comanda.adicionarItem(produto, request.quantidade());
        return toResponse(comanda);
    }

    @Transactional
    public ComandaResponse removerItem(Long id, Long itemId) {
        Comanda comanda = buscarEntidade(id);
        validarAberta(comanda);
        ItemComanda item = comanda.getItens().stream().filter(atual -> atual.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item da comanda não encontrado."));
        estoqueClient.repor(movimentacao(List.of(new ItemMovimentacaoRequest(item.getProduto().getId(), item.getQuantidade()))));
        comanda.removerItem(itemId);
        return toResponse(comanda);
    }

    @Transactional(readOnly = true)
    public TotalComandaResponse calcularTotal(Long id) {
        Comanda comanda = buscarEntidade(id);
        return new TotalComandaResponse(comanda.getId(), comanda.calcularTotal());
    }

    @Transactional
    public ComandaResponse fechar(Long id) {
        Comanda comanda = buscarEntidade(id);
        validarAberta(comanda);
        if (comanda.getItens().isEmpty()) {
            throw new BusinessException("Não é possível fechar comanda vazia.");
        }
        comanda.fechar();
        comanda.getMesa().setStatus(StatusMesa.LIVRE);
        return toResponse(comanda);
    }

    @Transactional
    public ComandaResponse cancelar(Long id) {
        Comanda comanda = buscarEntidade(id);
        validarAberta(comanda);
        if (!comanda.getItens().isEmpty()) {
            estoqueClient.repor(movimentacao(comanda.getItens().stream()
                    .map(item -> new ItemMovimentacaoRequest(item.getProduto().getId(), item.getQuantidade())).toList()));
        }
        comanda.cancelar();
        comanda.getMesa().setStatus(StatusMesa.LIVRE);
        return toResponse(comanda);
    }

    private Comanda buscarEntidade(Long id) {
        return comandaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comanda não encontrada."));
    }

    private void validarAberta(Comanda comanda) {
        if (comanda.getStatus() != StatusComanda.ABERTA) {
            throw new BusinessException("A comanda precisa estar aberta para esta operação.");
        }
    }

    private static MovimentacaoEstoqueRequest movimentacao(List<ItemMovimentacaoRequest> itens) {
        return new MovimentacaoEstoqueRequest(itens);
    }

    private static ComandaResponse toResponse(Comanda comanda) {
        List<ItemComandaResponse> itens = comanda.getItens().stream().map(ComandaService::toItemResponse).toList();
        return new ComandaResponse(
                comanda.getId(),
                MesaService.toResponse(comanda.getMesa()),
                comanda.getStatus(),
                comanda.getAbertaEm(),
                comanda.getFechadaEm(),
                itens,
                comanda.calcularTotal());
    }

    private static ItemComandaResponse toItemResponse(ItemComanda item) {
        return new ItemComandaResponse(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal());
    }
}
