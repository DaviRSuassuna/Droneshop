# Trabalho em paralelo: um supervisor e duas trilhas

O backlog está dividido em duas **trilhas** que andam ao mesmo tempo, cada uma com um agente executor no VS Code. Um terceiro agente, o **supervisor**, sincroniza as duas. A divisão é por **módulo**: cada trilha é dona de módulos inteiros, então os dois executores quase nunca mexem no mesmo arquivo.

## Papéis

| Papel | Onde roda | Faz | Não faz |
|---|---|---|---|
| **Supervisor** | Claude no app desktop, com a pasta `D:\Projetos\droneshop` conectada | Mantém o quadro (`.agentes/QUADRO.md`), libera a próxima issue de cada trilha, confere portões, acompanha os PRs abertos (CI, conflitos, limite de módulo), sugere a ordem de merge, resolve conflito nos arquivos compartilhados e numera decisões novas no PRD | Não implementa issue; não revisa nem aprova PR no GitHub; não faz merge |
| **Executor A** | Claude Code no VS Code, na pasta `.worktrees/a` | Implementa as issues da trilha A, na ordem; faz commit, push da branch e abre o PR | Não mexe em módulo da trilha B; não numera decisão no PRD; não revisa nem aprova PR |
| **Executor B** | Claude Code no VS Code, na pasta `.worktrees/b` | Implementa as issues da trilha B, na ordem; faz commit, push da branch e abre o PR | Não mexe em módulo da trilha A; não numera decisão no PRD; não revisa nem aprova PR |
| **Davi** | GitHub e terminal | Aprova o plano de cada issue, revisa e aprova os PRs, faz o merge, roda a sincronização das issues | |

Os prompts para iniciar cada agente estão em [`PROMPTS.md`](PROMPTS.md).

## Quem é dono de quê

| | Trilha A: cliente, compra e pós-venda | Trilha B: catálogo, vitrine, admin e atendimento |
|---|---|---|
| Módulos do backend (`br.com.droneshop.*`) | `identidade`, `compra`, `pagamento`, `pedido`, `estoque`, `devolucao` | `catalogo`, `administracao`, `atendimento`, `notificacao` |
| Telas da loja | `storefront/auth`, `storefront/compra`, `storefront/conta` | `storefront/catalogo`, `storefront/home`, `storefront/suporte`, `storefront/empresas` |
| Telas do admin | `admin/pedidos`, `admin/estoque`, `admin/usuarios` | todas as outras de `admin/` |
| Cascas e estilos | | `layout/storefront`, `layout/admin`, `frontend/src/styles`, `shared/directives` |
| Diagramas | `aeria-identidade`, `aeria-compra`, `aeria-pagamento`, `aeria-pedido`, `aeria-estoque` | `aeria-catalogo`, `aeria-administrativo`, `aeria-atendimento` |
| Carga de dados | `backend/src/main/resources/import-a.sql` | `backend/src/main/resources/import-b.sql` |
| Protótipo (`docs/design`) | | trilha B |

Precisa mexer em algo do dono da outra trilha? Não mexa: registre no quadro (seção da sua trilha, "Pedido para a outra trilha") e o supervisor decide.

## Ordem das trilhas

A ordem respeita a seção "Depende de" de cada issue. **Portão** é a única dependência entre trilhas: a issue só começa (ou, quando indicado, só ganha a etiqueta de tela) depois que a issue da outra trilha estiver na `develop`. Se o portão não estiver aberto, o supervisor troca a issue com a próxima.

### Trilha A

