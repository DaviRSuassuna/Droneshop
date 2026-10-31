---
name: angular-frontend
description: >-
  Orienta a implementacao e a revisao de frontend Angular standalone.
  Use esta skill ao criar, alterar ou revisar componentes, rotas, formularios,
  servicos HTTP, modelos, guards, estado de sessao, listagens, inclusao, edicao
  e integracao com APIs.
---

# Angular Frontend

Runbook para implementacao e revisao de funcionalidades frontend em Angular standalone (`frontend/`), alinhado aos contratos HTTP da API, aos documentos de dominio, ao Design System e ao prototipo navegavel.

Para padroes detalhados de codigo (listagem, inclusao/edicao, servicos, guards e sessao) e checklist de revisao, consulte [padroes-frontend.md](./references/padroes-frontend.md).

## 1. Inspecao Previa Obrigatoria do Projeto

Antes de criar ou editar qualquer arquivo em `frontend/`, execute obrigatoriamente esta sequencia de leitura:

1. **Guias, seguranca e design**:
   - Leia [`AGENTS.md`](../../../AGENTS.md) na raiz, [`frontend/AGENTS.md`](../../../frontend/AGENTS.md) e [`SECURITY.md`](../../../SECURITY.md).
   - Consulte [`docs/design/DESIGN.md`](../../../docs/design/DESIGN.md), o HTML correspondente em `docs/design/prototipo/` e o mapeamento em `frontend/e2e/conformidade/telas.ts`.
2. **Documentos de dominio e contratos HTTP**:
   - Consulte [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md) e [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) para conferir regras aprovadas (`[IMPLEMENTADO]` e `[PENDENTE]`) e decisoes (`D1` a `D40`).
   - Consulte [`docs/modelo.plantuml`](../../../docs/modelo.plantuml), [`docs/API.md`](../../../docs/API.md) e a especificacao da tarefa em `docs/issues/NNN.md`.
3. **Inspecao e reutilizacao de padroes existentes**:
   - Inspecione `frontend/package.json` para confirmar as bibliotecas instaladas (Angular 22 standalone; se o escopo mencionar Angular Material/CDK, verifique antes se o pacote consta em `package.json` ou se o projeto utiliza os componentes e tokens proprios em `src/styles/` e `src/app/shared/`).
   - Leia os modelos (`src/app/core/models/`), servicos (`src/app/core/services/`), guards (`src/app/core/guards/`), interceptors (`src/app/core/interceptors/`), utilitarios/pipes/diretivas (`src/app/shared/` e `src/app/core/utils/`) e componentes vizinhos do modulo antes de escrever codigo novo.
   - Se houver divergencia entre prototipo e modelo/contrato, **o modelo e o contrato da issue vencem o prototipo (D36)**. Se houver ambiguidade no contrato, pare e pergunte ao mantenedor.

## 2. Arquitetura, Reutilizacao e Contratos HTTP

Respeite a organizacao em `src/app/` (`admin/`, `storefront/`, `core/`, `shared/`, `layout/`) e reutilize os padroes consolidados do projeto:

- **Angular Standalone moderno**:
  - `standalone` e `ChangeDetectionStrategy.OnPush` ja sao padrao: **nao** declare `standalone: true` nem `changeDetection` no `@Component`.
  - Use arquivos separados (`.ts`, `.html`, `.scss`), injecao via `inject()`, estado com `signal()` (`set`/`update`), derivacao com `computed()`, `input()`/`output()` e controle de fluxo nativo (`@if`, `@for`, `@switch`).
  - Nao importe `CommonModule` nem use `*ngIf`, `*ngFor`, `ngClass`, `ngStyle` ou o tipo `any`.
- **Modelos (`core/models/`) e Servicos HTTP (`core/services/`)**:
  - Os contratos TypeScript devem espelhar **exatamente** os campos e tipos do contrato HTTP (`idXxx` nos DTOs de escrita, objetos aninhados e `{ id, nome }` para enums na leitura, `PageResponse<T>` nas listagens paginadas).
  - **Ausencia de campos ou endpoints inventados**: nunca crie propriedade em model, payload ou chamada HTTP que nao exista em `docs/API.md` ou `docs/issues/NNN.md`. Nunca introduza `VarianteProduto` (D40).
  - Servicos usam `@Injectable({ providedIn: 'root' })`, `inject(HttpClient)` e `${environment.apiUrl}/...`.
- **Reutilizacao de utilitarios**:
  - Reaproveite `ProblemDetail` (`core/models/problem.model.ts`), `STATUS_PUBLICACAO` (`core/models/status.model.ts`), formatadores e limpeza de digitos (`core/utils/documentos.ts`), diretiva de mascara (`shared/directives/mascara.directive.ts`), pipes (`cep`, `cnpj`, `telefone`) e download de arquivos (`shared/arquivo.ts`).

## 3. Estados de Tela, Formularios, Sessao e Permissoes

