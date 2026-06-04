package com.br.infnet.pb_barpesujo.cardapio.dto;

import com.br.infnet.pb_barpesujo.cardapio.domain.CategoriaProduto;
import java.math.BigDecimal;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        CategoriaProduto categoria,
        BigDecimal preco,
        boolean disponivel
) {
}
