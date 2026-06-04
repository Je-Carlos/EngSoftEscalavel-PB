package com.br.infnet.pb_barpesujo.comanda.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdicionarItemComandaRequest(
        @NotNull Long produtoId,
        @NotNull @Positive Integer quantidade
) {
}