1. **Tratamento de carregamento, sucesso, vazio e erro**:
   - **Carregamento e submissao**: controle sinais explicitos (`carregando`, `salvando`, `excluindo`) para desabilitar acoes concorrentes e informar o usuario.
   - **Vazio**: toda listagem ou tabela deve tratar explicitamente a lista vazia (`@if (itens().length === 0)`) conforme a estrutura esperada.
   - **Sucesso**: apos inclusao, edicao ou exclusao, atualize o estado local ou navegue para a rota prevista, limpando erros anteriores (`erroGeral.set(null)`, `errosCampo.set({})`).
   - **Erro HTTP (`ProblemDetail` RFC 7807/9457)**: trate `HttpErrorResponse` extraindo `err.error as ProblemDetail`. Preencha `errosCampo` a partir de `problem.errors` (`field` -> `message`) em erros `400`, trate `404` (registro nao encontrado) e exiba `problem.detail` em conflitos `409` (registro em uso ou transicao invalida). A API nao utiliza `422`.
2. **Validacao de formularios, rascunho e acessibilidade**:
   - Use `ReactiveFormsModule` com `FormBuilder.nonNullable.group(...)` e sincronize mudancas com `toSignal(this.form.valueChanges...)`.
   - **Rascunho (D21, D28)**: cadastros com status de publicacao iniciam em `RASCUNHO` (`1`), calculam `pendencias = computed(...)` para o card `.card.pend`, permitem salvar rascunho com campos vazios e alteram para `ATIVA` (`2`) ao publicar.
   - **Documentos com mascara**: envie CPF, CNPJ, CEP e telefone para a API **apenas com digitos**.
   - **Acessibilidade (D20 / WCAG A/AA)**: associe cada controle a `<label for="...">` ou `aria-label`, aplique `[attr.aria-invalid]="campoInvalido('campo')"` e exiba mensagens de erro com `role="alert"`. Garanta ausencia de rolagem horizontal em `1280px`, `480px`, `390px` e `360px`.
3. **Estado de sessao, rotas, guards e permissoes**:
   - Gerencie sessao exclusivamente via `AuthService` (`localStorage`/`sessionStorage`, validacao de expiracao `exp` do JWT e limpeza em `limparSessao()`).
   - O `authInterceptor` anexa `Authorization: Bearer <token>` somente para requisicoes da origem `environment.apiUrl` (exceto login) e, ao receber `401`, limpa a sessao e redireciona para `/entrar` com `returnUrl`.
   - Proteja rotas administrativas em `app.routes.ts` com `canActivateChild: [adminAuthGuard]` (e guards especificos de cliente/perfil/modulo quando previstos na issue), respeitando os caminhos exatos de `e2e/conformidade/telas.ts`.

## 4. Limites Estritos (O Que Nao Fazer Sem Autorizacao Explicita)

O agente **nao deve**, sob nenhuma hipotese, realizar as acoes abaixo sem autorizacao explicita do mantenedor:

- **Nao alterar arquivos do backend (`backend/`)**, migrations, `import.sql` ou contratos de API para acomodar necessidades do frontend.
- **Nao instalar, remover ou atualizar dependencias** em `frontend/package.json` ou `package-lock.json` (incluindo nao adicionar `@angular/material`, `@angular/cdk` ou bibliotecas externas sem aprovacao).
- **Nao inventar endpoints, rotas, query params ou campos de payload/resposta** fora do contrato aprovado em `docs/issues/NNN.md` e `docs/API.md`.
- **Nao alterar o prototipo (`docs/design/prototipo/`)** nem afrouxar testes em `src/app/**/*.spec.ts` ou `frontend/e2e/conformidade/` para fazer a build passar.
- **Nao usar `bypassSecurityTrust*`, `[innerHTML]` sem sanitizacao, cores fora dos tokens CSS (`DESIGN.md`) ou segredos em `environment*.ts`**.

## 5. Passo a Passo de Execucao, Testes e Build

1. **Mapear o escopo**: confirme na issue (`docs/issues/NNN.md`), no prototipo e em `e2e/conformidade/telas.ts` as rotas, colunas, filtros, campos do formulario e endpoints consumidos.
2. **Implementar ou revisar em ordem**:
   - `core/models/*.model.ts` -> `core/services/*.service.ts` (+ `*.service.spec.ts`) -> guards/rotas (`app.routes.ts`) -> componentes de listagem/inclusao/edicao (`.ts`, `.html`, `.scss`).
   - Confira cada item no checklist de [padroes-frontend.md](./references/padroes-frontend.md).
3. **Executar validacoes, testes e build**:
   ```bash
   # Testes unitarios (Vitest)
   cd frontend && npx ng test --watch=false

   # Build de producao e verificacao de tipos
   cd frontend && npm run build

   # Integridade do prototipo e conformidade E2E (Playwright)
   cd frontend && npm run prototipo:verificar
   cd frontend && npm run e2e -- --grep @issue-<numero>
   ```
4. **Sincronizar documentacao e travas de entrega**:
   - Ao concluir uma tela de issue, adicione `issue-<numero>` em `frontend/e2e/telas-entregues.txt` e atualize `docs/CONTEXTO_PROJETO.md` e `docs/REGRAS_NEGOCIO.md` se aplicavel.
5. **Registrar evidencias**:
   - Liste os arquivos criados/alterados com links diretos e reporte o resultado dos testes unitarios, build e E2E.
   - Em revisoes de PR, classifique cada apontamento como **Bloqueante**, **Sugestao** ou **Pergunta**, declarando explicitamente se nao houver itens bloqueantes.
