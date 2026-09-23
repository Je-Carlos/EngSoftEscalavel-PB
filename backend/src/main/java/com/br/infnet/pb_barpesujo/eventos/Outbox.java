package com.br.infnet.pb_barpesujo.eventos;

import com.br.infnet.eventos.ComandaEvento;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Outbox {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public Outbox(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public void gravar(String tipo, Long comandaId, List<ComandaEvento.Item> itens) {
        ComandaEvento evento = new ComandaEvento(UUID.randomUUID(), tipo, Instant.now(), comandaId, itens);
        String chave = tipo.equals(ComandaEvento.ITEM_REMOVIDO) ? "comanda.item.removido" : "comanda.cancelada";
        try {
            jdbc.update("insert into eventos_outbox (event_id, aggregate_id, routing_key, occurred_at, payload) values (?, ?, ?, ?, ?)",
                    evento.eventId(), comandaId, chave, Timestamp.from(evento.occurredAt()), mapper.writeValueAsString(evento));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar evento " + evento.eventId(), e);
        }
    }
}
