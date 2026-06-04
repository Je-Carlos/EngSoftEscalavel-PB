import { Banknote, ReceiptText, Trash2 } from 'lucide-react';

export default function ComandaPanel({ comanda, onFecharComanda, onRemoverItem, formatarMoeda }) {
  if (!comanda) {
    return (
      <aside className="rounded-lg border border-dashed border-[#d8bd83] bg-[#fff8e8] p-5 text-[#263126]">
        <p className="text-xs font-black uppercase tracking-[0.14em] text-[#af3324]">Comanda</p>
        <h2 className="font-display text-3xl">Selecione uma mesa</h2>
        <p className="mt-2 text-sm font-semibold text-stone-600">
          Toque em uma mesa livre para abrir comanda ou escolha uma mesa ocupada para lançar consumo.
        </p>
      </aside>
    );
  }

  const possuiItens = comanda.itens.length > 0;

  return (
    <aside className="sticky top-4 rounded-lg border border-[#263126] bg-[#263126] p-4 text-[#fff4dc] shadow-xl">
      <div className="mb-4 flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.14em] text-[#ffd04f]">Mesa {comanda.mesa.numero}</p>
          <h2 className="font-display text-4xl">Comanda #{comanda.id}</h2>
        </div>
        <ReceiptText aria-hidden="true" className="text-[#ffd04f]" size={30} />
      </div>

      <div className="grid gap-2">
        {comanda.itens.map((item) => (
          <div
            aria-label={`Item ${item.produtoNome}`}
            className="rounded-lg border border-white/10 bg-white/10 p-3"
            key={item.id}
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <strong className="block text-white">{item.produtoNome}</strong>
                <span className="text-sm text-[#f7d99a]">{item.quantidade}x · {formatarMoeda(item.subtotal)}</span>
              </div>
              <button
                aria-label={`Remover ${item.produtoNome}`}
                className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-white/15 bg-white/10 text-[#ffd04f] transition hover:border-[#ffd04f] hover:bg-[#af3324] hover:text-white"
                onClick={() => onRemoverItem(comanda.id, item.id)}
                title={`Remover ${item.produtoNome}`}
                type="button"
              >
                <Trash2 aria-hidden="true" size={17} />
              </button>
            </div>
          </div>
        ))}

        {!possuiItens && (
          <p className="rounded-lg border border-dashed border-white/20 bg-white/10 p-3 text-sm font-semibold text-[#f7d99a]">
            Comanda vazia. Adicione pelo cardápio rápido.
          </p>
        )}
      </div>

      <div className="mt-5 rounded-lg bg-[#fff4dc] p-4 text-[#263126]">
        <span className="flex items-center gap-2 text-sm font-black uppercase tracking-wide text-[#af3324]">
          <Banknote aria-hidden="true" size={18} />
          Total da conta
        </span>
        <strong className="mt-1 block font-display text-4xl">{formatarMoeda(comanda.total)}</strong>
      </div>

      <button
        aria-label={`Fechar conta da mesa ${comanda.mesa.numero}`}
        className="mt-3 w-full rounded-lg bg-[#ffd04f] px-4 py-3 text-base font-black text-[#263126] shadow-sm transition hover:-translate-y-0.5 hover:bg-[#ffe083] disabled:cursor-not-allowed disabled:opacity-60"
        disabled={!possuiItens}
        onClick={() => onFecharComanda(comanda.id)}
        type="button"
      >
        Fechar conta da mesa {comanda.mesa.numero}
      </button>
    </aside>
  );
}
