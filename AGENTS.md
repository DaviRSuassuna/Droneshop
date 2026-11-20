# AGENTS.md

Guia para agentes de IA que trabalham neste repositório, na **implementação das issues em duas trilhas paralelas** (`docs/agentes/README.md`). A seção "Como revisar um PR" é o critério que o mantenedor usa ao revisar e que o executor confere antes de abrir o PR; agente não revisa nem aprova PR no GitHub. Escreva código, comentários e sugestões em **português**.

## Contexto

Aeria Drones: e-commerce de drones e acessórios, com loja (storefront) e painel administrativo. Projeto de estudo: pagamento em sandbox e NF-e simulada.

| Onde | O que tem |
|---|---|
| `docs/PRD-Aeria.md` | Requisitos (RF), regras de negócio (RN) e decisões (D1, D2...). **As decisões valem como regra**: se o PR contraria uma, aponte |
| `docs/issues/NNN.md` | Corpo de cada issue, com o **contrato de API** e os critérios de aceite |
| `docs/API.md` | Mapa de endpoints e convenções (paginação, `idXxx`, status `{id, nome}`, erros Problem) |
| `docs/diagramas/*.puml` | Diagramas de classes por módulo; o código segue o diagrama |
| `docs/design/` | Protótipo navegável e `DESIGN.md` (tokens, componentes, movimento). O protótipo 02 é a referência das telas (D38, D41) |
| `docs/agentes/` | Trabalho em paralelo: papéis (supervisor e dois executores), dono de cada módulo, ordem das trilhas, portões e prompts |
| `backend/` | Java 25, Quarkus 3, Hibernate ORM com Panache, PostgreSQL 16 |
| `frontend/` | Angular 22 (standalone, lazy loading), SCSS, Vitest, Playwright |

## Como revisar um PR

1. Leia a issue ligada ao PR (`Closes #N`) e o `docs/issues/NNN.md` correspondente. Revise **contra o contrato e os critérios de aceite**, não contra o seu gosto.
2. Confira se o PR faz só o que a issue pede. Refatoração de código vizinho, arquivo ou dependência que a issue não pediu: aponte.
3. Classifique cada comentário:
   - **Bloqueante**: bug, contrato de API quebrado, regra do PRD violada, falha de segurança (ver `SECURITY.md`), teste ajustado para passar.
   - **Sugestão**: legibilidade, nome, simplificação.
   - **Pergunta**: ambiguidade que só o autor resolve.
4. Não comente formatação que o projeto não padroniza nem peça reescrita de algo que já segue o padrão.
5. Se não houver nada bloqueante, diga isso claramente.

## Trabalho em paralelo

O backlog anda em duas trilhas ao mesmo tempo (`docs/agentes/README.md`). Na revisão, além do resto:

- O PR mexe só nos módulos e telas da trilha da issue (etiqueta `trilha:a` ou `trilha:b`). Arquivo de módulo da outra trilha sem aval do supervisor: **bloqueante**.
- Carga de dados nova vai em `import-a.sql` ou `import-b.sql`, sem id fixo e referenciando por chave natural. Mudança no `import.sql`: bloqueante.
- Etiquetas de `issues-entregues.txt` e `telas-entregues.txt` ficam abaixo da linha da trilha.
- Decisão nova aparece como "Decisão proposta" na descrição do PR; quem numera e escreve no PRD é o supervisor.

## Backend (padrão SGA)

Confira em todo PR de backend:

