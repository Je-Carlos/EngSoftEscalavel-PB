package com.br.infnet.pb_barpesujo.mesa.dto;

import com.br.infnet.pb_barpesujo.mesa.domain.StatusMesa;

public record MesaResponse(
        Long id,
        Integer numero,
        StatusMesa status
) {
}
