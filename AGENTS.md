# AGENTS.md (Raiz)

Guia geral para agentes de IA que trabalham neste repositório (implementação de issues e revisão de pull requests). Sempre escreva código, commits, comentários e sugestões em **português**, sem usar travessão nem emojis.

Para regras específicas de cada camada, consulte também:
- [`backend/AGENTS.md`](backend/AGENTS.md): Java 25, Quarkus 3, padrão SGA, segurança da API e testes RestAssured.
- [`frontend/AGENTS.md`](frontend/AGENTS.md): Angular 22, conformidade com o protótipo, tokens de design e testes Vitest/Playwright.

## Leitura obrigatória antes de implementar

Antes de iniciar **qualquer** implementação, revisão ou plano, consulte os três documentos de contexto em `docs/`:

| Documento | Para quê |
|---|---|
| [`docs/CONTEXTO_PROJETO.md`](docs/CONTEXTO_PROJETO.md) | Visão, stack, decisões arquiteturais, **estado atual** (o que já está entregue) e **pendências** por fase |
| [`docs/REGRAS_NEGOCIO.md`](docs/REGRAS_NEGOCIO.md) | Entidades, fluxos, validações, máquinas de estado e regras de domínio, marcadas como `[IMPLEMENTADO]` ou `[PENDENTE]` |
| [`docs/modelo.plantuml`](docs/modelo.plantuml) | Modelo de dados **implementado** no backend (entidades, atributos, tipos, enums e relacionamentos) |

Use-os para saber o que já existe (não duplique entidade, rota ou regra), o que a issue em mãos precisa respeitar e onde ela se encaixa no faseamento. Se um deles divergir do código ou do PRD, o PRD e o contrato da issue prevalecem: corrija o documento no mesmo PR.

### Manter os documentos sincronizados

Atualizar esses arquivos faz parte da entrega, **no mesmo PR** da mudança:

- **Mudou o progresso** (issue entregue, etiqueta adicionada em `issues-entregues.txt` ou `telas-entregues.txt`): atualize "Estado atual" e "Pendências" em `docs/CONTEXTO_PROJETO.md`.
- **Mudou o domínio** (nova regra, validação, transição de status, conflito 409, decisão `Dn` registrada ou alterada): atualize `docs/REGRAS_NEGOCIO.md` (troque `[PENDENTE]` por `[IMPLEMENTADO]` quando a regra for implementada) e, se for decisão, a seção de decisões de `docs/CONTEXTO_PROJETO.md`.
- **Mudou o modelo de dados** (entidade, atributo, tipo, enum ou relacionamento): atualize `docs/modelo.plantuml`, o `.puml` do contexto em `docs/diagramas/` e o `backend/src/main/resources/import.sql`.
- **Mudou a stack** (versão de framework, banco, biblioteca ou ferramenta de CI): atualize a seção "Stack" de `docs/CONTEXTO_PROJETO.md`.

Em revisão de PR, documento de contexto desatualizado em relação à mudança é **bloqueante**.

## Contexto do projeto

**Aeria Drones**: e-commerce de drones e acessórios, composto por **loja** (storefront) e **painel administrativo**. Projeto de estudo: pagamento roda permanentemente em **sandbox** (Mercado Pago, D2), a **NF-e é simulada** (D3) e o **e-mail tem envio real** (Resend, D5).

| Onde | O que tem |
|---|---|
| `docs/PRD-Aeria.md` | Requisitos (`RF`), regras de negócio (`RN`) e decisões (`D1` a `D40`). **As decisões valem como regra**: se o código ou PR contraria uma, é bloqueante |
| `docs/issues/NNN.md` | Corpo de cada issue, com o **Contrato de API** e os **Critérios de aceite** |
| `docs/API.md` | Mapa de endpoints e convenções gerais (paginação, `idXxx`, enums `{id, nome}`, erros `Problem`, status `200`/`204`) |
| `docs/diagramas/*.puml` | 8 diagramas de classes PlantUML por contexto delimitado; o código deve refletir o diagrama |
| `docs/design/` | Protótipo navegável (`prototipo/`), `DESIGN.md` (tokens e componentes) e `PRODUCT.md`. O protótipo é a referência visual e estrutural das telas (D38) |
| `SECURITY.md` | Checklist de segurança para backend, frontend e CI. Qualquer violação é **bloqueante** |
| `backend/` | API REST em Java 25, Quarkus 3, Hibernate ORM com Panache, PostgreSQL 16 (ver [`backend/AGENTS.md`](backend/AGENTS.md)) |
| `frontend/` | Aplicação Angular 22 standalone, SCSS, Vitest e Playwright (ver [`frontend/AGENTS.md`](frontend/AGENTS.md)) |

