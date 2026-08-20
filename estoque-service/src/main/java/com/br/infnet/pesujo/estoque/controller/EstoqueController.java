package com.br.infnet.pesujo.estoque.controller;

import com.br.infnet.pesujo.estoque.dto.AtualizarEstoqueRequest;
import com.br.infnet.pesujo.estoque.dto.EstoqueResponse;
import com.br.infnet.pesujo.estoque.dto.MovimentacaoEstoqueRequest;
import com.br.infnet.pesujo.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    public List<EstoqueResponse> listar() {
        return estoqueService.listar();
    }

    @GetMapping("/{produtoId}")
    public EstoqueResponse buscarPorProduto(@PathVariable @Positive Long produtoId) {
        return estoqueService.buscarPorProduto(produtoId);
    }

    @PutMapping("/{produtoId}")
    public EstoqueResponse definirSaldo(@PathVariable @Positive Long produtoId, @Valid @RequestBody AtualizarEstoqueRequest request) {
        return estoqueService.definirSaldo(produtoId, request.quantidade());
    }

    @PostMapping("/movimentacoes/baixas")
    public void baixar(@Valid @RequestBody MovimentacaoEstoqueRequest request) {
        estoqueService.baixar(request.itens());
    }

    @PostMapping("/movimentacoes/reposicoes")
    public void repor(@Valid @RequestBody MovimentacaoEstoqueRequest request) {
        estoqueService.repor(request.itens());
    }
}
