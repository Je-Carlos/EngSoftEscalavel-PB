import { Plus, Search } from 'lucide-react';
import { useMemo, useState } from 'react';

const categoriasLabels = {
  PORCAO: 'Porções',
  SALGADO: 'Salgados',
  BEBIDA: 'Bebidas',
  PRATO_FEITO: 'Pratos',
  SOBREMESA: 'Sobremesas',
};

export default function CardapioQuickAdd({ produtos, comandaSelecionada, onAdicionarItem, formatarMoeda }) {
  const [busca, setBusca] = useState('');
  const [categoriaAtiva, setCategoriaAtiva] = useState('TODOS');

  const produtosDisponiveis = produtos.filter((produto) => produto.disponivel);
  const categorias = ['TODOS', ...new Set(produtosDisponiveis.map((produto) => produto.categoria))];

  const produtosFiltrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();

    return produtosDisponiveis.filter((produto) => {
      const bateCategoria = categoriaAtiva === 'TODOS' || produto.categoria === categoriaAtiva;
      const bateBusca = !termo
        || produto.nome.toLowerCase().includes(termo)
        || produto.descricao.toLowerCase().includes(termo);

      return bateCategoria && bateBusca;
    });
  }, [busca, categoriaAtiva, produtosDisponiveis]);

  return (
    <section className="rounded-lg border border-[#d8bd83] bg-white p-4 shadow-sm">
      <div className="mb-3 flex items-center justify-between gap-3">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.14em] text-[#af3324]">Cardápio rápido</p>
          <h3 className="text-lg font-black text-[#263126]">Lançar consumo</h3>
        </div>
        <span className="rounded-full bg-[#fff4dc] px-3 py-1 text-sm font-black text-[#1d6f57]">
          {produtosDisponiveis.length} disponíveis
        </span>
      </div>

      <label className="mb-3 grid gap-1 text-sm font-bold text-[#263126]">
        Buscar no cardápio
        <span className="relative">
          <Search aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-stone-500" size={18} />
          <input
            className="w-full rounded-lg border border-[#d8bd83] bg-[#fff8e8] py-2.5 pl-10 pr-3 text-[#2a241b] outline-none transition focus:border-[#af3324] focus:ring-4 focus:ring-[#ffd04f]/40"
            onChange={(event) => setBusca(event.target.value)}
            placeholder="Pastel, cerveja, torresmo..."
            value={busca}
          />
        </span>
      </label>

      <div className="mb-3 flex flex-wrap gap-2">
        {categorias.map((categoria) => (
          <button
            className={`rounded-full border px-3 py-1.5 text-sm font-black transition ${categoriaAtiva === categoria ? 'border-[#af3324] bg-[#af3324] text-white' : 'border-[#d8bd83] bg-[#fff8e8] text-[#263126] hover:border-[#af3324]'}`}
            key={categoria}
            onClick={() => setCategoriaAtiva(categoria)}
            type="button"
          >
            {categoria === 'TODOS' ? 'Todos' : categoriasLabels[categoria] ?? categoria}
          </button>
        ))}
      </div>

      <div className="grid max-h-[25rem] gap-2 overflow-auto pr-1">
        {produtosFiltrados.map((produto) => (
          <button
            aria-label={`Adicionar ${produto.nome}`}
            className="group flex items-center justify-between gap-3 rounded-lg border border-[#e6d1a1] bg-[#fffdf6] p-3 text-left transition hover:border-[#af3324] hover:bg-[#fff4dc] disabled:cursor-not-allowed disabled:opacity-55"
            disabled={!comandaSelecionada}
            key={produto.id}
            onClick={() => onAdicionarItem(produto.id, 1)}
            type="button"
          >
            <span>
              <strong className="block text-[#263126]">{produto.nome}</strong>
              <small className="text-stone-600">{categoriasLabels[produto.categoria] ?? produto.categoria} · {formatarMoeda(produto.preco)}</small>
            </span>
            <span className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-[#1d6f57] text-white transition group-hover:bg-[#af3324]">
              <Plus aria-hidden="true" size={18} />
            </span>
          </button>
        ))}
        {produtosFiltrados.length === 0 && (
          <p className="rounded-lg border border-dashed border-[#d8bd83] bg-[#fff8e8] p-3 text-sm font-semibold text-stone-600">
            Nenhum produto disponível nesse filtro.
          </p>
        )}
      </div>
    </section>
  );
}