## Regras globais de domínio e arquitetura

- **O modelo vence o protótipo (D36)**: se um campo visual do protótipo não existe no modelo/contrato da issue, ele não deve ser inventado.
- **Unidade de venda é o Produto (D40)**: **não existe `VarianteProduto`**. Cada versão é um `Produto` com `sku`, `preco`, `custoAquisicao`, `estoqueFisico`, `estoqueMinimo` e dimensões de embalagem; versões se agrupam opcionalmente em `GrupoProduto`. Qualquer retorno de `VarianteProduto` é **bloqueante**.
- **Tipo × Categoria (D39)**: o tipo (`DRONE`, `ACESSORIO`, `KIT`) define a estrutura e as rotas (`/drones`, `/acessorios`, `/kits`); a categoria organiza a vitrine dentro de um tipo.
- **Rascunho e exclusão (D21, D24, D28)**: cadastros em `RASCUNHO` (id `1`) aceitam campos vazios; campos obrigatórios só são cobrados ao publicar (`ATIVA`/`INATIVA`). Registros em uso nunca são excluídos em cascata: respondem `409 Conflict` orientando a inativação.
- **Conflito e validação (D22, D30)**: erros seguem a RFC 7807/9457 (`Problem`). A API **não usa 422**: validação é `400`, não encontrado é `404` e conflito de uso ou transição de status inválida é `409`.

## Como revisar um PR

1. Leia a issue vinculada ao PR (`Closes #N`) e o arquivo `docs/issues/NNN.md` correspondente. Revise **contra o contrato de API, os critérios de aceite, o PRD e o protótipo**, não contra preferências pessoais.
2. Confira se o PR faz **apenas** o que a issue pede. Refatoração de código vizinho, formatação fora do escopo, arquivo ou dependência que a issue não pediu: aponte.
3. Classifique cada comentário:
   - **Bloqueante**: bug, contrato de API quebrado, decisão/regra do PRD violada, falha de segurança (`SECURITY.md`), teste afrouxado para passar, divergência de estrutura/tokens em relação ao protótipo.
   - **Sugestão**: legibilidade, nomenclatura, simplificação pontual.
   - **Pergunta**: ambiguidade que só o autor ou mantenedor pode esclarecer.
4. Não comente estilo de formatação que o projeto não padroniza nem peça reescrita de código que já segue o padrão do repositório.
5. Se não houver nenhum item bloqueante, declare isso explicitamente no resumo da revisão.

## Fluxo de entrega, commits e PR

- **Branches e PRs**: branch criada a partir de `develop` (`<numero>-<resumo>` ou `feat/<numero>-...`), PR apontando para `develop` seguindo `.github/pull_request_template.md` com `Closes #N`.
- **Commits**: Conventional Commits em português com escopo do módulo (`feat(catalogo): ...`, `fix(estoque): ...`, `docs(issues): ...`).
- **Travas de regressão no CI**:
  - Ao entregar uma issue de backend, adicione a etiqueta `issue-N` em `backend/src/test/issues-entregues.txt`.
  - Ao entregar uma tela de frontend, adicione a etiqueta `issue-N` em `frontend/e2e/telas-entregues.txt`.
  - Se o modelo mudou, atualize `docs/modelo.plantuml`, o `.puml` correspondente em `docs/diagramas/` e a carga `backend/src/main/resources/import.sql`.
  - Atualize `docs/CONTEXTO_PROJETO.md` e `docs/REGRAS_NEGOCIO.md` conforme a seção "Manter os documentos sincronizados".

## Comandos rápidos

```bash
# Backend (requer Docker para PostgreSQL)
cd backend && ./mvnw verify
cd backend && ./mvnw test -Dgroups=issue-80

# Frontend
cd frontend && npx ng test --watch=false
cd frontend && npm run build
cd frontend && npm run prototipo:verificar
cd frontend && npm run e2e
```

## O que agentes não fazem

- Não executam `git commit`, `git push`, merge nem rebase sem pedido explícito do mantenedor.
- Não aprovam o próprio PR nem alteram regras de proteção (`.github/rulesets`).
- Não afrouxam testes existentes (`src/test` ou `e2e/conformidade`) para fazer a build passar: a correção é no código da aplicação.
- Não incluem segredo, token ou dado pessoal real em código, teste, carga de dados, log ou comentário.
- Não usam travessão nem emojis em documentação, código, commits ou respostas.
