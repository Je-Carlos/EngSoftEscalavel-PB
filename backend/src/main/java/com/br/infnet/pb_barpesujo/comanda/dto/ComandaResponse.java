package com.br.infnet.pb_barpesujo.comanda.dto;

import com.br.infnet.pb_barpesujo.comanda.domain.StatusComanda;
import com.br.infnet.pb_barpesujo.mesa.dto.MesaResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComandaResponse(
        Long id,
        MesaResponse mesa,
        StatusComanda status,
        LocalDateTime abertaEm,
        LocalDateTime fechadaEm,
        List<ItemComandaResponse> itens,
        BigDecimal total
) {
}
