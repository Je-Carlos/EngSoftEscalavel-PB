import { apiRequest } from '../api/client.js';

export const pesujoService = {
  listarMesas: () => apiRequest('/mesas'),
  listarProdutos: () => apiRequest('/produtos'),
  listarComandas: () => apiRequest('/comandas'),
  cadastrarProduto: (produto) => apiRequest('/produtos', {
    method: 'POST',
    body: JSON.stringify(produto),
  }),
  atualizarProduto: (produtoId, produto) => apiRequest(`/produtos/${produtoId}`, {
    method: 'PUT',
    body: JSON.stringify(produto),
  }),
  abrirComanda: (mesaId) => apiRequest('/comandas/abrir', {
    method: 'POST',
    body: JSON.stringify({ mesaId }),
  }),
  adicionarItem: (comandaId, produtoId, quantidade) => apiRequest(`/comandas/${comandaId}/itens`, {
    method: 'POST',
    body: JSON.stringify({ produtoId, quantidade }),
  }),
  removerItemComanda: (comandaId, itemId) => apiRequest(`/comandas/${comandaId}/itens/${itemId}`, {
    method: 'DELETE',
  }),
  fecharComanda: (comandaId) => apiRequest(`/comandas/${comandaId}/fechar`, {
    method: 'PATCH',
  }),
  cancelarComanda: (comandaId) => apiRequest(`/comandas/${comandaId}/cancelar`, {
    method: 'PATCH',
  }),
};
