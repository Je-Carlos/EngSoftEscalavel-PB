import { ClipboardEdit, Save } from 'lucide-react';
import { useState } from 'react';
import StatusPill from './StatusPill.jsx';

const categorias = ['PORCAO', 'SALGADO', 'BEBIDA', 'PRATO_FEITO', 'SOBREMESA'];

const categoriasLabels = {
  PORCAO: 'Porção',
  SALGADO: 'Salgado',
  BEBIDA: 'Bebida',
  PRATO_FEITO: 'Prato feito',
  SOBREMESA: 'Sobremesa',
};

const produtoInicial = {
  nome: '',
  descricao: '',
  categoria: 'BEBIDA',
  preco: '',
  disponivel: true,
};

export default function ProdutoManager({
  aberto,
  produtos,
  produtosEdicao,
  produtoEmEdicao,
  produtoForm,
  setAberto,
  setProdutoEmEdicao,
  setProdutoForm,
  editarProduto,
  salvarProduto,
  cadastrarProduto,
  formatarMoeda,
  estoques,
  atualizarEstoque,
}) {
  const [mostrarLista, setMostrarLista] = useState(false);
  const [saldosEdicao, setSaldosEdicao] = useState({});

  return (
    <section className="rounded-lg border border-[#d8bd83] bg-[#fff8e8] p-4 shadow-sm">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.14em] text-[#af3324]">Operação secundária</p>
          <h2 className="font-display text-3xl text-[#263126]">Cardápio do Pé Sujo</h2>
        </div>
        <button
          className="inline-flex items-center justify-center gap-2 rounded-lg bg-[#1d6f57] px-4 py-2.5 font-black text-white transition hover:bg-[#145443]"
          onClick={() => setAberto(!aberto)}
          type="button"
        >
          <ClipboardEdit aria-hidden="true" size={18} />
          Gerenciar cardápio
        </button>
      </div>

      {aberto && (
        <div className="mt-4 grid gap-4 xl:grid-cols-[0.8fr_1.2fr]">
          <form className="grid gap-3 rounded-lg border border-[#d8bd83] bg-white p-4" onSubmit={cadastrarProduto}>
            <h3 className="text-lg font-black text-[#263126]">Novo produto</h3>
            <label className="grid gap-1 text-sm font-bold text-[#263126]">
              Nome do novo produto
              <input
                className="rounded-lg border border-[#d8bd83] bg-[#fff8e8] px-3 py-2.5 outline-none focus:border-[#af3324] focus:ring-4 focus:ring-[#ffd04f]/40"
                onChange={(event) => setProdutoForm({ ...produtoForm, nome: event.target.value })}
                required
                value={produtoForm.nome}
              />
            </label>
            <label className="grid gap-1 text-sm font-bold text-[#263126]">
              Descrição do novo produto
              <input
                className="rounded-lg border border-[#d8bd83] bg-[#fff8e8] px-3 py-2.5 outline-none focus:border-[#af3324] focus:ring-4 focus:ring-[#ffd04f]/40"
                onChange={(event) => setProdutoForm({ ...produtoForm, descricao: event.target.value })}
                required
                value={produtoForm.descricao}
              />
            </label>
            <div className="grid gap-3 sm:grid-cols-2">
              <label className="grid gap-1 text-sm font-bold text-[#263126]">
                Categoria do novo produto
                <select
                  className="rounded-lg border border-[#d8bd83] bg-[#fff8e8] px-3 py-2.5 outline-none focus:border-[#af3324] focus:ring-4 focus:ring-[#ffd04f]/40"
                  onChange={(event) => setProdutoForm({ ...produtoForm, categoria: event.target.value })}
                  value={produtoForm.categoria}
                >
                  {categorias.map((categoria) => (
                    <option key={categoria} value={categoria}>{categoriasLabels[categoria]}</option>
                  ))}
                </select>
              </label>
              <label className="grid gap-1 text-sm font-bold text-[#263126]">
                Preço do novo produto
                <input
                  className="rounded-lg border border-[#d8bd83] bg-[#fff8e8] px-3 py-2.5 outline-none focus:border-[#af3324] focus:ring-4 focus:ring-[#ffd04f]/40"
                  min="0.01"
                  onChange={(event) => setProdutoForm({ ...produtoForm, preco: event.target.value })}
                  required
                  step="0.01"
                  type="number"
                  value={produtoForm.preco}
                />
              </label>
            </div>
            <button className="inline-flex items-center justify-center gap-2 rounded-lg bg-[#af3324] px-4 py-3 font-black text-white transition hover:bg-[#84261a]" type="submit">
              <Save aria-hidden="true" size={18} />
              Cadastrar produto
            </button>
          </form>

          <div className="rounded-lg border border-[#d8bd83] bg-white p-4">
            <div className="mb-3 flex items-center justify-between gap-3">
              <h3 className="text-lg font-black text-[#263126]">Produtos cadastrados</h3>
              <button
                className="rounded-full border border-[#d8bd83] px-3 py-1.5 text-sm font-black text-[#263126] hover:border-[#af3324]"
                onClick={() => setMostrarLista(!mostrarLista)}
                type="button"
              >
                {mostrarLista ? 'Ocultar lista' : 'Mostrar lista'}
              </button>
            </div>

            {mostrarLista && (
              <div className="grid max-h-[28rem] gap-3 overflow-auto pr-1">
                {produtos.map((produto) => {
                  const produtoEditado = produtosEdicao[produto.id] ?? produto;
                  const editando = produtoEmEdicao === produto.id;

                  return (
                    <article className="rounded-lg border border-[#e6d1a1] bg-[#fffdf6] p-3" key={produto.id}>
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <strong className="block text-[#263126]">{produto.nome}</strong>
                          <small className="text-stone-600">{categoriasLabels[produto.categoria]} · {formatarMoeda(produto.preco)}</small>
                        </div>
                        <div className="flex items-center gap-2">
                          <StatusPill status={produto.disponivel ? 'DISPONIVEL' : 'INDISPONIVEL'} />
                          <button
                            className="rounded-full bg-[#263126] px-3 py-1.5 text-sm font-black text-[#ffd04f]"
                            onClick={() => setProdutoEmEdicao(editando ? null : produto.id)}
                            type="button"
                          >
                            Editar
                          </button>
                        </div>
                      </div>

                      <div className="mt-3 flex items-end gap-2">
                        <label className="grid gap-1 text-sm font-bold text-[#263126]">
                          Saldo em estoque
                          <input
                            aria-label={`Saldo ${produto.nome}`}
                            className="w-28 rounded-lg border border-[#d8bd83] px-3 py-2"
                            min="0"
                            onChange={(event) => setSaldosEdicao({ ...saldosEdicao, [produto.id]: event.target.value })}
                            type="number"
                            value={saldosEdicao[produto.id] ?? estoques[produto.id] ?? 0}
                          />
                        </label>
                        <button
                          className="rounded-lg bg-[#1d6f57] px-3 py-2 font-black text-white"
                          onClick={() => atualizarEstoque(produto.id, Number(saldosEdicao[produto.id] ?? estoques[produto.id] ?? 0))}
                          type="button"
                        >
                          Atualizar estoque
                        </button>
                      </div>

                      {editando && (
                        <div className="mt-3 grid gap-2 md:grid-cols-2">
                          <input className="rounded-lg border border-[#d8bd83] px-3 py-2" onChange={(event) => editarProduto(produto.id, 'nome', event.target.value)} value={produtoEditado.nome} />
                          <input className="rounded-lg border border-[#d8bd83] px-3 py-2" onChange={(event) => editarProduto(produto.id, 'descricao', event.target.value)} value={produtoEditado.descricao} />
                          <select className="rounded-lg border border-[#d8bd83] px-3 py-2" onChange={(event) => editarProduto(produto.id, 'categoria', event.target.value)} value={produtoEditado.categoria}>
                            {categorias.map((categoria) => (
                              <option key={categoria} value={categoria}>{categoriasLabels[categoria]}</option>
                            ))}
                          </select>
                          <input className="rounded-lg border border-[#d8bd83] px-3 py-2" min="0.01" onChange={(event) => editarProduto(produto.id, 'preco', event.target.value)} step="0.01" type="number" value={produtoEditado.preco} />
                          <label className="flex items-center gap-2 text-sm font-bold text-[#263126]">
                            <input checked={produtoEditado.disponivel} onChange={(event) => editarProduto(produto.id, 'disponivel', event.target.checked)} type="checkbox" />
                            Produto disponível
                          </label>
                          <button className="rounded-lg bg-[#1d6f57] px-4 py-2 font-black text-white" onClick={() => salvarProduto(produto)} type="button">
                            Salvar produto
                          </button>
                        </div>
                      )}
                    </article>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}
    </section>
  );
}

export { produtoInicial };
