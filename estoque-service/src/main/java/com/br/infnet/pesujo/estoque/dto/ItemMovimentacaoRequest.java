package com.br.infnet.pesujo.estoque.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemMovimentacaoRequest(@NotNull @Positive Long produtoId, @NotNull @Positive Integer quantidade) {
}
