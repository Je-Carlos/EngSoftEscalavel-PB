package com.br.infnet.pesujo.estoque.service;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EstornoRabbitConfig {
    @Bean
    Declarables filaEstoque() {
        TopicExchange eventos = new TopicExchange("comandas.eventos", true, false);
        DirectExchange mortos = new DirectExchange("comandas.dlx", true, false);
        Queue estoque = QueueBuilder.durable("comandas.estoque")
                .deadLetterExchange(mortos.getName()).deadLetterRoutingKey("estoque").build();
        Queue dlq = QueueBuilder.durable("comandas.estoque.dlq").build();
        return new Declarables(eventos, mortos, estoque, dlq,
                BindingBuilder.bind(estoque).to(eventos).with("comanda.#"),
                BindingBuilder.bind(dlq).to(mortos).with("estoque"));
    }
}
