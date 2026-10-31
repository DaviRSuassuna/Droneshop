# Padroes de Codigo e Checklist Angular Frontend

Referencia complementar da skill `angular-frontend` com os padroes de implementacao e o checklist de revisao para modelos, servicos HTTP, estado de sessao, guards, rotas, listagens e formularios de inclusao/edicao.

## 1. Padrao por Camada e Artefato

### Modelos (`src/app/core/models/*.model.ts`)

- Interfaces de leitura (`Xxx`) refletem exatamente o JSON retornado pela API:
  - Enums como `StatusRef` (`{ id: number; nome: string }`).
  - Objetos aninhados conforme o `ResponseDTO` do backend.
  - Paginacao padronizada com `PageResponse<T>` (`items`, `page`, `pageSize`, `totalItems`, `totalPages`).
- Interfaces de escrita (`XxxDTO`) usam `idXxx: number` para relacionamentos e enums (`idStatus`, `idMarca`, `idCategoria`, etc.).
- Erros seguem `ProblemDetail` (`src/app/core/models/problem.model.ts`) com `title`, `status`, `detail` e `errors?: ProblemFieldError[]`.

### Servicos HTTP (`src/app/core/services/*.service.ts` e `*.spec.ts`)

- Decorados com `@Injectable({ providedIn: 'root' })`, usando `private readonly http = inject(HttpClient)` e `baseUrl = `${environment.apiUrl}/<recurso>``.
- Operacoes padrao:
  - `listar(page = 0, pageSize = 10, filtros?)`: monta `HttpParams` apenas para filtros preenchidos e retorna `Observable<PageResponse<Xxx>>`.
  - `buscarPorId(id: number)`: `GET ${this.baseUrl}/${id}` retornando `Observable<Xxx>`.
  - `criar(dto: XxxDTO)`: `POST ${this.baseUrl}` retornando `Observable<Xxx>`.
  - `atualizar(id: number, dto: XxxDTO)`: `PUT ${this.baseUrl}/${id}` retornando `Observable<void>`.
  - `excluir(id: number)`: `DELETE ${this.baseUrl}/${id}` retornando `Observable<void>`.
- Todo servico deve possuir `*.service.spec.ts` com `HttpTestingController` testando verbo HTTP, URL, `HttpParams` e corpo enviado.

### Sessao, Interceptor e Guards (`src/app/core/`)

- **Sessao (`AuthService`)**:
  - Armazena o token na chave `CHAVE_TOKEN` em `localStorage` (quando `manterConectado = true`) ou `sessionStorage`.
  - Valida o claim `exp` do JWT antes de considerar a sessao ativa e remove tokens expirados ou malformados.
- **Interceptor (`authInterceptor`)**:
  - Compara a origem da requisicao com `new URL(environment.apiUrl).origin`. Nunca envia o header `Authorization` para dominios externos nem para `/auth/login`.
  - Em erro `401` vindo da API, executa `limparSessao()` e redireciona para `/entrar` preservando `returnUrl`.
- **Guards (`adminAuthGuard` e guards de rota)**:
  - Implementados como funcoes (`CanActivateFn` / `CanActivateChildFn`) usando `inject(AuthService)` e `inject(Router)`.
  - Retornam `UrlTree` para `/entrar` com `queryParams: { returnUrl: state.url }` quando nao autenticado.
  - Permissoes de interface (ocultar/desabilitar botao por perfil ou modulo) complementam a experiencia, mas nunca substituem a validacao da API.

### Componentes de Listagem (`listagem/`)

- Sinais de estado:
  - `carregando = signal(true)`
  - `erro = signal<string | null>(null)`
  - `itens = signal<Xxx[]>([])`
  - Sinais de filtro (`termoBusca`, `filtroStatus`, etc.) e `pagina = signal(0)`.
- Derivacoes com `computed()`:
  - Lista filtrada/paginada, `totalPaginas` e `intervaloExibido` (ex.: `1-10` ou `0-0` quando vazio).
  - Ao alterar qualquer filtro, resetar `this.pagina.set(0)`.
