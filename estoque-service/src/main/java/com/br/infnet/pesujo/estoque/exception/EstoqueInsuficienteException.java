package com.br.infnet.pesujo.estoque.exception;

public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException() {
        super("Estoque insuficiente para concluir a operação.");
    }
}
