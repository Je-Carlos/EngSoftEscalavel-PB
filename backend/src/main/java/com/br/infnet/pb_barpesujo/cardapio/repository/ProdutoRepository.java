package com.br.infnet.pb_barpesujo.cardapio.repository;

import com.br.infnet.pb_barpesujo.cardapio.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
