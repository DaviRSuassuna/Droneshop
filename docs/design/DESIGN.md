# Design system: Aeria Drones

Tokens e componentes do protótipo (`prototipo/assets/site.css` para a loja e `prototipo/assets/admin.css` para o painel). O frontend Angular usa os mesmos nomes de token em `frontend/src/styles.scss`: mudou aqui, muda lá.

## Visão geral

Direção "Noite + Campo": fundo grafite, letra fina e o drone como protagonista. A loja empresta a linguagem do controle de voo: grade fina, cruzes de mira, régua de telemetria com números grandes e leves, desenhos técnicos de linha. O âmbar aparece pouco e só onde pede atenção. A ficha técnica continua sendo o centro: números grandes, rótulos em mono, nada de enfeite.

Referências estudadas: DJI (hero de estúdio, blocos de produto), DJI Agriculture (drone grande cortado pela borda), Autel (vitrine em cards), Skydio (menu flutuante, seção escura da família), GTEEX (barra de aviso, cotação para empresas).

## Tema

Um tema só, escuro. O tema claro e o `theme.js` saíram junto com o protótipo antigo.

## Cores

| Token | Valor | Uso |
|---|---|---|
| `--bg` | `#0E0F11` | fundo da página |
| `--bg2` | `#141619` (loja) · `#121417` (painel) | painéis, cards de lista, faixas |
| `--surf` / `--surf2` | `#1A1C20` / `#202328` | superfície elevada, item ativo, trilho de barra |
| `--field` | `#0B0C0E` | fundo de input |
| `--ink` / `--ink2` | `#F1F2F4` / `#C3C7CD` | texto principal / secundário |
| `--mute` / `--faint` | `#8A9099` / `#5C626B` | texto de apoio / marcas discretas |
| `--line` / `--line2` | branco 8% / 16% | divisórias e bordas |
| `--amber` | `#E8A86B` | acento: Ofertas, preço em oferta, chip de lançamento, item ativo, foco, primeiro número da régua |
| `--ok` | `#7CC49A` | sucesso, status ativo, desconto aplicado |
| `--warn` | `#F08A6C` | erro, estoque baixo, ação destrutiva |
| `--info` | `#8FB4F0` | status em andamento (agendado, em separação, enviado) |

Regra: o âmbar não é cor de botão. O botão primário é sólido em `--ink` com texto em `--bg`.

## Tipografia

| Papel | Fonte | Exemplo |
|---|---|---|
| Títulos, preços e números da ficha | Sora 200 a 300 (Google Fonts) | hero 92px, página 58px, seção 42px, número da régua 44 a 48px |
| Texto e interface | DM Sans 400 e 500 | corpo 15px na loja, 14px no painel |
| Rótulos em caixa alta, SKU, código, unidade | IBM Plex Mono 400 e 500 | rótulo 10.5px com `letter-spacing: .16em`; unidade ao lado do número |

Peso nunca passa de 500. Hierarquia vem de tamanho e cor, não de negrito.

## Layout

| Área | Medidas |
|---|---|
| Loja: menu (`.navwrap` > `.nav`) | 56px, flutuante e fixo, cantos de 14px, fundo com `backdrop-filter` |
| Loja: conteúdo (`.wrap`) | largura máxima 1320px, margem lateral mínima de 16px |
| Loja: régua de telemetria (`.tele`) | 4 leituras com divisória; vira 2×2 no celular |
| Painel: barra superior (`.tb`) | 56px, fixa, abas de módulo |
| Painel: formulário (`.form`) | coluna principal + coluna lateral de 330px (publicação, prévia, pendências) |
| Painel: barra de ações (`.actionbar`) | fixa no rodapé dos formulários |
| Espaço entre blocos | 12 a 14px em grade; 70 a 88px entre seções da loja |

## Formas

| Elemento | Raio |
|---|---|
| Botão, input, select | 8 a 9px |
| Card, painel, tabela | 12 a 16px |
| Menu flutuante, palco do produto | 14 a 18px |
| Chip, tag, botão de quantidade de página | 999px |

Sem sombra de caixa. A separação é por borda e tom de fundo; o drone recortado usa `drop-shadow`.

## Componentes

| Componente | Classe | Regra |
|---|---|---|
| Botão | `.btn`, `.btn.p` (primário), `.btn.sm`, `.btn.sq` (só ícone), `.btn.d` (destrutivo, painel) | um primário por área de ação |
| Chip | `.chip` (âmbar), `.chip.n` (neutro) | mono, caixa alta |
| Rótulo | `.lbl` | mono 10.5px, caixa alta |
| Card de produto | `.pcard` > `.top`, `.stage`, `h3`, `.specs`, `.pr` | preço em oferta em `--amber` com o antigo riscado |
| Palco | `.stage` | foto recortada ou desenho sobre gradiente radial grafite |
| Desenho de linha | `.pic-art` (loja), `.art` (seção de uso e Empresas) | traço 1.2px, cinza; detalhe em âmbar |
| Régua de telemetria | `.tele` > `.read` | rótulo mono + número Sora 200 + unidade mono |
| Filtros | `.filters` > `.fgroup` > `.opt` + `.ck` | contagem em mono à direita; no celular abrem por botão |
| Opção | `.choice` (rádio em card), `.rad` (rádio em pílula, painel) | selecionado com borda âmbar |
| Status | `.st.ok`, `.st.warn`, `.st.amber`, `.st.info`, `.st.mute` | ponto colorido + texto, nunca só cor |
| Linha do tempo | `.tl` > `li.done`, `li.now`, `li.todo` | pedido, rastreio, próximos passos |
| Indicador (painel) | `.kpis` > `.kpi` | número Sora 200, variação em `--ok`/`--warn`, minigráfico |
| Nota de regra | `div.info` (painel) | explica a regra de negócio da tela (D18, D24, append-only...) |
| Pendências | `.card.pend` | campos obrigatórios que faltam, na coluna lateral |

## Imagens

Só renders fictícios (`prototipo/assets/img/*.webp`, fundo transparente) e desenhos de linha. Nada de foto de produto real ou logotipo de marca real.

## Responsivo

Toda tela foi desenhada também para 390px (D20). Pontos de quebra: 1100px (menu vira botão, grades caem para 2 colunas, coluna lateral desce), 640px (grades de 1 coluna, formulários em 2 colunas) e 480px. Em 390px nenhuma tela rola para o lado.

## Faça / não faça

**Faça:** use token em vez de cor; mantenha o âmbar raro; número grande e leve com unidade em mono; status com texto além da cor; só dados fictícios.

**Não faça:** negrito acima de 500; sombra de caixa; gradiente ou âmbar em botão primário; foto de produto real; desenho de drone feito em CSS.
