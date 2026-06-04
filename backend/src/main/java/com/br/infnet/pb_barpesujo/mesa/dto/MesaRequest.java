package com.br.infnet.pb_barpesujo.mesa.dto;

import com.br.infnet.pb_barpesujo.mesa.domain.StatusMesa;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MesaRequest(
        @NotNull @Positive Integer numero,
        StatusMesa status
) {
}
