package com.br.infnet.pesujo.estoque.service;

import com.br.infnet.eventos.ComandaEvento;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class EstornoEventoIntegrationTest {
    @Autowired EstoqueService estoque;
    @Autowired EstornoEventoListener listener;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    @Test
    void entregaDuplicadaEstornaApenasUmaVez(CapturedOutput output) throws Exception {
        estoque.definirSaldo(901L, 0);
        UUID id = UUID.randomUUID();
        byte[] payload = mapper.writeValueAsBytes(new ComandaEvento(id, ComandaEvento.ITEM_REMOVIDO,
                Instant.now(), 7L, List.of(new ComandaEvento.Item(8L, 901L, 2))));

        listener.receber(payload);
        listener.receber(payload);

        assertThat(estoque.buscarPorProduto(901L).quantidade()).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from eventos_processados where event_id = ?", Integer.class, id))
                .isEqualTo(1);
        assertThat(output).contains("Estorno aplicado").contains("Evento duplicado ignorado")
                .contains("\"eventId\":\"" + id + "\"");
    }

    @Test
    void falhaNoEstornoNaoMarcaEventoComoProcessado() throws Exception {
        UUID id = UUID.randomUUID();
        byte[] payload = mapper.writeValueAsBytes(new ComandaEvento(id, ComandaEvento.COMANDA_CANCELADA,
                Instant.now(), 9L, List.of(new ComandaEvento.Item(10L, 902L, 1))));

        assertThatThrownBy(() -> listener.receber(payload)).isInstanceOf(RuntimeException.class);
        assertThat(jdbc.queryForObject("select count(*) from eventos_processados where event_id = ?", Integer.class, id))
                .isZero();
        estoque.definirSaldo(902L, 0);
        listener.receber(payload);
        assertThat(estoque.buscarPorProduto(902L).quantidade()).isEqualTo(1);
    }
}
