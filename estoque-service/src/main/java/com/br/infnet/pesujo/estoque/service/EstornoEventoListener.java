package com.br.infnet.pesujo.estoque.service;

import com.br.infnet.eventos.ComandaEvento;
import com.br.infnet.pesujo.estoque.dto.ItemMovimentacaoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstornoEventoListener {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final EstoqueService estoque;

    public EstornoEventoListener(JdbcTemplate jdbc, ObjectMapper mapper, EstoqueService estoque) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.estoque = estoque;
    }

    @Transactional
    public void receber(byte[] payload) throws IOException {
        ComandaEvento evento = mapper.readValue(payload, ComandaEvento.class);
        if (evento.eventId() == null || evento.aggregateId() == null || evento.itens() == null
                || !List.of(ComandaEvento.ITEM_REMOVIDO, ComandaEvento.COMANDA_CANCELADA).contains(evento.eventType())
                || evento.itens().stream().anyMatch(item -> item.itemId() == null || item.produtoId() == null
                || item.quantidade() == null || item.quantidade() <= 0)) {
            throw new IllegalArgumentException("Evento de estorno inválido");
        }
        if (jdbc.update("insert into eventos_processados (event_id) values (?) on conflict do nothing", evento.eventId()) == 0) {
            return;
        }
        if (!evento.itens().isEmpty()) {
            estoque.repor(evento.itens().stream()
                    .map(item -> new ItemMovimentacaoRequest(item.produtoId(), item.quantidade())).toList());
        }
    }

    @Component
    @ConditionalOnProperty(name = "app.eventos.consumidores.enabled", havingValue = "true", matchIfMissing = true)
    static class Consumidor {
        private final EstornoEventoListener listener;

        Consumidor(EstornoEventoListener listener) {
            this.listener = listener;
        }

        @RabbitListener(queues = "comandas.estoque")
        void receber(byte[] payload) throws IOException {
            listener.receber(payload);
        }
    }
}
