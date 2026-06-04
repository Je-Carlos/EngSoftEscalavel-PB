package com.br.infnet.pb_barpesujo.shared.config;

import com.br.infnet.pb_barpesujo.cardapio.domain.CategoriaProduto;
import com.br.infnet.pb_barpesujo.cardapio.domain.Produto;
import com.br.infnet.pb_barpesujo.cardapio.repository.ProdutoRepository;
import com.br.infnet.pb_barpesujo.mesa.domain.Mesa;
import com.br.infnet.pb_barpesujo.mesa.domain.StatusMesa;
import com.br.infnet.pb_barpesujo.mesa.repository.MesaRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner carregarDadosIniciais(MesaRepository mesaRepository, ProdutoRepository produtoRepository) {
        return args -> {
            if (mesaRepository.count() == 0) {
                for (int numero = 1; numero <= 5; numero++) {
                    mesaRepository.save(new Mesa(numero, StatusMesa.LIVRE));
                }
            }

            if (produtoRepository.count() == 0) {
                produtoRepository.save(new Produto("Pastel de carne", "Pastel frito recheado com carne", CategoriaProduto.SALGADO, new BigDecimal("8.00"), true));
                produtoRepository.save(new Produto("Pastel de queijo", "Pastel frito recheado com queijo", CategoriaProduto.SALGADO, new BigDecimal("7.50"), true));
                produtoRepository.save(new Produto("Torresmo", "Porção crocante de torresmo", CategoriaProduto.PORCAO, new BigDecimal("18.00"), true));
                produtoRepository.save(new Produto("Mandioca frita", "Porção de mandioca frita", CategoriaProduto.PORCAO, new BigDecimal("16.00"), true));
                produtoRepository.save(new Produto("Calabresa acebolada", "Calabresa com cebola na chapa", CategoriaProduto.PORCAO, new BigDecimal("24.00"), true));
                produtoRepository.save(new Produto("Refrigerante", "Lata 350ml", CategoriaProduto.BEBIDA, new BigDecimal("6.00"), true));
                produtoRepository.save(new Produto("Suco natural", "Suco natural da casa", CategoriaProduto.BEBIDA, new BigDecimal("9.00"), true));
                produtoRepository.save(new Produto("Água", "Água mineral sem gás", CategoriaProduto.BEBIDA, new BigDecimal("4.00"), true));
                produtoRepository.save(new Produto("Antarctica", "Cerveja gelada", CategoriaProduto.BEBIDA, new BigDecimal("9.00"), true));
                produtoRepository.save(new Produto("Lokal", "Cerveja popular do balcão", CategoriaProduto.BEBIDA, new BigDecimal("7.00"), true));
                produtoRepository.save(new Produto("Cachaça 51", "Dose de cachaça", CategoriaProduto.BEBIDA, new BigDecimal("5.00"), true));
            }
        };
    }
}
