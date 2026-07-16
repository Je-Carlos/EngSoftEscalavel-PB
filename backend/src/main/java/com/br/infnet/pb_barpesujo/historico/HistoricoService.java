package com.br.infnet.pb_barpesujo.historico;

import com.br.infnet.pb_barpesujo.cardapio.domain.Produto;
import com.br.infnet.pb_barpesujo.comanda.domain.Comanda;
import com.br.infnet.pb_barpesujo.comanda.domain.ItemComanda;
import com.br.infnet.pb_barpesujo.mesa.domain.Mesa;
import com.br.infnet.pb_barpesujo.shared.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoricoService {

    private final EntityManager entityManager;

    public HistoricoService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> consultar(TipoEntidade tipo, Long id) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<?> revisoes = reader.createQuery()
                .forRevisionsOfEntity(classe(tipo), false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();
        if (revisoes.isEmpty()) {
            throw new ResourceNotFoundException("Histórico não encontrado.");
        }

        List<HistoricoResponse> resultado = new ArrayList<>();
        Map<String, Object> anterior = null;
        for (Object revisaoBruta : revisoes) {
            Object[] revisao = (Object[]) revisaoBruta;
            Map<String, Object> atual = snapshot(tipo, revisao[0]);
            RevisionType tipoRevisao = (RevisionType) revisao[2];
            TipoOperacao operacao = operacao(tipoRevisao);
            Map<String, Object> antes = operacao == TipoOperacao.CRIACAO ? null : anterior;
            Map<String, Object> depois = operacao == TipoOperacao.EXCLUSAO ? null : atual;
            DefaultRevisionEntity metadados = (DefaultRevisionEntity) revisao[1];
            resultado.add(new HistoricoResponse(
                    tipo, id, metadados.getId(), operacao, Instant.ofEpochMilli(metadados.getTimestamp()), antes, depois, null));
            anterior = atual;
        }
        return resultado;
    }

    private static Class<?> classe(TipoEntidade tipo) {
        return switch (tipo) {
            case MESA -> Mesa.class;
            case PRODUTO -> Produto.class;
            case COMANDA -> Comanda.class;
            case ITEM_COMANDA -> ItemComanda.class;
        };
    }

    private static TipoOperacao operacao(RevisionType tipo) {
        return switch (tipo) {
            case ADD -> TipoOperacao.CRIACAO;
            case MOD -> TipoOperacao.ALTERACAO;
            case DEL -> TipoOperacao.EXCLUSAO;
        };
    }

    private static Map<String, Object> snapshot(TipoEntidade tipo, Object entidade) {
        Map<String, Object> campos = new LinkedHashMap<>();
        switch (tipo) {
            case MESA -> {
                Mesa mesa = (Mesa) entidade;
                campos.put("id", mesa.getId());
                campos.put("numero", mesa.getNumero());
                campos.put("status", mesa.getStatus());
            }
            case PRODUTO -> {
                Produto produto = (Produto) entidade;
                campos.put("id", produto.getId());
                campos.put("nome", produto.getNome());
                campos.put("descricao", produto.getDescricao());
                campos.put("categoria", produto.getCategoria());
                campos.put("preco", produto.getPreco());
                campos.put("disponivel", produto.isDisponivel());
            }
            case COMANDA -> {
                Comanda comanda = (Comanda) entidade;
                campos.put("id", comanda.getId());
                campos.put("mesaId", comanda.getMesa().getId());
                campos.put("status", comanda.getStatus());
                campos.put("abertaEm", comanda.getAbertaEm());
                campos.put("fechadaEm", comanda.getFechadaEm());
            }
            case ITEM_COMANDA -> {
                ItemComanda item = (ItemComanda) entidade;
                campos.put("id", item.getId());
                campos.put("comandaId", item.getComanda().getId());
                campos.put("produtoId", item.getProduto().getId());
                campos.put("quantidade", item.getQuantidade());
                campos.put("precoUnitario", item.getPrecoUnitario());
            }
        }
        return campos;
    }
}
