package com.br.infnet.pb_barpesujo.eventos;

import com.br.infnet.eventos.ComandaEvento;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class PublicacaoAuditoriaIntegrationTest {
    @Autowired Outbox outbox;
    @Autowired OutboxPublisher publisher;
    @Autowired EventoAuditoria auditoria;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean RabbitTemplate rabbit;

    @Test
    void falhaDoBrokerMantemEventoPendenteEAuditoriaAceitaEntregaDuplicada() throws Exception {
        long comandaId = 987654L;
        outbox.gravar(ComandaEvento.ITEM_REMOVIDO, comandaId,
                List.of(new ComandaEvento.Item(12L, 34L, 2)));
        UUID alvo = jdbc.queryForObject("select event_id from eventos_outbox where aggregate_id = ?", UUID.class, comandaId);
        AtomicInteger tentativas = new AtomicInteger();
        doAnswer(call -> {
            CorrelationData correlation = call.getArgument(3);
            if (correlation.getId().equals(alvo.toString()) && tentativas.getAndIncrement() == 0) {
                throw new IllegalStateException("broker off");
            }
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(), any(CorrelationData.class));

        publisher.publicar();
        assertThat(jdbc.queryForObject("select count(*) from eventos_outbox where aggregate_id = ? and published_at is null",
                Integer.class, comandaId)).isEqualTo(1);
        publisher.publicar();
        assertThat(jdbc.queryForObject("select count(*) from eventos_outbox where aggregate_id = ? and published_at is null",
                Integer.class, comandaId)).isZero();

        String payload = jdbc.queryForObject("select payload from eventos_outbox where aggregate_id = ?", String.class, comandaId);
        auditoria.receber(payload);
        auditoria.receber(payload);
        assertThat(auditoria.listar(comandaId)).hasSize(1);
        assertThat(auditoria.listar(comandaId).getFirst().itens().getFirst().produtoId()).isEqualTo(34L);
    }
}
