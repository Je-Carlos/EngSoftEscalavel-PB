package com.br.infnet.eventos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ComandaEvento(UUID eventId, String eventType, Instant occurredAt, Long aggregateId,
                            List<Item> itens) {
    public static final String ITEM_REMOVIDO = "ItemRemovido";
    public static final String COMANDA_CANCELADA = "ComandaCancelada";

    public record Item(Long itemId, Long produtoId, Integer quantidade) {}
}
