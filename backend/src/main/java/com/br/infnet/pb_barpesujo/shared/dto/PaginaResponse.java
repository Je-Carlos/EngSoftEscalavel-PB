package com.br.infnet.pb_barpesujo.shared.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {

    public static <T, R> PaginaResponse<R> of(Page<T> page, java.util.function.Function<T, R> mapper) {
        return new PaginaResponse<>(page.map(mapper).getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
