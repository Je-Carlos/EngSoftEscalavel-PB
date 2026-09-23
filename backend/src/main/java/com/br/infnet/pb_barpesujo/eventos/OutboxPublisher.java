package com.br.infnet.pb_barpesujo.eventos;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@ConditionalOnProperty(name = "app.eventos.publicador.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;
    private final TransactionTemplate transaction;

    public OutboxPublisher(JdbcTemplate jdbc, RabbitTemplate rabbit, TransactionTemplate transaction) {
        this.jdbc = jdbc;
        this.rabbit = rabbit;
        this.transaction = transaction;
    }

    @Scheduled(fixedDelayString = "${app.eventos.publicador.intervalo-ms:1000}")
    public void publicar() {
        transaction.executeWithoutResult(status -> {
            // ponytail: o lock dura até o broker confirmar; dividir em lotes menores se a taxa crescer.
            List<Pendente> pendentes = jdbc.query("select event_id, routing_key, payload from eventos_outbox "
                    + "where published_at is null order by occurred_at limit 20 for update skip locked",
                    (rs, row) -> new Pendente(rs.getObject("event_id", UUID.class),
                            rs.getString("routing_key"), rs.getString("payload")));
            for (Pendente pendente : pendentes) {
                try {
                    CorrelationData correlation = new CorrelationData(pendente.id().toString());
                    Message mensagem = MessageBuilder.withBody(pendente.payload().getBytes(StandardCharsets.UTF_8))
                            .setContentType("application/json")
                            .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                            .setMessageId(pendente.id().toString()).build();
                    rabbit.send("comandas.eventos", pendente.chave(), mensagem, correlation);
                    if (!correlation.getFuture().get(5, TimeUnit.SECONDS).isAck() || correlation.getReturned() != null) {
                        throw new IllegalStateException("Broker recusou ou não roteou evento " + pendente.id());
                    }
                    jdbc.update("update eventos_outbox set published_at = now() where event_id = ?", pendente.id());
                } catch (Exception e) {
                    log.warn("Publicação pendente para eventId={}", pendente.id(), e);
                }
            }
        });
    }

    private record Pendente(UUID id, String chave, String payload) {}
}
