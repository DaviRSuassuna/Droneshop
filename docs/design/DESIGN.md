# Design system: Aeria Drones

Tokens e componentes do protótipo (`prototipo/assets/site.css`). O frontend Angular usa os mesmos nomes de token em `frontend/src/styles.scss`: mudou aqui, muda lá.

## Visão geral

Referência visual: site da DJI. Fundo quase preto (ou cinza-azulado claro no tema claro), tipografia fina, cards planos com borda de 1px, acento azul só em CTA, badge e item ativo. A ficha técnica é o centro da loja, então a interface sai do caminho: pouca cor, muito espaço, números grandes e leves.

## Temas

| Tema | Como ativa | Uso |
|---|---|---|
| Claro | padrão (`theme.js` grava `aeria-theme` no `localStorage`) | tema inicial do protótipo |
| Escuro | botão "Tema escuro" no canto da tela | alternativa, mesma estrutura |

Os dois temas trocam só valores de token (`:root` e `[data-theme="light"]`). Componente nunca usa cor fixa; sempre `var(--token)`.

## Cores

| Token | Escuro | Claro | Uso |
|---|---|---|---|
| `--bg` | `#0A0A0A` | `#F2F4F8` | fundo da página |
| `--bg2` | `#111114` | `#E6EAF3` | faixa de destaque (hero) |
| `--surface` | `#141416` | `#FFFFFF` | card, tabela, painel |
| `--field` | `#0C0C0E` | `#F4F6FA` | fundo de input |
| `--ink` / `--ink2` | `#F5F5F7` / `#C9C9CE` | `#15171C` / `#31353E` | texto principal / secundário |
| `--dim`, `--mute`, `--faint` | cinzas | cinzas | texto de apoio, em ordem decrescente de contraste |
| `--pure` | `#FFFFFF` | `#15171C` | botão primário, preço, item ativo |
| `--acc` | `#3B7AF0` | `#0B69D4` | acento: badge, contador do carrinho, link de destaque, checkbox marcado |
| `--warn` | `#FF6B4A` | `#B23310` | erro, obrigatório, ação destrutiva |
| `--ok` | `#5FA97F` | `#1C6844` | sucesso, status ativo |
| `--line`, `--b1`…`--b6` | branco com alfa | azul-escuro com alfa | divisórias e bordas, da mais leve à mais forte |
| `--veil` | `#0A0A0A` 93% | `#F8FAFD` 86% | barras fixas com `backdrop-filter: blur` |

Regra: o acento azul não é cor de botão primário. O primário usa `--pure` (branco no escuro, grafite no claro).

## Tipografia

| Papel | Fonte | Exemplo |
|---|---|---|
| Títulos do storefront (`.disp-xl`, `.sec-t`, nome e preço do produto) | Sora 200 a 300 (Google Fonts) | hero 58px, seção 24px, card 15px |
| Texto e admin | Helvetica Neue 300 | corpo 14px/1.5, tabela 12.5 a 13px, título de página 20px |
| Rótulos em caixa alta (cabeçalho de tabela, `.fhead`) | Helvetica Neue 500, 10px, `letter-spacing: .14em` | |
| Código, SKU, chip (`.mono`, `.chip`) | `ui-monospace` 10 a 10.5px | |

Peso nunca passa de 500. Hierarquia vem de tamanho e cor, não de negrito.

## Layout

| Área | Medidas |
|---|---|
| Admin: barra superior (`.tb`) | 52px, fixa |
| Admin: barra da página (`.pagebar`) | fixa abaixo da `.tb`, título + ações |
| Admin: conteúdo (`.pad`) | 26px 28px; coluna lateral (`.rail`) de 320px, fixa |
| Admin: barra de ações (`.actionbar`) | fixa no rodapé dos formulários (Cancelar / Salvar) |
| Storefront: navegação (`.snav`) | 64px, fixa, com `--veil` |
| Storefront: conteúdo (`.swrap`) | 34px nas laterais |
| Espaçamento entre blocos | 18 a 24px |

## Formas

| Elemento | Raio |
|---|---|
| Input, botão, paginação | 5 a 6px |
| Card, tabela, KPI, banner | 10px |
| Pílula, busca, carrinho, tag | 18 a 20px |
| Checkbox | 4px |

Sombra só no tema claro, bem suave, em card, tabela e KPI. No escuro a separação é pela borda.

## Componentes

| Componente | Classe | Regra |
|---|---|---|
| Botão | `.btn` (contorno), `.btn.p` (primário), `.btn.d` (destrutivo), `.btn.sm` | um primário por área de ação |
| Botão do storefront | `.sbtn`, `.sbtn.p` | 46px de altura |
| Campo | `.f` > `label` + `input`/`select`/`textarea` | 38px; obrigatório marcado com `.req` (`*` em `--warn`) |
| Grupo de campos | `.fhead` | rótulo em caixa alta + linha |
| Checkbox | `.ck`, `.perm` | 16px; marcado = fundo `--acc` com ✓ branco, nos dois temas |
| Upload | `.up` | borda tracejada, 104px |
| Tabela | `.tbl` > `.trow.head` + `.trow` | linhas em CSS grid; a linha inteira é link quando abre o registro |
| Status | `.status` + `.dot` | ponto colorido + texto (nunca só cor) |
| Paginação | `.pager` + `.pages` | página atual em `--pure` |
| Abas | `.tabs` | ativa com borda inferior `--pure` |
| Indicador | `.kpi` | número 28px peso 200 + variação em `--ok`/`--warn` |
| Chip / tag | `.chip` (mono, código) / `.tag` (filtro ativo) | |
| Card de produto | `.pcard` (`.media`, `.nm`, `.pr`) + `.badge` | badge de oferta em `--acc` |
| Imagem provisória | `.ph` | listras diagonais; o protótipo não usa foto real |

## Responsivo

O protótipo foi desenhado para desktop; os únicos ajustes são em 1180px (colunas empilham) e 1100px (navegação do storefront quebra linha). Pela D20 do PRD, toda tela também precisa funcionar no navegador do celular (360 a 480px). As versões de celular ainda não estão no protótipo: até existirem, a tela implementada empilha as colunas e mantém os mesmos tokens e componentes.

## Faça / não faça

**Faça:** use token em vez de cor; mantenha o acento raro; alinhe números à direita em tabela; mostre status com texto além da cor; use só dados fictícios.

**Não faça:** negrito acima de 500; sombra no tema escuro; gradiente ou cor de marca em botão primário; foto de produto real ou logotipo de marca real no protótipo.
