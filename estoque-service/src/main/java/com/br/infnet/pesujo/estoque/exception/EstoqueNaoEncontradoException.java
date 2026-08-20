package com.br.infnet.pesujo.estoque.exception;

public class EstoqueNaoEncontradoException extends RuntimeException {

    public EstoqueNaoEncontradoException(Long produtoId) {
        super("Estoque não encontrado para o produto " + produtoId + ".");
    }
}
