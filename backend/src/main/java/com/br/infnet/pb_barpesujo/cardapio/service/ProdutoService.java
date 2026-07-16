package com.br.infnet.pb_barpesujo.cardapio.service;

import com.br.infnet.pb_barpesujo.cardapio.domain.Produto;
import com.br.infnet.pb_barpesujo.cardapio.domain.CategoriaProduto;
import com.br.infnet.pb_barpesujo.cardapio.dto.ProdutoRequest;
import com.br.infnet.pb_barpesujo.cardapio.dto.ProdutoResponse;
import com.br.infnet.pb_barpesujo.cardapio.repository.ProdutoRepository;
import com.br.infnet.pb_barpesujo.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.data.domain.Pageable;
import com.br.infnet.pb_barpesujo.shared.dto.PaginaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        boolean disponivel = request.disponivel() == null || request.disponivel();
        Produto produto = new Produto(request.nome(), request.descricao(), request.categoria(), request.preco(), disponivel);
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return produtoRepository.findAll().stream().map(ProdutoService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ProdutoResponse> pesquisar(String nome, CategoriaProduto categoria, Boolean disponivel, Pageable pageable) {
        return PaginaResponse.of(produtoRepository.pesquisar(nome, categoria, disponivel, pageable), ProdutoService::toResponse);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidade(id);
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCategoria(request.categoria());
        produto.setPreco(request.preco());
        produto.setDisponivel(request.disponivel() == null || request.disponivel());
        return toResponse(produto);
    }

    @Transactional
    public ProdutoResponse alterarDisponibilidade(Long id, boolean disponivel) {
        Produto produto = buscarEntidade(id);
        produto.setDisponivel(disponivel);
        return toResponse(produto);
    }

    @Transactional
    public void remover(Long id) {
        Produto produto = buscarEntidade(id);
        produtoRepository.delete(produto);
    }

    public Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
    }

    public static ProdutoResponse toResponse(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getPreco(),
                produto.isDisponivel());
    }
}