- Entidade herda `DefaultEntity`; enums de domínio têm `id` e `nome`, gravados pelo id com `converterjpa` e expostos em uma rota de listagem (D31).
- DTO de entrada é `record XxxRequestDTO` com Bean Validation e mensagens terminadas em `(back)`; texto em branco vira `null` (`Textos.nuloSeVazio`) quando o cadastro aceita rascunho.
- Camadas: `model`, `dto`, `mapper`, `repository` (Panache), `service` (interface + `Impl`, `@Transactional` nas escritas) e `resource`.
- Listagens devolvem `PageResponse`; filtros vazios são ignorados.
- Erros no formato Problem: 400 com `errors[].field` igual ao nome do campo do contrato, 404 para recurso inexistente, 409 para "em uso" (D24: o que está em uso não se exclui, se inativa).
- Rascunho aceita campos vazios; ATIVO/INATIVO exige os obrigatórios (D21, D28).
- Desde a D40 não existe variante: o produto é a unidade de venda (`sku`, `preco`, estoque, embalagem), o kit é o tipo `KIT` e versões se juntam em `GrupoProduto`. Qualquer volta de `VarianteProduto` é bloqueante.
- Estoque físico só muda na criação (saldo inicial) ou por `MovimentoEstoque` (RF14).
- Mudou o modelo: o `.puml` do módulo e a carga de dados da trilha (`import-a.sql` ou `import-b.sql`) acompanham.

### Testes do backend

- Testes de API com RestAssured (`@QuarkusTest`), etiquetados com `@Tag("issue-N")`. As etiquetas em `backend/src/test/issues-entregues.txt` bloqueiam o merge.
- Massa de dados só pela API, com `testes/Fixtures.java`; cada teste cria os próprios registros e não depende do total das tabelas.
- Teste que foi afrouxado para passar (asserção removida, status trocado sem mudança no contrato) é bloqueante: a correção é no código.
- Serviços externos usam os falsos de teste (`ViaCepFalso`, `MercadoPagoFalso`, `FreteFalso`, `ResendFalso`, `GoogleFalso`); nenhum teste depende da internet.

## Frontend

- Tela nova ou alterada segue a página correspondente do protótipo (`docs/design/prototipo`) e os tokens do `DESIGN.md`. Cor, fonte ou espaçamento fixo fora dos tokens: aponte.
- Efeito de movimento vem da camada de efeitos (`frontend/src/styles` e `shared/directives`, um por arquivo) e respeita "reduzir movimento" (`DESIGN.md`, seção Movimento). Efeito que carrega informação ou que ignora `prefers-reduced-motion`: bloqueante.
- Toda tela funciona no desktop e no celular de 360 a 480px, sem rolagem horizontal (D20).
- Conformidade com o protótipo: `frontend/e2e/conformidade` (Playwright). Etiquetas em `frontend/e2e/telas-entregues.txt` bloqueiam o merge.
- Services em `core/services` têm `*.spec.ts` cobrindo método, URL, parâmetros e corpo de cada chamada.
- Models em `core/models` batem com o contrato de API da issue.

## Commits e PR

- Conventional Commits em português, com escopo do módulo: `feat(catalogo): ...`, `fix(estoque): ...`, `docs(issues): ...`.
- O PR segue `.github/pull_request_template.md` e liga a issue com `Closes #N`.

## Comandos

```bash
cd backend && ./mvnw verify                      # backend inteiro (precisa do Docker)
cd backend && ./mvnw test -Dgroups=issue-80      # testes de uma issue
cd frontend && npx ng test --watch=false         # specs do frontend
cd frontend && npx ng build                      # build
cd frontend && npx playwright test e2e/conformidade
```

A trilha B usa outras portas (API 8090, banco 5433, testes 8091, Angular 4210): comandos em `docs/agentes/README.md`, seção Ambiente.

## O que agentes não fazem

- Neste projeto, agentes **podem** fazer commit, push da branch da própria issue e abrir o PR para a `develop` (`docs/agentes/README.md`, "Ciclo de uma issue").
- **Não revisam nem aprovam PR** no GitHub, não fazem merge e não usam o bypass do ruleset: revisão, aprovação e merge são do mantenedor.
- Não fazem force push, rebase nem push direto na `develop` ou na `main`.
- Não começam issue que o supervisor não liberou no quadro (`.agentes/QUADRO.md`) nem mexem em módulo da outra trilha.
- Não aprovam o próprio PR nem alteram regras de proteção (`.github/rulesets`).
- Não incluem segredo, token ou dado pessoal real em código, teste, log ou comentário.
