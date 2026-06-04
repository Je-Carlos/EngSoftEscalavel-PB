package com.br.infnet.pb_barpesujo.cardapio.dto;

import jakarta.validation.constraints.NotNull;

public record ProdutoDisponibilidadeRequest(@NotNull Boolean disponivel) {
}