| # | Issue | Módulo | Título | Portão |
|---|---|---|---|---|
| 1 | #19 | identidade | [PerfilCliente e endereços do cliente](../issues/019.md) | casca da loja (só a etiqueta da tela) |
| 2 | #20 | identidade | [SessaoUsuario: escolha de perfil no login e revogação](../issues/020.md) | |
| 3 | #22 | compra | [Lista de desejos](../issues/022.md) | |
| 4 | #24 | compra | [Carrinho anônimo por token e vínculo no login](../issues/024.md) | #21 |
| 5 | #23 | estoque | [Reserva de estoque no checkout com expiração](../issues/023.md) | |
| 6 | #25 | compra | [Aplicar cupom no carrinho](../issues/025.md) | |
| 7 | #30 | pedido | [Frete híbrido: agregador + RegraFrete de fallback](../issues/030.md) | |
| 8 | #26 | compra | [Checkout: endereço, entrega, pagamento e confirmação](../issues/026.md) | |
| 9 | #27 | pagamento | [Pagamento via Mercado Pago sandbox com webhook](../issues/027.md) | |
| 10 | #29 | pedido | [Criação do pedido no checkout com descontos e uso de cupom](../issues/029.md) | |
| 11 | #28 | pagamento | [Cartões salvos por token do gateway](../issues/028.md) | |
| 12 | #31 | pedido | [Rastreio da entrega com eventos](../issues/031.md) | |
| 13 | #34 | estoque | [Unidade individual por número de série na expedição](../issues/034.md) | |
| 14 | #35 | pedido | [NF-e simulada de venda e de devolução](../issues/035.md) | |
| 15 | #36 | pagamento | [Estorno total ou parcial de pagamento](../issues/036.md) | |
| 16 | #37 | devolucao | [Fluxo de devolução e troca com política configurada](../issues/037.md) | |
| 17 | #33 | identidade | [Verificação de e-mail e recuperação de senha por token](../issues/033.md) | #32 |
| 18 | #46 | identidade | [Consentimento LGPD e exclusão/anonimização de conta](../issues/046.md) | |
| 19 | #47 | identidade | [Login social com Google (OIDC)](../issues/047.md) | |
| 20 | #45 | administracao | [2FA opcional no perfil administrativo](../issues/045.md) | |
| 21 | #86 | compra | [Favoritos: queda de preço, mover tudo para o carrinho e lista compartilhada](../issues/nova-favoritos-compartilhar.md) | |
| 22 | #88 | estoque | [Drones registrados na conta do cliente, com série, garantia e registro ANAC](../issues/nova-meus-drones.md) | |
| 23 | #89 | pedido | [Painel de pedidos: indicadores, busca por CPF/CNPJ, filtro por pagamento e etiquetas em lote](../issues/nova-pedidos-painel.md) | #61 |

A #45 é do módulo `administracao`, mas mexe no login: fica na trilha A, junto das outras issues de login, para os dois agentes não mexerem no login ao mesmo tempo.

### Trilha B

| # | Issue | Módulo | Título | Portão |
|---|---|---|---|---|
| 1 | nova | design | [Casca da loja no protótipo 02](../issues/nova-loja-prototipo-02.md) | |
| 2 | nova | design | [Admin no protótipo 02](../issues/nova-admin-prototipo-02.md) | |
| 3 | #87 | catalogo | [Imagem principal do produto nas listagens e fotos na carga de dados](../issues/nova-imagem-produto.md) | |
| 4 | nova | catalogo | [Mídias do produto no armazenamento S3 (RustFS)](../issues/nova-armazenamento-s3.md) | |
| 5 | #84 | catalogo | [Logo da marca por upload, peso do acessório, sugestão nos compatíveis e kit em Ofertas](../issues/nova-catalogo-complementos.md) | |
| 6 | #83 | design | [Tirar do protótipo o que contradiz o modelo](../issues/nova-ajuste-prototipo.md) | |
| 7 | #21 | catalogo | [Storefront: listagem, filtros e página de produto](../issues/021.md) | |
| 8 | #61 | administracao | [Ações em lote, reordenação de categorias, exportação CSV e etiqueta de pedido](../issues/061.md) | |
| 9 | #32 | notificacao | [E-mails transacionais via Resend com templates editáveis](../issues/032.md) | |
| 10 | #43 | catalogo | [Registro de termos buscados e buscas sem resultado](../issues/043.md) | |
| 11 | #44 | catalogo | [Comparação de especificações entre modelos](../issues/044.md) | |
| 12 | #85 | administracao | [Dashboard: comparação com o período anterior, vendas por dia e mais vendidos](../issues/nova-dashboard-indicadores.md) | |
| 13 | #41 | administracao | [Log de auditoria das ações administrativas](../issues/041.md) | |
| 14 | #38 | atendimento | [Chamados de suporte com histórico de mensagens](../issues/038.md) | #19 |
| 15 | #40 | atendimento | [Perguntas e respostas sobre produtos](../issues/040.md) | #19 |
| 16 | #39 | atendimento | [Avaliações com compra verificada e moderação](../issues/039.md) | |
| 17 | #90 | atendimento | [Suporte: temas e perguntas frequentes com texto fixo](../issues/nova-suporte-faq.md) | |
| 18 | #42 | notificacao | [Preferências de notificação, marketing e descadastro](../issues/042.md) | #24 |
| 19 | #60 | atendimento | [Proposta comercial para empresas](../issues/nova-empresas.md) | |
| 20 | #59 | infra | [Deploy de produção](../issues/nova-deploy.md) | depois das duas trilhas |

Os portões foram postos longe do começo da outra trilha: a #19 é a primeira da A e só é portão para a 14ª da B; a #21 é a 7ª da B e é portão para a 4ª da A. Esse último é o único apertado: se a #21 atrasar, a A segue com a #23, que não depende dela.

