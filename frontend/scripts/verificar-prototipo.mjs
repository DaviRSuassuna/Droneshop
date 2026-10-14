// Checagem básica do protótipo navegável (docs/design/prototipo), sem dependências.
//
// Para cada página .html confere:
//   - <html lang>, <title> e <meta name="viewport"> (D20: a tela abre no celular);
//   - links e arquivos locais (href/src) apontam para algo que existe;
//   - toda <img> tem alt (RNF08).
// E confere que o CSS tem regra para telas estreitas (max-width até 480px).
//
// Uso (dentro de frontend/): npm run prototipo:verificar
import { readFileSync, readdirSync, statSync, existsSync } from 'node:fs';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const raiz = resolve(dirname(fileURLToPath(import.meta.url)), '../../docs/design/prototipo');
const erros = [];
const avisos = [];

function paginas(pasta) {
  return readdirSync(pasta).flatMap((nome) => {
    const caminho = join(pasta, nome);
    if (statSync(caminho).isDirectory()) return paginas(caminho);
    return nome.endsWith('.html') ? [caminho] : [];
  });
}

const lista = paginas(raiz);
for (const pagina of lista) {
  const nome = relative(raiz, pagina);
  const html = readFileSync(pagina, 'utf8');
  if (!/<html[^>]*\blang=/i.test(html)) erros.push(`${nome}: <html> sem lang`);
  if (!/<title>[^<]+<\/title>/i.test(html)) erros.push(`${nome}: sem <title>`);
  if (!/<meta[^>]+name=["']viewport["'][^>]*width=device-width/i.test(html))
    erros.push(`${nome}: sem <meta name="viewport" content="width=device-width,...">`);

  for (const [, alvo] of html.matchAll(/\b(?:href|src)=["']([^"'#?]+)[^"']*["']/gi)) {
    if (/^(https?:|mailto:|tel:|data:|javascript:|\/\/)/i.test(alvo)) continue;
    const destino = resolve(dirname(pagina), alvo);
    if (!existsSync(destino)) erros.push(`${nome}: link quebrado -> ${alvo}`);
  }

  for (const [img] of html.matchAll(/<img\b[^>]*>/gi)) {
    if (!/\balt=/i.test(img)) erros.push(`${nome}: <img> sem alt -> ${img.slice(0, 60)}`);
  }
}

const css = readdirSync(join(raiz, 'assets'))
  .filter((n) => n.endsWith('.css'))
  .map((n) => readFileSync(join(raiz, 'assets', n), 'utf8'))
  .join('\n');
const larguras = [...css.matchAll(/@media[^{]*max-width:\s*(\d+)px/gi)].map((m) => Number(m[1]));
if (!larguras.some((l) => l >= 360 && l <= 768))
  avisos.push('nenhuma regra @media (max-width) entre 360 e 768px no CSS: confira o layout no celular (D20)');

for (const a of avisos) console.warn(`aviso: ${a}`);
for (const e of erros) console.error(`erro:  ${e}`);
console.log(`${lista.length} páginas verificadas, ${erros.length} erro(s), ${avisos.length} aviso(s).`);
process.exit(erros.length ? 1 : 0);
