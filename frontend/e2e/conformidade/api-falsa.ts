import { Page } from '@playwright/test';

/**
 * API simulada para os testes de conformidade: a tela precisa abrir, não
 * precisa de dados reais. Toda chamada a http://localhost:8080 recebe:
 *   - a resposta específica da tela, se houver (telas.ts, campo api);
 *   - [] nas rotas de enum (/status-*, /tipos-*, /origens-*...);
 *   - PageResponse vazia nas demais leituras;
 *   - 204 em POST, PUT, PATCH e DELETE.
 */
const API = 'http://localhost:8080';
const ENUM =
  /^\/(status|tipos|origens|papeis|modulos|escopos|metodos|modalidades|perfis|provedores|acoes|resolucoes)-/;
const LISTA_SIMPLES =
  /^\/(estados|loja\/categorias|loja\/marcas|conta\/enderecos|conta\/desejos|conta\/cartoes)$/;

export async function simularApi(
  page: Page,
  especificas: Record<string, unknown> = {},
  administrativo = false,
): Promise<void> {
  await page.route(`${API}/**`, async (route) => {
    const req = route.request();
    const caminho = new URL(req.url()).pathname;
    const json = (corpo: unknown) =>
      route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(corpo) });

    if (caminho === '/auth/renovar' && req.method() === 'POST') {
      return json({
        accessToken: 'token-conformidade',
        expiraEm: 3600,
        papeis: [administrativo ? 'ADMINISTRATIVO' : 'CLIENTE'],
      });
    }
    if (caminho in especificas) return json(especificas[caminho]);
    if (req.method() !== 'GET') return route.fulfill({ status: 204 });
    if (ENUM.test(caminho) || LISTA_SIMPLES.test(caminho)) return json([]);
    return json({ items: [], page: 0, pageSize: 10, totalItems: 0, totalPages: 0 });
  });
}
