package com.br.infnet.pb_barpesujo.historico;

import java.time.Instant;
import java.util.Map;

public record HistoricoResponse(
        TipoEntidade entidade,
        Long registroId,
        Integer revisao,
        TipoOperacao operacao,
        Instant alteradoEm,
        Map<String, Object> anterior,
        Map<String, Object> posterior,
        String responsavel) {
}
