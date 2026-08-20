package com.br.infnet.pesujo.estoque.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "estoques")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produto_id", nullable = false, unique = true)
    private Long produtoId;

    @Column(nullable = false)
    private Integer quantidade;

    protected Estoque() {
    }

    public Estoque(Long produtoId, Integer quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void definirQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public void baixar(Integer quantidade) {
        this.quantidade -= quantidade;
    }

    public void repor(Integer quantidade) {
        this.quantidade += quantidade;
    }
}
