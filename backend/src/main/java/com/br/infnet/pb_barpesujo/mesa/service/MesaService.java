package com.br.infnet.pb_barpesujo.mesa.service;

import com.br.infnet.pb_barpesujo.comanda.domain.StatusComanda;
import com.br.infnet.pb_barpesujo.comanda.repository.ComandaRepository;
import com.br.infnet.pb_barpesujo.mesa.domain.Mesa;
import com.br.infnet.pb_barpesujo.mesa.domain.StatusMesa;
import com.br.infnet.pb_barpesujo.mesa.dto.MesaRequest;
import com.br.infnet.pb_barpesujo.mesa.dto.MesaResponse;
import com.br.infnet.pb_barpesujo.mesa.repository.MesaRepository;
import com.br.infnet.pb_barpesujo.shared.exception.BusinessException;
import com.br.infnet.pb_barpesujo.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final ComandaRepository comandaRepository;

    public MesaService(MesaRepository mesaRepository, ComandaRepository comandaRepository) {
        this.mesaRepository = mesaRepository;
        this.comandaRepository = comandaRepository;
    }

    @Transactional
    public MesaResponse criar(MesaRequest request) {
        if (mesaRepository.existsByNumero(request.numero())) {
            throw new BusinessException("Já existe mesa cadastrada com este número.");
        }
        return toResponse(mesaRepository.save(new Mesa(request.numero(), request.status())));
    }

    @Transactional(readOnly = true)
    public List<MesaResponse> listar() {
        return mesaRepository.findAll().stream().map(MesaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MesaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public MesaResponse atualizar(Long id, MesaRequest request) {
        Mesa mesa = buscarEntidade(id);
        if (mesaRepository.existsByNumeroAndIdNot(request.numero(), id)) {
            throw new BusinessException("Já existe mesa cadastrada com este número.");
        }
        mesa.setNumero(request.numero());
        mesa.setStatus(request.status() == null ? mesa.getStatus() : request.status());
        return toResponse(mesa);
    }

    @Transactional
    public MesaResponse alterarStatus(Long id, StatusMesa status) {
        Mesa mesa = buscarEntidade(id);
        mesa.setStatus(status);
        return toResponse(mesa);
    }

    @Transactional
    public void remover(Long id) {
        Mesa mesa = buscarEntidade(id);
        if (comandaRepository.existsByMesaIdAndStatus(id, StatusComanda.ABERTA)) {
            throw new BusinessException("Não é possível remover mesa com comanda aberta.");
        }
        mesaRepository.delete(mesa);
    }

    public Mesa buscarEntidade(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa não encontrada."));
    }

    public static MesaResponse toResponse(Mesa mesa) {
        return new MesaResponse(mesa.getId(), mesa.getNumero(), mesa.getStatus());
    }
}
