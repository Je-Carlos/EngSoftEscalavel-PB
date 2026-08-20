package com.br.infnet.pb_barpesujo.estoque.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MovimentacaoEstoqueRequest(@NotEmpty List<@Valid ItemMovimentacaoRequest> itens) {
}
