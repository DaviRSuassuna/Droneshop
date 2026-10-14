import { Page } from '@playwright/test';

/**
 * "Esqueleto" de uma tela: os textos que definem a estrutura dela, lidos pelas
 * mesmas classes CSS no protótipo e no Angular (o Angular reaproveita as classes
 * do site.css, ver docs/design/DESIGN.md).
 *
 * Texto com número, "R$" ou mais de 60 caracteres é tratado como dado de
 * exemplo e fica de fora (nome de produto, preço, contagem...).
 */
export interface Esqueleto {
  navegacao: string[];
  titulo: string[];
  acoes: string[];
  filtros: string[];
  colunas: string[][];
  secoes: string[];
  rotulos: string[];
  abas: string[];
}

export const PARTES: { chave: Exclude<keyof Esqueleto, 'colunas'>; nome: string }[] = [
  { chave: 'navegacao', nome: 'menu' },
  { chave: 'titulo', nome: 'título da página' },
  { chave: 'acoes', nome: 'botões' },
  { chave: 'filtros', nome: 'filtros' },
  { chave: 'secoes', nome: 'títulos de seção' },
  { chave: 'rotulos', nome: 'rótulos de campo' },
  { chave: 'abas', nome: 'abas' },
];

export async function lerEsqueleto(page: Page): Promise<Esqueleto> {
  return page.evaluate(() => {
    const limpar = (s: string | null | undefined) =>
      (s ?? '').replace(/\*/g, '').replace(/[↗→←▾▸]/g, '').replace(/\s+/g, ' ').trim();
    const ehDado = (s: string) => !s || /\d|R\$/.test(s) || s.length > 60;
    const fora = (el: Element) => !!el.closest('.theme-t, [hidden], script, template');
    const textos = (seletor: string, transformar: (el: Element) => string = (el) => el.textContent ?? '') => {
      const vistos = new Set<string>();
      for (const el of Array.from(document.querySelectorAll(seletor))) {
        if (fora(el)) continue;
        const t = limpar(transformar(el));
        if (!ehDado(t)) vistos.add(t);
      }
      return [...vistos];
    };
    // rótulo sem o texto dos controles que estão dentro dele
    const textoProprio = (el: Element) =>
      Array.from(el.childNodes)
        .filter((n) => n.nodeType === Node.TEXT_NODE || (n as Element).tagName === 'SPAN')
        .map((n) => n.textContent)
        .join(' ');
    // item de menu sem o contador (ex.: "Carrinho 2")
    const semContador = (el: Element) => (el.textContent ?? '').replace(/\d+/g, '');

    return {
      navegacao: textos('.tb-nav a, .tb-nav span, .snav-nav a, .snav-r a, .acct-n a', semContador),
      titulo: textos('.pagebar .h1, .pagebar h1'),
      acoes: textos('.pagebar .actions a, .pagebar .actions button, .actions .btn, .actions .sbtn, button.btn, a.btn, .sbtn'),
      filtros: [
        ...textos('.filters input[placeholder]', (el) => el.getAttribute('placeholder') ?? ''),
        ...textos('.filters select', (el) => (el as HTMLSelectElement).options[0]?.text ?? ''),
      ],
      colunas: Array.from(document.querySelectorAll('.trow.head'))
        .filter((el) => !fora(el))
        .map((el) => Array.from(el.children).map((c) => limpar(c.textContent)).filter((t) => t && !/\d/.test(t))),
      secoes: textos('.card-t, .eyebrow, .sec-t, .scard-t, .pad-t h2, h2, h3'),
      rotulos: textos('label', textoProprio),
      abas: textos('.tabs a, .tabs button, .tabs > span'),
    };
  });
}

/** Variáveis CSS do :root (tokens do DESIGN.md) no tema informado. */
export async function lerTokens(page: Page, tema: 'light' | 'dark'): Promise<Record<string, string>> {
  return page.evaluate((t) => {
    const raiz = document.documentElement;
    const anterior = raiz.getAttribute('data-theme');
    raiz.setAttribute('data-theme', t);
    const estilo = getComputedStyle(raiz);
    const nomes = new Set<string>();
    for (const folha of Array.from(document.styleSheets)) {
      let regras: CSSRuleList;
      try {
        regras = folha.cssRules;
      } catch {
        continue;
      }
      for (const r of Array.from(regras)) {
        const st = (r as CSSStyleRule).style;
        if (!st) continue;
        for (let i = 0; i < st.length; i++) if (st[i].startsWith('--')) nomes.add(st[i]);
      }
    }
    const saida: Record<string, string> = {};
    // mesma escrita para o mesmo valor: ".5" = "0.50" = "0.5"
    const normalizar = (v: string) =>
      v
        .replace(/\s+/g, '')
        .toLowerCase()
        .replace(/(^|[^\d])\.(\d)/g, '$10.$2')
        .replace(/(\d+\.\d*?)0+(?=\D|$)/g, '$1')
        .replace(/\.(?=\D|$)/g, '');
    for (const n of nomes) saida[n] = normalizar(estilo.getPropertyValue(n));
    if (anterior) raiz.setAttribute('data-theme', anterior);
    else raiz.removeAttribute('data-theme');
    return saida;
  }, tema);
}

/** Estilo calculado de elementos-chave, quando existem. */
export async function lerEstilos(page: Page): Promise<Record<string, Record<string, string> | null>> {
  return page.evaluate(() => {
    const alvo: Record<string, string[]> = {
      body: ['font-family', 'font-size', 'background-color', 'color'],
      '.pagebar .h1': ['font-family', 'font-size', 'font-weight', 'color'],
      '.btn.p': ['background-color', 'color', 'border-radius', 'font-size'],
      '.trow.head': ['font-size', 'text-transform', 'color'],
      '.tb-nav a': ['font-size', 'color'],
      '.sbtn.p': ['background-color', 'color', 'border-radius'],
      'label': ['font-size', 'color'],
    };
    const saida: Record<string, Record<string, string> | null> = {};
    for (const [sel, props] of Object.entries(alvo)) {
      const el = Array.from(document.querySelectorAll(sel)).find((e) => !e.closest('.theme-t'));
      if (!el) {
        saida[sel] = null;
        continue;
      }
      const cs = getComputedStyle(el);
      saida[sel] = Object.fromEntries(props.map((p) => [p, cs.getPropertyValue(p).replace(/\s+/g, ' ').trim()]));
    }
    return saida;
  });
}

/** Itens do protótipo que faltam na tela real (comparação sem diferenciar maiúsculas). */
export function faltando(prototipo: string[], real: string[], ignorar: string[] = []): string[] {
  const norm = (s: string) => s.toLocaleLowerCase('pt-BR');
  const temReal = new Set(real.map(norm));
  const ign = new Set(ignorar.map(norm));
  return prototipo.filter((t) => !temReal.has(norm(t)) && !ign.has(norm(t)));
}
