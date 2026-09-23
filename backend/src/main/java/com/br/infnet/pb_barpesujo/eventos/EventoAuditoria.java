package com.br.infnet.pb_barpesujo.eventos;

import com.br.infnet.eventos.ComandaEvento;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventoAuditoria {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public EventoAuditoria(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Transactional
    @RabbitListener(queues = "comandas.auditoria")
    public void receber(String payload) throws JsonProcessingException {
        ComandaEvento evento = mapper.readValue(payload, ComandaEvento.class);
        jdbc.update("insert into eventos_auditoria (event_id, aggregate_id, occurred_at, payload) "
                        + "values (?, ?, ?, ?) on conflict (event_id) do nothing",
                evento.eventId(), evento.aggregateId(), Timestamp.from(evento.occurredAt()), payload);
    }

    public List<ComandaEvento> listar(Long comandaId) {
        return jdbc.query("select payload from eventos_auditoria where aggregate_id = ? order by occurred_at",
                (rs, row) -> {
                    try {
                        return mapper.readValue(rs.getString(1), ComandaEvento.class);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Evento de auditoria inválido", e);
                    }
                }, comandaId);
    }
}
