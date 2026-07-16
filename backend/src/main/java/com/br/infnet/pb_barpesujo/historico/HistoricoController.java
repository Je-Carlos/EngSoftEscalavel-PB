package com.br.infnet.pb_barpesujo.historico;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historico")
public class HistoricoController {

    private final HistoricoService historicoService;

    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping("/{entidade}/{id}")
    public List<HistoricoResponse> consultar(@PathVariable TipoEntidade entidade, @PathVariable Long id) {
        return historicoService.consultar(entidade, id);
    }
}
