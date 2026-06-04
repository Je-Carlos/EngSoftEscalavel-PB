package com.br.infnet.pb_barpesujo.mesa.repository;

import com.br.infnet.pb_barpesujo.mesa.domain.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    boolean existsByNumero(Integer numero);

    boolean existsByNumeroAndIdNot(Integer numero, Long id);
}
