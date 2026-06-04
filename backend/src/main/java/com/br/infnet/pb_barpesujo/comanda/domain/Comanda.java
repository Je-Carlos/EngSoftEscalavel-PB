package com.br.infnet.pb_barpesujo.comanda.domain;

import com.br.infnet.pb_barpesujo.cardapio.domain.Produto;
import com.br.infnet.pb_barpesujo.mesa.domain.Mesa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comandas")
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusComanda status = StatusComanda.ABERTA;

    @Column(nullable = false)
    private LocalDateTime abertaEm = LocalDateTime.now();

    private LocalDateTime fechadaEm;

    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemComanda> itens = new ArrayList<>();

    protected Comanda() {
    }

    public Comanda(Mesa mesa) {
        this.mesa = mesa;
    }

    public Long getId() {
        return id;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public StatusComanda getStatus() {
        return status;
    }

    public LocalDateTime getAbertaEm() {
        return abertaEm;
    }

    public LocalDateTime getFechadaEm() {
        return fechadaEm;
    }

    public List<ItemComanda> getItens() {
        return itens;
    }

    public void adicionarItem(Produto produto, Integer quantidade) {
        itens.add(new ItemComanda(this, produto, quantidade));
    }

    public void removerItem(Long itemId) {
        itens.removeIf(item -> item.getId().equals(itemId));
    }

    public BigDecimal calcularTotal() {
        return itens.stream()
                .map(ItemComanda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void fechar() {
        this.status = StatusComanda.FECHADA;
        this.fechadaEm = LocalDateTime.now();
    }

    public void cancelar() {
        this.status = StatusComanda.CANCELADA;
        this.fechadaEm = LocalDateTime.now();
    }
}
