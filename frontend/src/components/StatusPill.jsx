export default function StatusPill({ status }) {
  const labels = {
    LIVRE: 'Livre',
    OCUPADA: 'Ocupada',
    AGUARDANDO_PAGAMENTO: 'Aguardando pagamento',
    DISPONIVEL: 'Disponível',
    INDISPONIVEL: 'Indisponível',
  };

  const styles = {
    LIVRE: 'bg-emerald-700 text-white',
    OCUPADA: 'bg-[#af3324] text-white',
    AGUARDANDO_PAGAMENTO: 'bg-[#ffd04f] text-[#263126]',
    DISPONIVEL: 'bg-emerald-700 text-white',
    INDISPONIVEL: 'bg-stone-500 text-white',
  };

  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-1 text-xs font-black uppercase tracking-wide ${styles[status] ?? 'bg-stone-700 text-white'}`}>
      {labels[status] ?? status}
    </span>
  );
}