## Arquivos que as duas trilhas tocam

| Arquivo | Regra |
|---|---|
| `backend/src/test/issues-entregues.txt`, `frontend/e2e/telas-entregues.txt` | Acrescente a etiqueta só **abaixo da linha da sua trilha** (`# ---- trilha A` ou `# ---- trilha B`) |
| `import.sql` | Ninguém mexe. Dado novo vai em `import-a.sql` ou `import-b.sql`, sem id fixo e referenciando outro registro por chave natural (`(select id from produto where sku = '...')`) |
| `docs/PRD-Aeria.md` | Executor **não** numera decisão. Escreve "Decisão proposta" na descrição do PR; o supervisor numera e escreve no PRD |
| `docs/API.md` | Cada um edita só as linhas dos próprios módulos |
| `frontend/src/app/app.routes.ts` | Rotas novas só dentro do bloco da própria área (`admin`, loja do cliente ou loja do catálogo); a rota é a de `frontend/e2e/conformidade/telas.ts` |
| `frontend/e2e/conformidade/telas.ts` | Cada um edita só as entradas com a etiqueta das próprias issues |
| `backend/pom.xml`, `frontend/package.json` | Dependência nova só com motivo no PR (`SECURITY.md`); avise no quadro antes de adicionar |
| `exception/`, `model/DefaultEntity`, `core/interceptors`, `shared/components` | Código comum: mudança só com pedido no quadro e aval do supervisor |

## Ambiente

As duas trilhas rodam na mesma máquina, cada uma no próprio **worktree** do git (uma cópia de trabalho com branch própria, ligada ao mesmo repositório). A pasta `.worktrees/` fica dentro do repositório e é ignorada pelo git, para o supervisor enxergar as duas.

### Preparação (uma vez, Davi)

1. Merge do PR que trouxe este guia.
2. Criar as etiquetas das trilhas no GitHub:
   ```bash
   gh label create "trilha:a" --color 1f6feb --description "Trilha A: cliente, compra e pós-venda"
   gh label create "trilha:b" --color e8a86b --description "Trilha B: catálogo, vitrine, admin e atendimento"
   ```
3. **Actions > sincronizar-issues**: rodar com "simular" e depois para valer. Isso aplica as etiquetas e cria as issues `nova-*` sem número. Anote no quadro o número das três novas (casca da loja, admin no protótipo 02 e armazenamento S3).
4. Criar os worktrees (PowerShell, na pasta `D:\Projetos\droneshop`):
   ```powershell
   git fetch origin
   git worktree add .worktrees/a origin/develop --detach
   git worktree add .worktrees/b origin/develop --detach
   ```
5. Em cada worktree: `cd frontend; npm install`.
6. Subir os bancos: `cd backend; docker compose up -d postgres postgres-b`.
7. Abrir uma janela do VS Code em `.worktrees/a` e outra em `.worktrees/b` e iniciar os agentes com os prompts de `PROMPTS.md`.

### Portas

| | Trilha A | Trilha B |
|---|---|---|
| API (`quarkus:dev`) | 8080 | 8090 |
| Debug do Quarkus | 5005 | 5006 |
| Testes do backend | 8081 | 8091 |
| Banco de dev | `localhost:5432` | `localhost:5433` (`postgres-b`) |
| Angular | 4200 (`npm start`) | 4210 (`npm run start:b`) |
| Conformidade (Angular / protótipo) | 4200 / 4300 | 4211 / 4311 |

Comandos da trilha B (PowerShell, dentro de `.worktrees/b`):

```powershell
# API
cd backend
$env:QUARKUS_HTTP_PORT="8090"; $env:QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://localhost:5433/droneshopdb"
./mvnw quarkus:dev "-Ddebug=5006"

# testes do backend
./mvnw verify "-Dquarkus.http.test-port=8091"

# tela
cd frontend
npm run start:b

# conformidade com o protótipo
$env:PORTA_APP="4211"; $env:PORTA_PROTOTIPO="4311"; npm run e2e
```

A trilha A usa os comandos normais do `README.md`. Os testes do backend sobem o próprio Postgres pelo Dev Services, então os dois `./mvnw verify` podem rodar juntos. O RustFS (armazenamento S3) é um só para as duas trilhas, mas cada uma usa o próprio bucket (`AERIA_S3_BUCKET`: `aeria-midias-a` e `aeria-midias-b`).

## Ciclo de uma issue (executor)

