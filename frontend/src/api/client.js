const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

export async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (response.status === 204) {
    return null;
  }

  const body = await response.json().catch(() => null);

  if (!response.ok) {
    const detail = body?.detalhes?.[0] ?? body?.erro ?? 'Falha ao comunicar com a API';
    throw new Error(detail);
  }

  return body;
}
