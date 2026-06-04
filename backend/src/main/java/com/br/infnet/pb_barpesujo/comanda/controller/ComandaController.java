package com.br.infnet.pb_barpesujo.comanda.controller;

import com.br.infnet.pb_barpesujo.comanda.dto.AbrirComandaRequest;
import com.br.infnet.pb_barpesujo.comanda.dto.AdicionarItemComandaRequest;
import com.br.infnet.pb_barpesujo.comanda.dto.ComandaResponse;
import com.br.infnet.pb_barpesujo.comanda.dto.TotalComandaResponse;
import com.br.infnet.pb_barpesujo.comanda.service.ComandaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comandas")
public class ComandaController {

    private final ComandaService comandaService;

    public ComandaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping
    public List<ComandaResponse> listar() {
        return comandaService.listar();
    }

    @GetMapping("/{id}")
    public ComandaResponse buscarPorId(@PathVariable Long id) {
        return comandaService.buscarPorId(id);
    }

    @PostMapping("/abrir")
    public ResponseEntity<ComandaResponse> abrir(@Valid @RequestBody AbrirComandaRequest request) {
        ComandaResponse response = comandaService.abrir(request);
        return ResponseEntity.created(URI.create("/api/comandas/" + response.id())).body(response);
    }

    @PostMapping("/{id}/itens")
    public ComandaResponse adicionarItem(
            @PathVariable Long id,
            @Valid @RequestBody AdicionarItemComandaRequest request) {
        return comandaService.adicionarItem(id, request);
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    public ComandaResponse removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        return comandaService.removerItem(id, itemId);
    }

    @GetMapping("/{id}/total")
    public TotalComandaResponse calcularTotal(@PathVariable Long id) {
        return comandaService.calcularTotal(id);
    }

    @PatchMapping("/{id}/fechar")
    public ComandaResponse fechar(@PathVariable Long id) {
        return comandaService.fechar(id);
    }

    @PatchMapping("/{id}/cancelar")
    public ComandaResponse cancelar(@PathVariable Long id) {
        return comandaService.cancelar(id);
    }
}
