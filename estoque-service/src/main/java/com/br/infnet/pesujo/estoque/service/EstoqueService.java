package com.br.infnet.pesujo.estoque.service;

import com.br.infnet.pesujo.estoque.domain.Estoque;
import com.br.infnet.pesujo.estoque.dto.EstoqueResponse;
import com.br.infnet.pesujo.estoque.dto.ItemMovimentacaoRequest;
import com.br.infnet.pesujo.estoque.exception.EstoqueInsuficienteException;
import com.br.infnet.pesujo.estoque.exception.EstoqueNaoEncontradoException;
import com.br.infnet.pesujo.estoque.repository.EstoqueRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    public EstoqueService(EstoqueRepository estoqueRepository) {
        this.estoqueRepository = estoqueRepository;
    }

    @Transactional(readOnly = true)
    public List<EstoqueResponse> listar() {
        return estoqueRepository.findAll().stream().map(EstoqueService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EstoqueResponse buscarPorProduto(Long produtoId) {
        return toResponse(buscarEntidade(produtoId));
    }

    @Transactional
    public EstoqueResponse definirSaldo(Long produtoId, Integer quantidade) {
        estoqueRepository.definirSaldo(produtoId, quantidade);
        return buscarPorProduto(produtoId);
    }

    @Transactional
    public void baixar(List<ItemMovimentacaoRequest> itens) {
        movimentar(itens, true);
    }

    @Transactional
    public void repor(List<ItemMovimentacaoRequest> itens) {
        movimentar(itens, false);
    }

    private void movimentar(List<ItemMovimentacaoRequest> itens, boolean baixa) {
        Map<Long, Integer> quantidades = consolidar(itens);
        Map<Long, Estoque> estoques = estoqueRepository.findAllByProdutoIdInForUpdate(quantidades.keySet()).stream()
                .collect(java.util.stream.Collectors.toMap(Estoque::getProdutoId, estoque -> estoque));
        if (estoques.size() != quantidades.size()) {
            throw new EstoqueInsuficienteException();
        }
        if (baixa && quantidades.entrySet().stream().anyMatch(item -> estoques.get(item.getKey()).getQuantidade() < item.getValue())) {
            throw new EstoqueInsuficienteException();
        }
        quantidades.forEach((produtoId, quantidade) -> {
            if (baixa) {
                estoques.get(produtoId).baixar(quantidade);
            } else {
                estoques.get(produtoId).repor(quantidade);
            }
        });
    }

    private Map<Long, Integer> consolidar(List<ItemMovimentacaoRequest> itens) {
        Map<Long, Integer> quantidades = new LinkedHashMap<>();
        for (ItemMovimentacaoRequest item : itens) {
            quantidades.merge(item.produtoId(), item.quantidade(), Math::addExact);
        }
        return quantidades;
    }

    private Estoque buscarEntidade(Long produtoId) {
        return estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new EstoqueNaoEncontradoException(produtoId));
    }

    private static EstoqueResponse toResponse(Estoque estoque) {
        return new EstoqueResponse(estoque.getProdutoId(), estoque.getQuantidade());
    }
}
