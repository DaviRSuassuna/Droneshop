# AGENTS.md (Frontend)

Guia para agentes de IA que implementam ou revisam código em `frontend/` (`droneshop-web`). Complementa o [`AGENTS.md` da raiz](../AGENTS.md), o [`docs/design/DESIGN.md`](../docs/design/DESIGN.md), o [`frontend/e2e/README.md`](e2e/README.md) e o [`SECURITY.md`](../SECURITY.md). Escreva código, textos de interface, testes e comentários em **português**, sem usar travessão nem emojis.

## Stack e arquitetura

- **Angular 22** (standalone, `OnPush` e `standalone` já são padrão do framework: **não** declare `standalone: true` nem `changeDetection: ChangeDetectionStrategy.OnPush` nos decorators).
- **TypeScript estrito** (sem `any`; use tipos explícitos dos models ou `unknown`), **SCSS**, **Vitest** (`ng test`) para testes unitários e **Playwright** (`e2e/conformidade`) para conformidade com o protótipo.
- Estrutura em `src/app/`:
  - `admin/`: telas do painel administrativo por módulo (`catalogo`, `configuracao`, `cupons`, `dashboard`, `estoque`, `pedidos`, `promocoes`, `usuarios`).
  - `storefront/`: telas da loja (`auth`, `catalogo`, `compra`, `conta`, `empresas`, `home`, `suporte`).
  - `core/`: `models/` (contratos da API), `services/` (clientes HTTP + `*.spec.ts`), `guards/`, `interceptors/`, `utils/`.
  - `shared/`: `components/`, `directives/` (`mascara.directive.ts`), `pipes/` (`cep`, `cnpj`, `telefone`), utilitários (`arquivo.ts`, `csv.ts`).
  - `layout/`: cabeçalhos, menus e estruturas de página do `admin` e do `storefront`.

## Regra de ouro: Conformidade com o Protótipo e Design System (D38)

O protótipo navegável (`docs/design/prototipo/`) e o [`docs/design/DESIGN.md`](../docs/design/DESIGN.md) são a **referência obrigatória** de toda tela:

1. **Estrutura idêntica (`e2e/conformidade/esqueleto.ts`)**:
   - Menu, breadcrumb (`.crumb`), título (`h1`), botões (`.btn`, `.btn.p`, `.btn.d`), filtros, títulos de seção (`.sh > h2`), rótulos (`.lbl`), abas, cards laterais (`aside.rail > section.card`) e barra de ações (`.actionbar`) devem ter os mesmos textos e a mesma hierarquia do protótipo.
   - Tabelas devem ter exatamente as mesmas colunas e na mesma ordem do protótipo.
   - As rotas do Angular seguem o mapa oficial em `e2e/conformidade/telas.ts`.
   - Não antecipe na tela recursos ou tabelas cuja API pertença a issues futuras ainda não entregues (ver `ignorar` e `tabelasSemApi` em `e2e/conformidade/telas.ts`).

2. **Tokens e estilos (`DESIGN.md` e `src/styles/`)**:
   - **Tema único escuro ("Noite + Campo")**: use sempre as variáveis CSS definidas em `src/styles/` (`--bg`, `--bg2`, `--surf`, `--surf2`, `--field`, `--ink`, `--ink2`, `--mute`, `--faint`, `--line`, `--line2`, `--amber`, `--ok`, `--warn`, `--info`).
   - **Proibido**: cores hexadecimais ou `rgb()` soltas fora dos tokens, peso de fonte acima de `500`, `box-shadow` em caixas (a separação é por borda `--line` e superfície), gradiente ou `--amber` como fundo de botão primário (o botão `.btn.p` é sólido em `--ink` com texto em `--bg`), fotos de produtos/marcas reais.
   - **Tipografia**: `Sora` (200 a 300) para títulos, preços e números grandes; `DM Sans` (400 a 500) para corpo e inputs; `IBM Plex Mono` (400 a 500) para `.lbl`, SKUs, códigos e unidades.

3. **Responsividade e Acessibilidade (D20)**:
   - Toda tela deve funcionar no desktop (`1280px`) e no celular (`360px`, `390px` e `480px`) **sem rolagem horizontal**.
   - Deve passar sem violações **axe WCAG A/AA**: todo controle de formulário precisa de `<label for="...">` ou `aria-label`, grupos de rádio usam `role="radiogroup"`, estados de erro usam `[attr.aria-invalid]="!!mensagemErro('campo')"` e mensagens de erro usam `role="alert"`.

## Padrões de código Angular no projeto

