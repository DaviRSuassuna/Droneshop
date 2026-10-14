// Serve o protótipo (docs/design/prototipo) em http://localhost:4300, sem dependências.
// Uso (dentro de frontend/): npm run prototipo   (porta: PORTA=5000 npm run prototipo)
import { createServer } from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import { dirname, extname, join, normalize, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const raiz = resolve(dirname(fileURLToPath(import.meta.url)), '../../docs/design/prototipo');
const porta = Number(process.env.PORTA ?? 4300);
const tipos = {
  '.html': 'text/html; charset=utf-8', '.css': 'text/css', '.js': 'text/javascript',
  '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg', '.webp': 'image/webp',
};

createServer(async (req, res) => {
  const caminho = decodeURIComponent(new URL(req.url, 'http://x').pathname);
  let arquivo = normalize(join(raiz, caminho));
  if (!arquivo.startsWith(raiz)) return res.writeHead(403).end();
  try {
    if ((await stat(arquivo)).isDirectory()) arquivo = join(arquivo, 'index.html');
    res.writeHead(200, { 'content-type': tipos[extname(arquivo)] ?? 'application/octet-stream' });
    res.end(await readFile(arquivo));
  } catch {
    res.writeHead(404).end('não encontrado');
  }
}).listen(porta, () => {
  console.log(`Protótipo em http://localhost:${porta}/  (admin: http://localhost:${porta}/admin/)`);
  console.log('Para ver como no celular: DevTools > modo responsivo, largura 360 a 480px.');
});
