package com.br.infnet.pb_barpesujo.comanda.repository;

import com.br.infnet.pb_barpesujo.comanda.domain.Comanda;
import com.br.infnet.pb_barpesujo.comanda.domain.StatusComanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    boolean existsByMesaIdAndStatus(Long mesaId, StatusComanda status);

    @EntityGraph(attributePaths = "mesa")
    @Query("select c from Comanda c where (:status is null or c.status = :status)")
    Page<Comanda> pesquisar(@Param("status") StatusComanda status, Pageable pageable);
}