Siga o padrão real já adotado nas telas e serviços do repositório:

- **Componentes**:
  - Arquivos separados para componentes de página (`nome-componente.ts`, `.html`, `.scss`), com caminhos relativos no `@Component`.
  - Injeção de dependências sempre com `inject()`.
  - Estado local reativo com `signal()`, estado derivado com `computed()`, e integração com `FormGroup` via `toSignal(this.form.valueChanges...)`.
  - Não use `mutate` em signals; use `set` ou `update`.
  - Use `input()` e `output()` em vez de `@Input()` / `@Output()`, e bindings no objeto `host` do decorator em vez de `@HostBinding` / `@HostListener`.
- **Templates**:
  - Controle de fluxo nativo (`@if`, `@for`, `@switch`); nunca use `*ngIf`, `*ngFor`, `*ngSwitch`, `ngClass` ou `ngStyle`.
  - Não importe `CommonModule`; importe apenas as diretivas, pipes e `RouterLink` utilizados.
- **Formulários, Rascunho e Erros da API (`ProblemDetail`)**:
  - Os cadastros usam `ReactiveFormsModule` com `FormBuilder.nonNullable.group(...)`.
  - **Rascunho (D21, D28)**: cadastros com status (`STATUS_PUBLICACAO` em `core/models/status.model.ts`) iniciam em `RASCUNHO` (`1`), calculam a lista reativa `pendencias = computed(...)` exibida no card `.card.pend` da coluna lateral (`.rail`), permitem "Salvar rascunho" com campos vazios e mudam para `ATIVA` (`2`) ao clicar em "Publicar".
  - **Erros no formato Problem**: trate `HttpErrorResponse` lendo `err.error as ProblemDetail` (`core/models/problem.model.ts`). Preencha `errosCampo = signal<Record<string, string>>({})` a partir de `problem.errors` (`fe.field` -> `fe.message`) e `erroGeral = signal<string | null>(null)` a partir de `problem.detail`.
- **Models e Services (`core/`)**:
  - Todo contrato em `core/models/*.model.ts` reflete exatamente os nomes de campo do contrato de API da issue (`idXxx` no envio, objetos e `{ id, nome }` na resposta).
  - Services em `core/services/*.service.ts` usam `@Injectable({ providedIn: 'root' })`, `inject(HttpClient)` e `${environment.apiUrl}/...`.
  - **Máscaras e documentos**: CPF, CNPJ, CEP e telefone são enviados à API **apenas com dígitos** (`core/utils/documentos.ts`); a formatação visual fica nos pipes (`cep`, `cnpj`, `telefone`) e na diretiva `mascara.directive.ts`.

## Segurança no Frontend (`SECURITY.md`)

- Nunca use `bypassSecurityTrust*` nem `[innerHTML]` com conteúdo vindo da API sem sanitização.
- O token JWT só é anexado pelo `auth.interceptor.ts` para requisições destinadas a `environment.apiUrl`.
- Não coloque chaves privadas, segredos ou URLs internas em código ou em `environment*.ts`.

## Testes

1. **Unitários (`*.spec.ts` com Vitest)**:
   - Todo service em `core/services/` deve ter seu `*.service.spec.ts` cobrindo método HTTP, URL, query params (`HttpParams`) e corpo enviado em cada operação.
   - Guards e interceptors em `core/` também possuem specs dedicadas.
2. **Conformidade com o protótipo (`e2e/conformidade` com Playwright)**:
   - Compara estrutura, tokens/estilos computados e ausência de overflow horizontal em `390px` contra o protótipo (`docs/design/prototipo`).
   - A API é simulada em `e2e/conformidade/api-falsa.ts` e por tela em `e2e/conformidade/telas.ts`.
   - Ao concluir uma tela de uma issue, certifique-se de que ela passa em `npm run e2e -- --grep @issue-N` e adicione a etiqueta em `e2e/telas-entregues.txt`.
   - Se for necessário alterar o design, a mudança começa no protótipo (`docs/design/prototipo`) em PR próprio, seguindo o fluxo descrito em [`e2e/README.md`](e2e/README.md).

## Comandos úteis

```bash
# Testes unitários (Vitest)
npx ng test --watch=false

# Build de produção (valida tipos e bundles)
npm run build

# Verificar integridade do protótipo (links, páginas, viewport, alt)
npm run prototipo:verificar

# Testes E2E de conformidade com o protótipo (sobe app em 4200 e protótipo em 4300)
npm run e2e
npm run e2e -- --grep @issue-80
npm run e2e:relatorio
```
