package com.br.infnet.pb_barpesujo.comanda.dto;

import java.math.BigDecimal;

public record ItemComandaResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
}