1. **Pegar:** ler no quadro (`.agentes/QUADRO.md`) qual issue o supervisor liberou. Só começa issue liberada.
2. **Branch nova a partir da `develop` atual:**
   ```bash
   git fetch origin
   git switch -c <numero>-<resumo> origin/develop
   ```
   Issue ainda sem número no GitHub: use o nome do arquivo (`armazenamento-s3`).
3. **Planejar:** ler `docs/issues/<arquivo>.md` (contrato de API e critérios de aceite), o diagrama do módulo e os testes já escritos (`@Tag("issue-N")`). Escrever o plano curto na sua seção de `.agentes/trilha-<a|b>.md` e **esperar o "ok" do Davi** antes de codar.
4. **Implementar** só o que a issue pede, só nos seus módulos, seguindo `AGENTS.md`.
5. **Conferir:** testes da issue (`./mvnw test -Dgroups=issue-N`), `./mvnw verify`, `ng test`, `ng build` e a conformidade das telas da issue. Acrescentar as etiquetas abaixo da linha da sua trilha.
6. **Commit** na branch da issue, em Conventional Commits em português.
7. **Push e PR:** `git push -u origin <branch>` e abrir o PR para a `develop` seguindo `.github/pull_request_template.md`, com `Closes #N`, a trilha preenchida e a etiqueta `trilha:a` ou `trilha:b` (`gh pr create --base develop --label trilha:a ...`). Nunca force push, nunca push na `develop` ou na `main`.
8. **Passagem:** atualizar `.agentes/trilha-<a|b>.md` com o link do PR, o que mudou, o que ficou de fora, "Decisão proposta" (se houver) e "Pedido para a outra trilha" (se houver). Marcar como "PR aberto".
9. **Não revisar nem aprovar** o próprio PR nem o da outra trilha. Pedido de mudança do Davi no PR: corrigir na mesma branch e fazer push de novo.
10. Esperar o supervisor liberar a próxima. Não comece a próxima issue sobre a branch da anterior.

## Rotina do supervisor

Cada vez que o Davi chamar o supervisor ("sincroniza", "status", "revisa a A"):

1. Ler `.agentes/QUADRO.md`, `.agentes/trilha-a.md` e `.agentes/trilha-b.md`, e o estado dos dois worktrees (`git -C .worktrees/a status`, `log origin/develop..HEAD`).
2. **PR aberto:** conferir CI, conflito com a `develop` e se o PR ficou nos módulos da trilha; registrar no quadro e avisar o Davi que o PR está pronto para a revisão dele. O supervisor não revisa nem aprova no GitHub.
3. **Ordem de merge:** sugerir ao Davi um PR por vez na `develop`. Depois de cada merge, avisar a outra trilha no quadro ("develop mudou: #N"); a próxima branch dela já sai da `develop` nova, então não há rebase.
4. **Conflito** num arquivo compartilhado: resolver no PR que entra por último, seguindo as regras da tabela acima.
5. **Decisão proposta:** numerar (D43, D44...), escrever no PRD e citar no PR.
6. **Liberar a próxima** de cada trilha, conferindo o portão. Portão fechado: trocar com a próxima issue da mesma trilha e registrar.
7. **Depois de merges que mudaram `docs/issues`:** lembrar o Davi de rodar `sincronizar-issues`.
8. Atualizar o quadro.

## Quadro (`.agentes/`)

Fica só no disco (ignorado pelo git), na raiz do repositório. Os executores leem com o caminho `../../.agentes/` a partir do worktree.

| Arquivo | Quem escreve | Conteúdo |
|---|---|---|
| `QUADRO.md` | supervisor | Issue liberada de cada trilha, portões, ordem de merge, avisos ("develop mudou") |
| `trilha-a.md` | executor A | Plano, andamento, passagem, decisões propostas, pedidos para a B |
| `trilha-b.md` | executor B | O mesmo, para a trilha B |

Modelo do `QUADRO.md`:

```markdown
# Quadro

Atualizado em: <data e hora>

| Trilha | Issue liberada | Branch | Estado |
|---|---|---|---|
| A | #19 | 19-perfil-cliente | planejando / aguardando ok / codando / PR aberto / na develop |
| B | casca da loja (#NN) | NN-casca-loja | ... |

## Portões
- #24 (A) espera #21 (B): fechado

## Ordem de merge
1. ...

## Avisos
- <data>: develop mudou com #NN (trilha B): import-b.sql, telas-entregues.txt
```

Modelo do `trilha-a.md` (o da B é igual):

```markdown
# Trilha A

## Issue atual: #NN <título>
Estado: planejando | aguardando ok | codando | PR aberto

### Plano
- ...

### Passagem
- O que mudou:
- Fora do escopo:
- Testes rodados:
- Decisão proposta:
- Pedido para a outra trilha:

### PR
<link>
```
