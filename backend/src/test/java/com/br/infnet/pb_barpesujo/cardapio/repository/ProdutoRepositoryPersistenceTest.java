package com.br.infnet.pb_barpesujo.cardapio.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.br.infnet.pb_barpesujo.cardapio.domain.CategoriaProduto;
import com.br.infnet.pb_barpesujo.cardapio.domain.Produto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@SpringBootTest
class ProdutoRepositoryPersistenceTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void pesquisaProdutosComFiltroPaginacaoEOrdenacao() {
        produtoRepository.save(new Produto("Z Teste", "Produto de teste", CategoriaProduto.BEBIDA, new BigDecimal("10.00"), true));
        produtoRepository.save(new Produto("A Teste", "Produto de teste", CategoriaProduto.BEBIDA, new BigDecimal("5.00"), true));

        Page<Produto> pagina = produtoRepository.pesquisar(
                "teste", CategoriaProduto.BEBIDA, true, PageRequest.of(0, 1, Sort.by("nome")));

        assertThat(pagina.getTotalElements()).isEqualTo(2);
        assertThat(pagina.getContent()).extracting(Produto::getNome).containsExactly("A Teste");
    }
}