- Exclusao na listagem:
  - Confirmar com o usuario antes de chamar `service.excluir(id)`.
  - Em caso de falha (`409 Conflict`), exibir `err.error?.detail` orientando sobre o vinculo ou inativacao.

### Componentes de Inclusao e Edicao (`cadastro/`)

- Mesmo componente atende criacao (`/novo` ou `/nova`) e edicao (`/:id/editar`), diferenciando pelo parametro `:id` da rota (`modoEdicao = computed(() => this.id() !== null)`).
- Sinais de controle:
  - `carregando = signal(false)`, `salvando = signal(false)`, `excluindo = signal(false)`
  - `erroGeral = signal<string | null>(null)`
  - `errosCampo = signal<Record<string, string>>({})`
- Formulario e Rascunho (D21, D28):
  - `form = this.fb.nonNullable.group({ ... })`
  - `valores = toSignal(this.form.valueChanges.pipe(map(() => this.form.getRawValue())), { initialValue: this.form.getRawValue() })`
  - `pendencias = computed(() => ...)` listando os rotulos obrigatorios ainda vazios para exibicao em `.card.pend`.
  - `salvar(publicar = false)`: se `publicar` for `true` e o status atual for `RASCUNHO`, promove `idStatus` para `ATIVA` antes de enviar o DTO com strings normalizadas (`.trim()`) e documentos apenas com digitos.
- Tratamento de erro `ProblemDetail`:
  - Se `problem?.errors?.length`, mapeia cada `{ field, message }` em `errosCampo` e define `erroGeral` com `problem.detail`.
  - Se nao houver `errors` por campo, exibe `problem?.detail` em `erroGeral`.

## 2. Checklist de Revisao e Implementacao

Use esta lista antes de concluir qualquer entrega ou revisao:

1. **Contratos, Escopo e Limites**:
   - [ ] Nenhum arquivo em `backend/`, `package.json` ou `package-lock.json` foi modificado sem autorizacao explicita?
   - [ ] Todos os endpoints, query params e campos de `model.ts` existem em `docs/issues/NNN.md` e `docs/API.md` (sem campos inventados e sem `VarianteProduto`)?
   - [ ] As rotas em `app.routes.ts` batem com `frontend/e2e/conformidade/telas.ts`?
2. **Conformidade Visual, Tokens e Acessibilidade**:
   - [ ] Hierarquia HTML (`.crumb`, `h1`, `.btn`, `.sh > h2`, `.lbl`, colunas de tabela, `aside.rail > section.card`, `.actionbar`) corresponde ao prototipo em `docs/design/prototipo/`?
   - [ ] Estilos usam exclusivamente os tokens CSS de `src/styles/` (sem hex/`rgb()` soltos, sem `box-shadow`, sem `font-weight > 500`)?
   - [ ] Controles possuem `<label for>` ou `aria-label`, `[attr.aria-invalid]` e mensagens de erro com `role="alert"`?
   - [ ] A tela nao apresenta rolagem horizontal em `1280px`, `480px`, `390px` e `360px`?
3. **Estados, Formularios e Seguranca**:
   - [ ] Os 4 estados (carregamento, sucesso, vazio e erro `ProblemDetail` `400`/`404`/`409`) sao tratados na tela?
   - [ ] Cadastros com status respeitam o fluxo de `RASCUNHO` (`pendencias` reativas) vs `Publicar` (`ATIVA`)?
   - [ ] CPF, CNPJ, CEP e telefone sao enviados apenas com digitos?
   - [ ] Nao ha uso de `any`, `*ngIf`/`*ngFor`, `CommonModule`, `bypassSecurityTrust*` ou `[innerHTML]` nao sanitizado?
4. **Testes e Build**:
   - [ ] `npx ng test --watch=false` e `npm run build` passam sem erros?
   - [ ] `npm run prototipo:verificar` e `npm run e2e` passam para a tela entregue?
