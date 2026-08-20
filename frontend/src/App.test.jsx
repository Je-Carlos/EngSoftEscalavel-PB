import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { afterEach, expect, test, vi } from 'vitest';
import App from './App.jsx';

const mesas = [
  { id: 1, numero: 1, status: 'LIVRE' },
  { id: 2, numero: 2, status: 'OCUPADA' },
  { id: 3, numero: 3, status: 'AGUARDANDO_PAGAMENTO' },
];

const produtos = [
  {
    id: 10,
    nome: 'Cerveja gelada',
    descricao: 'Antarctica trincando',
    categoria: 'BEBIDA',
    preco: 8.5,
    disponivel: true,
  },
  {
    id: 11,
    nome: 'Torresmo',
    descricao: 'Pururucado na hora',
    categoria: 'PORCAO',
    preco: 26,
    disponivel: true,
  },
  {
    id: 12,
    nome: 'Cachaça 51',
    descricao: 'Dose',
    categoria: 'BEBIDA',
    preco: 6,
    disponivel: false,
  },
  {
    id: 13,
    nome: 'Bolinho de bacalhau',
    descricao: 'Porção da casa',
    categoria: 'PORCAO',
    preco: 18,
    disponivel: true,
  },
];

const comandas = [
  {
    id: 20,
    mesa: { id: 2, numero: 2, status: 'OCUPADA' },
    status: 'ABERTA',
    itens: [{ id: 30, produtoId: 10, produtoNome: 'Cerveja gelada', quantidade: 2, subtotal: 17 }],
    total: 17,
  },
  {
    id: 21,
    mesa: { id: 3, numero: 3, status: 'AGUARDANDO_PAGAMENTO' },
    status: 'ABERTA',
    itens: [],
    total: 0,
  },
];

const estoques = [
  { produtoId: 10, quantidade: 8 },
  { produtoId: 11, quantidade: 5 },
  { produtoId: 12, quantidade: 4 },
  { produtoId: 13, quantidade: 0 },
];

function mockApi() {
  return vi.spyOn(globalThis, 'fetch').mockImplementation(async (url, options = {}) => {
    const path = String(url).replace('http://localhost:8080/api', '');

    if (path === '/mesas') {
      return Response.json(mesas);
    }

    if (path === '/produtos') {
      return Response.json(produtos);
    }

    if (path === '/comandas') {
      return Response.json(comandas);
    }

    if (path === '/estoques') {
      return Response.json(estoques);
    }

    if (path === '/comandas/abrir') {
      return Response.json({
        id: 22,
        mesa: { id: 1, numero: 1, status: 'OCUPADA' },
        status: 'ABERTA',
        itens: [],
        total: 0,
      });
    }

    if (path === '/comandas/20/itens' && options.method === 'POST') {
      return Response.json({
        ...comandas[0],
        itens: [
          ...comandas[0].itens,
          { id: 31, produtoId: 11, produtoNome: 'Torresmo', quantidade: 1, subtotal: 26 },
        ],
        total: 43,
      });
    }

    if (path === '/comandas/20/itens/30' && options.method === 'DELETE') {
      return Response.json({ ...comandas[0], itens: [], total: 0 });
    }

    if (path === '/comandas/20/fechar' && options.method === 'PATCH') {
      return Response.json({ ...comandas[0], status: 'FECHADA' });
    }

    if (path === '/produtos/10' && options.method === 'PUT') {
      return Response.json({ ...produtos[0], nome: 'Cerveja premium', preco: 9.9 });
    }

    if (path === '/produtos' && options.method === 'POST') {
      return Response.json({ id: 13, nome: 'Pastel de queijo' });
    }

    return Response.json({});
  });
}

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

test('mostra o salão como tela principal com identidade de boteco claro', async () => {
  mockApi();

  render(<App />);

  expect(await screen.findByRole('heading', { level: 1, name: 'Pé Sujo' })).toBeInTheDocument();
  expect(screen.getByRole('heading', { level: 2, name: /Salão e comandas em tempo real/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Mesa 1 livre/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Mesa 2 ocupada R\$ 17,00/i })).toBeInTheDocument();
  expect(screen.getByRole('heading', { level: 2, name: 'Comanda #20' })).toBeInTheDocument();
  expect(screen.getByLabelText(/Item Cerveja gelada/i)).toBeInTheDocument();
});

test('abre mesa livre e seleciona a nova comanda', async () => {
  const fetchMock = mockApi();

  render(<App />);

  fireEvent.click(await screen.findByRole('button', { name: /Mesa 1 livre/i }));

  await waitFor(() => {
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/comandas/abrir',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ mesaId: 1 }),
      }),
    );
  });

  expect(screen.getByText(/Comanda 22 aberta para a mesa 1/i)).toBeInTheDocument();
});

test('adiciona, remove e fecha itens pela comanda lateral', async () => {
  const fetchMock = mockApi();

  render(<App />);

  await screen.findByRole('heading', { level: 2, name: 'Comanda #20' });

  fireEvent.change(screen.getByLabelText(/Buscar no cardápio/i), { target: { value: 'torresmo' } });
  fireEvent.click(screen.getByRole('button', { name: /Adicionar Torresmo/i }));

  await waitFor(() => {
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/comandas/20/itens',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({ produtoId: 11, quantidade: 1 }),
      }),
    );
  });

  const item = screen.getByLabelText(/Item Cerveja gelada/i);
  fireEvent.click(within(item).getByRole('button', { name: /Remover Cerveja gelada/i }));

  await waitFor(() => {
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/comandas/20/itens/30',
      expect.objectContaining({ method: 'DELETE' }),
    );
  });

  fireEvent.click(screen.getByRole('button', { name: /Fechar conta da mesa 2/i }));

  await waitFor(() => {
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/comandas/20/fechar',
      expect.objectContaining({ method: 'PATCH' }),
    );
  });
});

test('mantem o cadastro de produto como fluxo secundario', async () => {
  const fetchMock = mockApi();

  render(<App />);

  fireEvent.click(await screen.findByRole('button', { name: /Gerenciar cardápio/i }));
  fireEvent.change(screen.getByLabelText(/Nome do novo produto/i), { target: { value: 'Pastel de queijo' } });
  fireEvent.change(screen.getByLabelText(/Descrição do novo produto/i), { target: { value: 'Massa sequinha' } });
  fireEvent.change(screen.getByLabelText(/Preço do novo produto/i), { target: { value: '9.50' } });
  fireEvent.click(screen.getByRole('button', { name: /Cadastrar produto/i }));

  await waitFor(() => {
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/produtos',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({
          nome: 'Pastel de queijo',
          descricao: 'Massa sequinha',
          categoria: 'BEBIDA',
          preco: 9.5,
          disponivel: true,
        }),
      }),
    );
  });
});

test('não permite lançar produto sem saldo no estoque', async () => {
  mockApi();

  render(<App />);

  await screen.findByRole('heading', { level: 2, name: 'Comanda #20' });
  fireEvent.change(screen.getByLabelText(/Buscar no cardápio/i), { target: { value: 'bolinho' } });

  expect(screen.queryByRole('button', { name: /Adicionar Bolinho de bacalhau/i })).not.toBeInTheDocument();
});
