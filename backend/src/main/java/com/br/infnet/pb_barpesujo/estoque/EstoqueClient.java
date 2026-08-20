package com.br.infnet.pb_barpesujo.estoque;

import com.br.infnet.pb_barpesujo.estoque.dto.AtualizarEstoqueRequest;
import com.br.infnet.pb_barpesujo.estoque.dto.EstoqueResponse;
import com.br.infnet.pb_barpesujo.estoque.dto.MovimentacaoEstoqueRequest;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "estoque-service", path = "/api/estoques")
public interface EstoqueClient {

    @GetMapping
    List<EstoqueResponse> listar();

    @PutMapping("/{produtoId}")
    EstoqueResponse definirSaldo(@PathVariable Long produtoId, @RequestBody AtualizarEstoqueRequest request);

    @PostMapping("/movimentacoes/baixas")
    void baixar(@RequestBody MovimentacaoEstoqueRequest request);

    @PostMapping("/movimentacoes/reposicoes")
    void repor(@RequestBody MovimentacaoEstoqueRequest request);
}
