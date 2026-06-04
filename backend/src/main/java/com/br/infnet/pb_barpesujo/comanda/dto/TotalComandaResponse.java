package com.br.infnet.pb_barpesujo.comanda.dto;

import java.math.BigDecimal;

public record TotalComandaResponse(Long comandaId, BigDecimal total) {
}
