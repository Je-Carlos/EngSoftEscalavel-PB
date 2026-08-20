package com.br.infnet.pesujo.estoque.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AtualizarEstoqueRequest(@NotNull @PositiveOrZero Integer quantidade) {
}
