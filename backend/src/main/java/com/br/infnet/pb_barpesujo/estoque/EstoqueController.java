package com.br.infnet.pb_barpesujo.estoque;

import com.br.infnet.pb_barpesujo.estoque.dto.AtualizarEstoqueRequest;
import com.br.infnet.pb_barpesujo.estoque.dto.EstoqueResponse;
import com.br.infnet.pb_barpesujo.cardapio.service.ProdutoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estoques")
public class EstoqueController {

    private final EstoqueClient estoqueClient;
    private final ProdutoService produtoService;

    public EstoqueController(EstoqueClient estoqueClient, ProdutoService produtoService) {
        this.estoqueClient = estoqueClient;
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<EstoqueResponse> listar() {
        return estoqueClient.listar();
    }

    @PutMapping("/{produtoId}")
    public EstoqueResponse definirSaldo(@PathVariable @Positive Long produtoId, @Valid @RequestBody AtualizarEstoqueRequest request) {
        produtoService.buscarPorId(produtoId);
        return estoqueClient.definirSaldo(produtoId, request);
    }
}
