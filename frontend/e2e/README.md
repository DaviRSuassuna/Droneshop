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
| estilo igual ao do protótipo | os tokens do `DESIGN.md` (variáveis CSS, tema claro e escuro) e o estilo calculado de título, botão principal, cabeçalho de tabela, menu e rótulos são iguais |
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
Rodado em 14/10/2026 sobre as telas já implementadas (`@base`). Os estilos (tokens e estilo calculado) batem com o protótipo; a estrutura e o celular não:

| Tela | Diferenças de estrutura |
|---|---|
| Marcas | coluna "Destaque" na tela, "Produtos" no protótipo |
| Cadastrar marca | faltam os botões "Salvar rascunho" e "Publicar marca", a seção "Pendências" e o upload "Logo da marca" |
| Categorias | faltam o botão "Reordenar", o filtro "Categoria" e a coluna "No menu" |
| Cadastrar categoria | faltam os botões "Salvar rascunho" e "Publicar categoria", as seções "Ícone / imagem" e "Prévia da árvore" e os campos "Ícone (URL)" e "Ícone da categoria" |
| Fornecedores | colunas "Cidade/UF" e "Telefone" diferentes do protótipo |
| Cadastrar fornecedor | faltam os campos "Cidade" e "UF" |

Todas as telas do admin já existentes naquela rodada rolavam para o lado no celular (117 a 125px), e o protótipo também não tinha layout para telas estreitas. As telas das issues ainda não implementadas no Angular não foram avaliadas nessa rodada. Promoções (`@issue-14`) foram implementadas depois e passaram nas verificações de estrutura, estilo e celular.
<!-- situacao:fim -->
