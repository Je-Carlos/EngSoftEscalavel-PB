package com.br.infnet.pb_barpesujo.mesa.dto;

import com.br.infnet.pb_barpesujo.mesa.domain.StatusMesa;
import jakarta.validation.constraints.NotNull;

public record MesaStatusRequest(@NotNull StatusMesa status) {
}
