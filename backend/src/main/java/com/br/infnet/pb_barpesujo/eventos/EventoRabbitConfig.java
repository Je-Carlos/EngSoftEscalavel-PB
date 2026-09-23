package com.br.infnet.pb_barpesujo.eventos;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class EventoRabbitConfig {
    @Bean
    Declarables filasEventos() {
        TopicExchange eventos = new TopicExchange("comandas.eventos", true, false);
        DirectExchange mortos = new DirectExchange("comandas.dlx", true, false);
        Queue estoque = QueueBuilder.durable("comandas.estoque")
                .deadLetterExchange(mortos.getName()).deadLetterRoutingKey("estoque").build();
        Queue auditoria = QueueBuilder.durable("comandas.auditoria")
                .deadLetterExchange(mortos.getName()).deadLetterRoutingKey("auditoria").build();
        Queue estoqueDlq = QueueBuilder.durable("comandas.estoque.dlq").build();
        Queue auditoriaDlq = QueueBuilder.durable("comandas.auditoria.dlq").build();
        return new Declarables(eventos, mortos, estoque, auditoria, estoqueDlq, auditoriaDlq,
                BindingBuilder.bind(estoque).to(eventos).with("comanda.#"),
                BindingBuilder.bind(auditoria).to(eventos).with("comanda.#"),
                BindingBuilder.bind(estoqueDlq).to(mortos).with("estoque"),
                BindingBuilder.bind(auditoriaDlq).to(mortos).with("auditoria"));
    }
}
