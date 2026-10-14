import { expect, Page, test } from '@playwright/test';

import { simularApi } from './api-falsa';
import { Esqueleto, faltando, lerEsqueleto, lerEstilos, lerTokens, PARTES } from './esqueleto';
import { TELAS } from './telas';

/**
 * Conformidade de cada tela com o protótipo publicado (docs/design/prototipo,
 * o mesmo conteúdo do GitHub Pages). O protótipo é a referência: para mudar o
 * design, muda-se primeiro o protótipo (PR próprio) e depois a tela.
 *
 * Para cada tela de e2e/conformidade/telas.ts:
 *  1. estrutura: menu, título, botões, filtros, colunas, seções, rótulos e abas
 *     do protótipo precisam existir na tela real (as colunas, na mesma ordem);
 *  2. estilo: tokens do DESIGN.md (variáveis CSS, temas claro e escuro) e estilo
 *     calculado de elementos-chave iguais aos do protótipo;
 *  3. celular: em 390px a tela real não pode ter rolagem horizontal (D20);
 *  4. imagens das duas versões, desktop e celular, anexadas ao relatório para a
 *     avaliação visual (npx playwright show-report e2e/.relatorio).
 *
 * Etiqueta de cada teste: @<etiqueta da issue> (ex.: @issue-21, @base).
 * Rodar uma issue: npm run e2e -- --grep @issue-21
 */
const PROTOTIPO = process.env['PROTOTIPO_URL'] ?? 'http://localhost:4300/';

async function abrir(page: Page, url: string): Promise<void> {
  await page.addInitScript(() => localStorage.setItem('aeria-theme', 'light'));
  const resp = await page.goto(url, { waitUntil: 'networkidle' });
  expect(resp?.ok(), `não abriu ${url}`).toBeTruthy();
}

/** Abre a rota do Angular e confere que ela existe (rota desconhecida cai no redirecionamento padrão). */
async function abrirTela(page: Page, rota: string): Promise<void> {
  await abrir(page, rota);
  expect(new URL(page.url()).pathname, `a rota ${rota} ainda não existe no Angular`).toBe(rota);
}

async function semRolagemHorizontal(page: Page): Promise<number> {
  return page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth);
}

for (const tela of TELAS) {
  test.describe(`${tela.prototipo} -> ${tela.rota}`, { tag: `@${tela.etiqueta}` }, () => {
    let esperado: Esqueleto;
    let tokensProtoClaro: Record<string, string>;
    let tokensProtoEscuro: Record<string, string>;
    let estilosProto: Awaited<ReturnType<typeof lerEstilos>>;

    test.beforeEach(async ({ browser }, info) => {
      const ctx = await browser.newContext({ viewport: { width: 1280, height: 800 } });
      const p = await ctx.newPage();
      await abrir(p, new URL(tela.prototipo, PROTOTIPO).href);
      esperado = await lerEsqueleto(p);
      tokensProtoClaro = await lerTokens(p, 'light');
      tokensProtoEscuro = await lerTokens(p, 'dark');
      estilosProto = await lerEstilos(p);
      await info.attach('protótipo (desktop)', { body: await p.screenshot({ fullPage: true }), contentType: 'image/png' });
      await p.setViewportSize({ width: 390, height: 844 });
      await info.attach('protótipo (celular)', { body: await p.screenshot({ fullPage: true }), contentType: 'image/png' });
      await ctx.close();
    });

    test('estrutura igual à do protótipo', async ({ page }, info) => {
      await simularApi(page, tela.api);
      await abrirTela(page, tela.rota);
      const real = await lerEsqueleto(page);
      await info.attach('tela real (desktop)', { body: await page.screenshot({ fullPage: true }), contentType: 'image/png' });

      for (const { chave, nome } of PARTES) {
        const falta = faltando(esperado[chave], real[chave], tela.ignorar);
        expect.soft(falta, `${nome} do protótipo que não aparecem na tela`).toEqual([]);
        const sobra = faltando(real[chave], esperado[chave]);
        if (sobra.length)
          info.annotations.push({ type: 'a mais na tela', description: `${nome}: ${sobra.join(' | ')}` });
      }
      esperado.colunas.forEach((colunas, i) => {
        expect.soft(real.colunas[i], `colunas da tabela ${i + 1}`).toEqual(colunas);
      });
    });

    test('estilo igual ao do protótipo', async ({ page }) => {
      await simularApi(page, tela.api);
      await abrirTela(page, tela.rota);
      for (const [tema, proto] of [
        ['light', tokensProtoClaro],
        ['dark', tokensProtoEscuro],
      ] as const) {
        const real = await lerTokens(page, tema);
        const diferentes = Object.entries(proto)
          .filter(([nome, valor]) => valor && real[nome] !== valor)
          .map(([nome, valor]) => `${nome}: protótipo ${valor}, tela ${real[nome] || '(não definido)'}`);
        expect.soft(diferentes, `tokens do tema ${tema} diferentes do protótipo`).toEqual([]);
      }
      const estilosReal = await lerEstilos(page);
      for (const [seletor, props] of Object.entries(estilosProto)) {
        if (!props || !estilosReal[seletor]) continue;
        expect.soft(estilosReal[seletor], `estilo de "${seletor}"`).toEqual(props);
      }
    });

    test('abre no celular sem rolagem horizontal (D20)', async ({ page }, info) => {
      await page.setViewportSize({ width: 390, height: 844 });
      await simularApi(page, tela.api);
      await abrirTela(page, tela.rota);
      await info.attach('tela real (celular)', { body: await page.screenshot({ fullPage: true }), contentType: 'image/png' });
      expect(await semRolagemHorizontal(page), 'pixels de rolagem horizontal em 390px').toBeLessThanOrEqual(1);
    });
  });
}
