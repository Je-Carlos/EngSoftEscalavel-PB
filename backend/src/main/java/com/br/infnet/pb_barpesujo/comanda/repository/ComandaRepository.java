package com.br.infnet.pb_barpesujo.comanda.repository;

import com.br.infnet.pb_barpesujo.comanda.domain.Comanda;
import com.br.infnet.pb_barpesujo.comanda.domain.StatusComanda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    boolean existsByMesaIdAndStatus(Long mesaId, StatusComanda status);
}
