package com.br.infnet.pesujo.estoque.repository;

import com.br.infnet.pesujo.estoque.domain.Estoque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByProdutoId(Long produtoId);

    @Modifying
    @Query(value = "insert into estoques (produto_id, quantidade) values (:produtoId, :quantidade) "
            + "on conflict (produto_id) do update set quantidade = excluded.quantidade", nativeQuery = true)
    void definirSaldo(@Param("produtoId") Long produtoId, @Param("quantidade") Integer quantidade);

    @Lock(PESSIMISTIC_WRITE)
    @Query("select e from Estoque e where e.produtoId in :produtoIds")
    List<Estoque> findAllByProdutoIdInForUpdate(@Param("produtoIds") Collection<Long> produtoIds);
}
