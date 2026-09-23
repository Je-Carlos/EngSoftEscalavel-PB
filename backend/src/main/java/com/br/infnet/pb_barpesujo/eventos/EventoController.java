package com.br.infnet.pb_barpesujo.eventos;

import com.br.infnet.eventos.ComandaEvento;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/comandas")
public class EventoController {
    private final EventoAuditoria auditoria;

    public EventoController(EventoAuditoria auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping("/{id}")
    public List<ComandaEvento> listar(@PathVariable Long id) {
        return auditoria.listar(id);
    }
}
