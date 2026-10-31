# Padroes Visuais, Responsividade e Acessibilidade (UI Frontend)

Referencia complementar da skill `ui-frontend` para implementacao e revisao de componentes, telas, estados visuais e acessibilidade no frontend Angular (`frontend/`).

## 1. Padroes por Tipo de Elemento Visual

### Telas e Layouts (`admin/` e `storefront/`)

- **Hierarquia estrutural**:
  - Respeite a arvore semantica do prototipo verificada por `frontend/e2e/conformidade/esqueleto.ts`: navegacao principal, `.crumb` (breadcrumb), `h1` (titulo da pagina), barra de filtros, secoes com `.sh > h2`, coluna lateral `aside.rail > section.card` e `.actionbar` fixa no rodape dos formularios administrativos.
- **Angular 22 moderno**:
  - Nao declare `standalone: true` nem `changeDetection: ChangeDetectionStrategy.OnPush` nos decorators `@Component` (ja sao o padrao do projeto).
  - Use controle de fluxo nativo (`@if`, `@for`, `@switch`) e bindings diretos (`[class.ativo]`, `[style.width.%]`), sem importar `CommonModule`.

### Tabelas e Listagens

- **Estrutura e semantica**:
  - Use `<table>`, `<thead>`, `<tbody>` e `<th scope="col">` com exatamente os mesmos titulos e ordem de colunas do prototipo.
  - Em telas moveis (`360px` a `480px`), aplique o padrao responsivo do prototipo/CSS global para evitar rolagem horizontal da pagina inteira.
- **Paginacao e filtros**:
  - Sincronize mudancas de filtro, busca e pagina com o estado de carregamento (`carregando`) e resete para a primeira pagina ao alterar filtros.

### Formularios, Labels e Mensagens

- **Associacao de rotulos**:
  - Todo `<input>`, `<select>` e `<textarea>` deve ter `id` unico associado a um `<label for="...">` (geralmente com classe `.lbl`) ou `aria-label` quando o rotulo visual for implicito (como busca compacta ou filtro).
- **Erros por campo e erro geral (`ProblemDetail`)**:
  - Exiba mensagens de validacao logo abaixo do campo com `role="alert"` e marque o controle com `[attr.aria-invalid]="!!mensagemErro('nomeCampo')"`.
  - Exiba falhas gerais de requisicao (`erroGeral`) ou conflitos `409 Conflict` em destaque acessivel (`role="alert"`), preservando os dados ja digitados pelo usuario.
- **Rascunho e pendencias (Painel Admin)**:
  - Nos cadastros que suportam `RASCUNHO` (`id = 1`), calcule reativamente `pendencias = computed(...)` e liste os itens faltantes no card `.card.pend` da coluna `.rail`.

### Dialogos, Modais e Acoes Criticas

- **Acessibilidade e teclado**:
  - Ao abrir um dialogo (nativo `<dialog>`, CDK Dialog/Overlay ou componente modal do projeto), mova o foco para o primeiro controle interativo ou titulo, retenha a tabulacao dentro do dialogo (`cdkTrapFocus` ou equivalente), permita fechar com `Escape` e restaure o foco ao botao de origem ao fechar.
- **Prevencao de acoes duplicadas e confirmacao**:
  - Acoes destrutivas (`.btn.d`, exclusao, cancelamento) ou mudancas irreversiveis de status exigem confirmacao explicita e desabilitacao imediata do botao enquanto a requisicao estiver pendente (`[disabled]="salvando()"`).

## 2. Checklist de Revisao Visual e Acessibilidade

Use esta lista antes de concluir a criacao ou revisao de qualquer tela ou componente:

1. **Consulta Previa e Consistencia Visual**:
   - [ ] Os tokens de `docs/design/DESIGN.md` e `frontend/src/styles/` foram consultados e reutilizados sem cores hexadecimais ou `rgb()` avulsas?
   - [ ] A tipografia respeita `Sora` (200 a 300), `DM Sans` (400 a 500) e `IBM Plex Mono` (400 a 500), sem `font-weight` acima de `500` e sem `box-shadow` em caixas?
   - [ ] O botao primario (`.btn.p`) usa fundo `--ink` com texto `--bg` (sem usar `--amber` nem gradiente como fundo de botao)?
   - [ ] A estrutura de elementos, classes e textos fixos bate com o prototipo em `docs/design/prototipo/` e com `e2e/conformidade/esqueleto.ts`?
2. **Responsividade (Desktop e Mobile)**:
   - [ ] A tela renderiza corretamente em `1280px` (desktop) e em `360px`, `390px` e `480px` (mobile) sem rolagem horizontal?
   - [ ] Menus, filtros, grades de cards, coluna lateral (`.rail`) e barra de acoes (`.actionbar`) adaptam-se adequadamente nos pontos de quebra (`1100px`, `640px`, `480px`)?
3. **Acessibilidade e Navegacao por Teclado**:
   - [ ] Todos os controles de formulario possuem `<label for="...">` ou `aria-label`, e grupos de radio customizados possuem `role="radiogroup"`?
   - [ ] Toda interacao pode ser executada apenas com teclado (`Tab`, `Shift+Tab`, `Enter`, `Espaco`, `Escape`) com foco visivel (`:focus-visible`)?
   - [ ] O contraste entre texto e superficie atende ao padrao WCAG A/AA (validado pelo axe nos testes E2E)?
   - [ ] Indicadores de status (`.st.*`) apresentam ponto colorido + texto, sem depender exclusivamente da cor?
4. **Estados Visuais e Prevencao de Duplicidade**:
   - [ ] Existem estados claros para carregamento (`carregando`), lista vazia (`vazio`) e erro (`erroGeral` / `errosCampo` com `role="alert"` e `aria-invalid`)?
   - [ ] Os botoes de submissao e acao ficam desabilitados (`[disabled]="salvando()"`) durante requisicoes em voo para impedir cliques e envios duplicados?
5. **Respeito aos Limites de Escopo**:
   - [ ] Nenhuma regra de negocio de dominio, contrato de API (`core/models/`, endpoints) ou fluxo de autorizacao (`guards`, `interceptors`) foi modificado indevidamente?
