package com.br.infnet.pb_barpesujo.cardapio.repository;

import com.br.infnet.pb_barpesujo.cardapio.domain.Produto;
import com.br.infnet.pb_barpesujo.cardapio.domain.CategoriaProduto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("""
            select p from Produto p
            where (:nome is null or lower(p.nome) like lower(concat('%', :nome, '%')))
              and (:categoria is null or p.categoria = :categoria)
              and (:disponivel is null or p.disponivel = :disponivel)
            """)
    Page<Produto> pesquisar(
            @Param("nome") String nome,
            @Param("categoria") CategoriaProduto categoria,
            @Param("disponivel") Boolean disponivel,
            Pageable pageable);
}
