package com.br.infnet.pb_barpesujo.comanda.dto;

import jakarta.validation.constraints.NotNull;

public record AbrirComandaRequest(@NotNull Long mesaId) {
}
