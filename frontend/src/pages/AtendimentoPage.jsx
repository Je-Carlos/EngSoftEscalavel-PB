import { useEffect, useMemo, useState } from 'react';
import { Clock3, RefreshCw, UtensilsCrossed } from 'lucide-react';
import CardapioQuickAdd from '../components/CardapioQuickAdd.jsx';
import ComandaPanel from '../components/ComandaPanel.jsx';
import MesaGrid from '../components/MesaGrid.jsx';
import ProdutoManager, { produtoInicial } from '../components/ProdutoManager.jsx';
import { pesujoService } from '../services/pesujoService.js';

function formatarMoeda(valor) {
  return Number(valor ?? 0)
    .toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
    .replace(/\u00a0/g, ' ');
}

function escolherComandaInicial(comandas) {
  return comandas.find((comanda) => comanda.status === 'ABERTA' && comanda.itens.length > 0)
    ?? comandas.find((comanda) => comanda.status === 'ABERTA')
    ?? null;
}

export default function AtendimentoPage() {
  const [mesas, setMesas] = useState([]);
  const [produtos, setProdutos] = useState([]);
  const [comandas, setComandas] = useState([]);
  const [estoques, setEstoques] = useState({});
  const [produtosEdicao, setProdutosEdicao] = useState({});
  const [produtoEmEdicao, setProdutoEmEdicao] = useState(null);
  const [produtoForm, setProdutoForm] = useState(produtoInicial);
  const [comandaSelecionadaId, setComandaSelecionadaId] = useState(null);
  const [mesaSelecionadaId, setMesaSelecionadaId] = useState(null);
  const [gerenciadorAberto, setGerenciadorAberto] = useState(false);
  const [mensagem, setMensagem] = useState('');
  const [carregando, setCarregando] = useState(false);

  const comandasAbertas = useMemo(
    () => comandas.filter((comanda) => comanda.status === 'ABERTA'),
    [comandas],
  );

  const comandaSelecionada = useMemo(
    () => comandasAbertas.find((comanda) => comanda.id === comandaSelecionadaId) ?? escolherComandaInicial(comandasAbertas),
    [comandaSelecionadaId, comandasAbertas],
  );

  const mesasLivres = mesas.filter((mesa) => mesa.status === 'LIVRE');
  const mesasOcupadas = mesas.filter((mesa) => mesa.status !== 'LIVRE');
  const totalEmAberto = comandasAbertas.reduce((total, comanda) => total + Number(comanda.total ?? 0), 0);

  async function carregarDados({ selecionarPrimeira = false } = {}) {
    setCarregando(true);
    setMensagem('');
    try {
      const [mesasApi, produtosApi, comandasApi, estoquesApi] = await Promise.all([
        pesujoService.listarMesas(),
        pesujoService.listarProdutos(),
        pesujoService.listarComandas(),
        pesujoService.listarEstoques(),
      ]);
      const comandaInicial = escolherComandaInicial(comandasApi);

      setMesas(mesasApi);
      setProdutos(produtosApi);
      setComandas(comandasApi);
      setEstoques(Object.fromEntries(estoquesApi.map((estoque) => [estoque.produtoId, estoque.quantidade])));
      setProdutosEdicao(Object.fromEntries(produtosApi.map((produto) => [produto.id, { ...produto }])));

      if (selecionarPrimeira || !comandaSelecionadaId) {
        setComandaSelecionadaId(comandaInicial?.id ?? null);
        setMesaSelecionadaId(comandaInicial?.mesa.id ?? mesasApi[0]?.id ?? null);
      }
    } catch (error) {
      setMensagem(`Não foi possível carregar a API: ${error.message}`);
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregarDados({ selecionarPrimeira: true });
  }, []);

  async function selecionarMesa(mesa) {
    setMesaSelecionadaId(mesa.id);
    const comandaDaMesa = comandasAbertas.find((comanda) => comanda.mesa.id === mesa.id);

    if (comandaDaMesa) {
      setComandaSelecionadaId(comandaDaMesa.id);
      setMensagem(`Mesa ${mesa.numero} selecionada.`);
      return;
    }

    if (mesa.status !== 'LIVRE') {
      setMensagem(`Mesa ${mesa.numero} não possui comanda aberta carregada.`);
      return;
    }

    try {
      const novaComanda = await pesujoService.abrirComanda(Number(mesa.id));
      setComandas((atuais) => [novaComanda, ...atuais.filter((comanda) => comanda.id !== novaComanda.id)]);
      setMesas((atuais) => atuais.map((mesaAtual) => (
        mesaAtual.id === mesa.id ? { ...mesaAtual, status: 'OCUPADA' } : mesaAtual
      )));
      setComandaSelecionadaId(novaComanda.id);
      setMesaSelecionadaId(novaComanda.mesa.id);
      setMensagem(`Comanda ${novaComanda.id} aberta para a mesa ${novaComanda.mesa.numero}.`);
    } catch (error) {
      setMensagem(error.message);
    }
  }

  async function adicionarItem(produtoId, quantidade = 1) {
    if (!comandaSelecionada) {
      setMensagem('Selecione uma comanda antes de adicionar itens.');
      return;
    }

    try {
      const comandaAtualizada = await pesujoService.adicionarItem(
        Number(comandaSelecionada.id),
        Number(produtoId),
        Number(quantidade),
      );
      setComandas((atuais) => atuais.map((comanda) => (
        comanda.id === comandaAtualizada.id ? comandaAtualizada : comanda
      )));
      setMensagem('Item adicionado à comanda.');
      await carregarDados();
    } catch (error) {
      setMensagem(error.message);
    }
  }

  async function removerItem(comandaId, itemId) {
    try {
      const comandaAtualizada = await pesujoService.removerItemComanda(comandaId, itemId);
      setComandas((atuais) => atuais.map((comanda) => (
        comanda.id === comandaAtualizada.id ? comandaAtualizada : comanda
      )));
      setMensagem('Item removido da comanda.');
      await carregarDados();
    } catch (error) {
      setMensagem(error.message);
    }
  }

  async function fecharComanda(comandaId) {
    try {
      await pesujoService.fecharComanda(comandaId);
      setMensagem('Conta fechada. Mesa liberada.');
      await carregarDados({ selecionarPrimeira: true });
    } catch (error) {
      setMensagem(error.message);
    }
  }

  async function cadastrarProduto(event) {
    event.preventDefault();
    try {
      await pesujoService.cadastrarProduto({
        ...produtoForm,
        preco: Number(produtoForm.preco),
      });
      setProdutoForm(produtoInicial);
      setMensagem('Produto cadastrado no cardápio.');
      await carregarDados();
    } catch (error) {
      setMensagem(error.message);
    }
  }

  async function salvarProduto(produto) {
    const produtoEditado = produtosEdicao[produto.id] ?? produto;
    try {
      await pesujoService.atualizarProduto(produto.id, {
        nome: produtoEditado.nome,
        descricao: produtoEditado.descricao,
        categoria: produtoEditado.categoria,
        preco: Number(produtoEditado.preco),
        disponivel: Boolean(produtoEditado.disponivel),
      });
      setMensagem(`Produto ${produtoEditado.nome} atualizado.`);
      setProdutoEmEdicao(null);
      await carregarDados();
    } catch (error) {
      setMensagem(error.message);
    }
  }

  async function atualizarEstoque(produtoId, quantidade) {
    try {
      const estoque = await pesujoService.atualizarEstoque(produtoId, quantidade);
      setEstoques((atuais) => ({ ...atuais, [estoque.produtoId]: estoque.quantidade }));
      setMensagem('Estoque atualizado.');
    } catch (error) {
      setMensagem(error.message);
    }
  }

  function editarProduto(produtoId, campo, valor) {
    setProdutosEdicao((atual) => ({
      ...atual,
      [produtoId]: {
        ...atual[produtoId],
        [campo]: valor,
      },
    }));
  }

  return (
    <main className="min-h-screen bg-[#fff4dc] bg-[radial-gradient(circle_at_top_left,rgba(175,51,36,0.12),transparent_30rem),linear-gradient(135deg,#fff4dc_0%,#fff8e8_45%,#eaf1df_100%)] p-3 text-[#2a241b] sm:p-5">
      <section className="mx-auto grid max-w-[90rem] gap-4">
        <header className="overflow-hidden rounded-lg border border-[#d8bd83] bg-[#fff8e8] shadow-sm">
          <div className="grid gap-4 p-5 lg:grid-cols-[1fr_auto] lg:items-center">
            <div>
              <p className="text-xs font-black uppercase tracking-[0.18em] text-[#af3324]">Boteco tradicional brasileiro</p>
              <h1 className="font-display text-6xl leading-none text-[#af3324] sm:text-7xl">Pé Sujo</h1>
              <p className="mt-2 max-w-2xl text-base font-semibold text-stone-700">
                Salão e comandas em tempo real para abrir mesa, lançar consumo e fechar conta sem perder o ritmo do balcão.
              </p>
            </div>
            <div className="grid gap-2 rounded-lg bg-[#263126] p-4 text-[#fff4dc] sm:grid-cols-3 lg:min-w-[31rem]">
              <span className="rounded-lg bg-white/10 p-3">
                <small className="block text-[#ffd04f]">Livres</small>
                <strong className="font-display text-3xl">{mesasLivres.length}</strong>
              </span>
              <span className="rounded-lg bg-white/10 p-3">
                <small className="block text-[#ffd04f]">Ocupadas</small>
                <strong className="font-display text-3xl">{mesasOcupadas.length}</strong>
              </span>
              <span className="rounded-lg bg-white/10 p-3">
                <small className="block text-[#ffd04f]">Em aberto</small>
                <strong className="font-display text-3xl">{formatarMoeda(totalEmAberto)}</strong>
              </span>
            </div>
          </div>
          <div className="flex flex-wrap items-center justify-between gap-3 border-t border-[#e7c98c] bg-[#1d6f57] px-5 py-3 text-sm font-bold text-white">
            <span className="inline-flex items-center gap-2">
              <UtensilsCrossed aria-hidden="true" size={18} />
              Atendimento PDV
            </span>
            <span className="inline-flex items-center gap-2 text-[#fff4dc]">
              <Clock3 aria-hidden="true" size={18} />
              Fluxo rápido de mesa, consumo e conta
            </span>
            <button
              className="inline-flex items-center gap-2 rounded-full bg-[#fff4dc] px-3 py-1.5 font-black text-[#1d6f57] transition hover:bg-[#ffd04f]"
              disabled={carregando}
              onClick={() => carregarDados({ selecionarPrimeira: true })}
              type="button"
            >
              <RefreshCw aria-hidden="true" className={carregando ? 'animate-spin' : ''} size={16} />
              Atualizar salão
            </button>
          </div>
        </header>

        {mensagem && (
          <p className="rounded-lg border border-[#ffd04f] bg-white px-4 py-3 font-bold text-[#263126] shadow-sm">
            {mensagem}
          </p>
        )}

        <section className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_25rem]">
          <div className="grid gap-4">
            <MesaGrid
              comandasAbertas={comandasAbertas}
              formatarMoeda={formatarMoeda}
              mesaSelecionadaId={mesaSelecionadaId ?? comandaSelecionada?.mesa.id}
              mesas={mesas}
              onSelecionarMesa={selecionarMesa}
            />
            <CardapioQuickAdd
              comandaSelecionada={comandaSelecionada}
              estoques={estoques}
              formatarMoeda={formatarMoeda}
              onAdicionarItem={adicionarItem}
              produtos={produtos}
            />
            <ProdutoManager
              aberto={gerenciadorAberto}
              cadastrarProduto={cadastrarProduto}
              editarProduto={editarProduto}
              estoques={estoques}
              formatarMoeda={formatarMoeda}
              produtoEmEdicao={produtoEmEdicao}
              produtoForm={produtoForm}
              produtos={produtos}
              produtosEdicao={produtosEdicao}
              salvarProduto={salvarProduto}
              atualizarEstoque={atualizarEstoque}
              setAberto={setGerenciadorAberto}
              setProdutoEmEdicao={setProdutoEmEdicao}
              setProdutoForm={setProdutoForm}
            />
          </div>
          <ComandaPanel
            comanda={comandaSelecionada}
            formatarMoeda={formatarMoeda}
            onFecharComanda={fecharComanda}
            onRemoverItem={removerItem}
          />
        </section>
      </section>
    </main>
  );
}
