package com.br.infnet.pb_barpesujo.cardapio.dto;

import com.br.infnet.pb_barpesujo.cardapio.domain.CategoriaProduto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank String nome,
        @NotBlank String descricao,
        @NotNull CategoriaProduto categoria,
        @NotNull @DecimalMin("0.01") BigDecimal preco,
        Boolean disponivel
) {
}
