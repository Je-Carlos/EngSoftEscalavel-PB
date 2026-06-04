package com.br.infnet.pb_barpesujo.mesa.controller;

import com.br.infnet.pb_barpesujo.mesa.dto.MesaRequest;
import com.br.infnet.pb_barpesujo.mesa.dto.MesaResponse;
import com.br.infnet.pb_barpesujo.mesa.dto.MesaStatusRequest;
import com.br.infnet.pb_barpesujo.mesa.service.MesaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    public List<MesaResponse> listar() {
        return mesaService.listar();
    }

    @GetMapping("/{id}")
    public MesaResponse buscarPorId(@PathVariable Long id) {
        return mesaService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<MesaResponse> criar(@Valid @RequestBody MesaRequest request) {
        MesaResponse response = mesaService.criar(request);
        return ResponseEntity.created(URI.create("/api/mesas/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public MesaResponse atualizar(@PathVariable Long id, @Valid @RequestBody MesaRequest request) {
        return mesaService.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    public MesaResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody MesaStatusRequest request) {
        return mesaService.alterarStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        mesaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
