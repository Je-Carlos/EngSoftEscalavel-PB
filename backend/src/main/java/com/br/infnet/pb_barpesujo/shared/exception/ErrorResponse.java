package com.br.infnet.pb_barpesujo.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        List<String> detalhes
) {
    public static ErrorResponse of(int status, String erro, List<String> detalhes) {
        return new ErrorResponse(LocalDateTime.now(), status, erro, detalhes);
    }
}
