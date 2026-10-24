# Conformidade das telas com o protótipo

O protótipo (`docs/design/prototipo`, publicado no GitHub Pages) é a referência visual do projeto (D38). Estes testes comparam cada tela do Angular com a tela correspondente do protótipo e são a avaliação de "está de acordo com o design".

## Como rodar

```bash
cd frontend
npx playwright install chromium          # só na primeira vez
npm run e2e                              # todas as telas
npm run e2e -- --grep @issue-21          # só as telas de uma issue
npm run e2e:relatorio                    # abre o relatório com as imagens lado a lado
```

O Playwright sobe o `ng serve` (4200) e o protótipo (4300) sozinho; a API é simulada (`conformidade/api-falsa.ts`), não precisa do backend. Para comparar com o que já está publicado no Pages em vez da pasta local:

```bash
PROTOTIPO_URL=https://davirsuassuna.github.io/droneshop/prototipo/ npm run e2e
```

## O que cada tela verifica

| Teste | Passa quando |
|---|---|
| estrutura igual à do protótipo | menu, título, botões, filtros, títulos de seção, rótulos e abas do protótipo existem na tela; as colunas das tabelas são as mesmas, na mesma ordem. Texto com número ou "R$" é dado de exemplo e não conta. O que a tela tem a mais vira anotação no relatório |
| estilo igual ao do protótipo | os tokens do `DESIGN.md` (variáveis CSS, tema único escuro) e o estilo calculado de título, botão principal, cabeçalho de tabela, menu e rótulos são iguais |
| abre no celular sem rolagem horizontal | em 390px de largura a página não rola para o lado (D20) |

Cada teste anexa ao relatório as imagens do protótipo e da tela, no desktop e no celular, para a avaliação visual.

## Arquivos

| Arquivo | Para quê |
|---|---|
| `conformidade/telas.ts` | Mapa tela do protótipo -> rota do Angular -> issue. É o contrato das rotas do frontend |
| `conformidade/esqueleto.ts` | Lê a estrutura, os tokens e os estilos de uma página (as mesmas classes CSS nas duas) |
| `conformidade/api-falsa.ts` | Respostas simuladas da API |
| `conformidade/conformidade.spec.ts` | Os testes |
| `telas-entregues.txt` | Etiquetas que bloqueiam o merge no CI |

No CI, todo PR roda as telas de `telas-entregues.txt` (bloqueia) e as telas da issue da branch, descoberta pelo número no começo do nome (`feat/10-...` ou `10-...` roda `@issue-10`; informativo até a etiqueta entrar na lista). Todas as telas só rodam quando pedidas à mão (Actions > frontend > Run workflow > "todas").

## Fluxo para mudar o design

1. PR que altera o protótipo em `docs/design/prototipo` (e o `DESIGN.md`, se mudou token). Se a tela já está em `telas-entregues.txt`, o mesmo PR tira a etiqueta dela de lá; senão o CI bloqueia, porque a tela real deixou de bater.
2. Depois do merge, o Pages publica o protótipo novo.
3. PR que muda a tela real até os testes da tela passarem, devolvendo a etiqueta ao `telas-entregues.txt`.

## Situação atual

<!-- situacao:inicio -->
Atualização de 23/10/2026 (issue #80): as oito telas base — listagem e cadastro de marcas,
categorias, subcategorias e fornecedores — passaram nas 24 verificações de estrutura,
estilo e celular (390px). A etiqueta `base` voltou a `telas-entregues.txt`.

O logo da marca continua por URL, conforme autorização do mantenedor; upload fica para outro bloco.
Subcategoria em rascunho aceita campos vazios conforme D21, inclusive `categoria: null`.
Foram verificados pelo MCP os fluxos de criação, edição, rascunho e publicação com API real em
banco descartável. No fornecedor, só a consulta externa de CEP foi simulada no navegador.
As cinco telas deste bloco foram conferidas em 360, 390 e 480px sem rolagem horizontal e
sem violações axe WCAG A/AA, incluindo o estado de erro de publicação da subcategoria.

Produtos continua com seleção e ações em lote; a seleção usa o checkbox da linha no celular.
Os seletores usam `idStatus` com a constante do enum, sem um segundo filtro para produtos ativos.
Configuração da loja também passou nos 3 testes de conformidade e a etiqueta `issue-6` foi ativada.
Os componentes de mídia usam o visual novo: foram verificados upload por teclado, imagem principal,
galeria/ordem, troca da principal, vídeo, PDF, exclusão e fila de produto novo com API real.
Configuração e cadastros de drone/acessório/kit passaram em 12 verificações deste bloco.
Axe WCAG A/AA sem violações e sem rolagem horizontal em 360/390/480px, inclusive com mídias preenchidas.
Login e listagem/cadastro de usuários passaram nas 9 verificações de conformidade (24/10/2026).
As etiquetas `issue-4` e `issue-5` foram reativadas. A comparação de estilos usa elementos visíveis
nas duas versões, evitando o botão da busca móvel oculto no protótipo.
Convite, 2FA, recuperação de senha, cadastro de clientes e busca da loja não foram antecipados.
Listagens de promoções e cupons migradas; lista + cadastro passaram em 12 verificações de conformidade
com dados preenchidos nas listas (24/10/2026). Etiquetas `issue-14` e `issue-15` reativadas.
Exportar usos fica em cada cupom, conforme autorização do mantenedor, usando o endpoint existente
com leitura de todas as páginas; falha não gera arquivo parcial. As listas preservam a carga atual
(até 1000 registros e filtros locais). Testes MCP verificaram 1280/360/390/480px e axe WCAG A/AA.
Pedidos (listagem/detalhe) e dashboard também foram migrados. `_legado.scss` e sua importação
foram removidos; não restam hosts `.legado` nem tokens CSS sem definição em `frontend/src`.
A regressão após essa remoção passou nos 81 casos das 27 telas implementadas (36 base/acesso/configuração,
18 catálogo, 27 estoque/promoções/cupons/pedidos/dashboard), reativando também `issue-8`, `issue-80`,
`issue-13`, `issue-16` e `issue-18`.
No MCP, pedidos e dashboard foram conferidos em 1280/360/390/480px sem overflow ou violações axe WCAG A/AA.
CSV com filtros, etiqueta PDF A5, busca/paginação e período usam a API real do banco descartável;
ações de status e cenários de falha foram verificados com respostas simuladas, sem alterar pedidos da prévia.

A API atual não calcula série diária/comparação, conversão, ranking ou relatório do dashboard.
Essas partes continuam fora da tela, registradas em `ignorar`; a tabela futura de ranking tem uma
exceção de colunas exatas e motivo em `tabelasSemApi`. As demais tabelas continuam sendo comparadas.
Em pedidos, as contagens dos cartões usam o resumo global por status. Busca é por número/nome,
etiqueta é individual e dados financeiros/logísticos futuros não foram antecipados.
<!-- situacao:fim -->
