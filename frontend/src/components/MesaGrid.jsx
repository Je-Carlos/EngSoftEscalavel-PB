import { CircleDollarSign, UsersRound } from 'lucide-react';
import StatusPill from './StatusPill.jsx';

function mesaStatusLabel(status) {
  const labels = {
    LIVRE: 'livre',
    OCUPADA: 'ocupada',
    AGUARDANDO_PAGAMENTO: 'aguardando pagamento',
  };

  return labels[status] ?? status?.toLowerCase() ?? 'sem status';
}

function statusBorder(status) {
  if (status === 'LIVRE') return 'border-l-emerald-700';
  if (status === 'AGUARDANDO_PAGAMENTO') return 'border-l-[#ffd04f]';
  return 'border-l-[#af3324]';
}

export default function MesaGrid({ mesas, comandasAbertas, mesaSelecionadaId, onSelecionarMesa, formatarMoeda }) {
  function comandaDaMesa(mesaId) {
    return comandasAbertas.find((comanda) => comanda.mesa.id === mesaId);
  }

  return (
    <section aria-label="Salão do boteco" className="rounded-lg border border-[#d8bd83] bg-[#fff8e8]/95 p-4 shadow-sm">
      <div className="mb-4 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-black uppercase tracking-[0.14em] text-[#af3324]">Mapa de mesas</p>
          <h2 className="font-display text-3xl text-[#263126]">Salão e comandas em tempo real</h2>
        </div>
        <div className="flex items-center gap-2 rounded-full bg-[#263126] px-3 py-2 text-sm font-bold text-[#fff4dc]">
          <UsersRound aria-hidden="true" size={18} />
          {mesas.length} mesas
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {mesas.map((mesa) => {
          const comanda = comandaDaMesa(mesa.id);
          const total = formatarMoeda(comanda?.total ?? 0);
          const selecionada = mesaSelecionadaId === mesa.id;
          const statusLabel = mesaStatusLabel(mesa.status);
          const ariaLabel = comanda
            ? `Mesa ${mesa.numero} ${statusLabel} ${total}`
            : `Mesa ${mesa.numero} ${statusLabel}`;

          return (
            <button
              aria-label={ariaLabel}
              className={`min-h-36 rounded-lg border border-[#d8bd83] border-l-8 ${statusBorder(mesa.status)} bg-white p-4 text-left text-[#2a241b] shadow-sm transition hover:-translate-y-0.5 hover:border-[#af3324] hover:shadow-md focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#af3324] ${selecionada ? 'ring-4 ring-[#ffd04f]/70' : ''}`}
              key={mesa.id}
              onClick={() => onSelecionarMesa(mesa)}
              type="button"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <span className="text-sm font-black uppercase tracking-wide text-stone-500">Mesa</span>
                  <strong className="block font-display text-5xl leading-none text-[#263126]">{mesa.numero}</strong>
                </div>
                <StatusPill status={mesa.status} />
              </div>
              <div className="mt-5 flex items-end justify-between gap-3">
                <span className="text-sm font-semibold text-stone-600">
                  {comanda ? `Comanda #${comanda.id}` : 'Toque para abrir'}
                </span>
                <span className="inline-flex items-center gap-1 rounded-full bg-[#fff4dc] px-2.5 py-1 font-black text-[#af3324]">
                  <CircleDollarSign aria-hidden="true" size={16} />
                  {comanda ? total : 'Livre'}
                </span>
              </div>
            </button>
          );
        })}
      </div>
    </section>
  );
}
