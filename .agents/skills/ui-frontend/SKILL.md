---
name: ui-frontend
description: >-
  Orienta a criacao e a revisao da camada visual do frontend Angular.
  Use esta skill ao criar, alterar ou revisar telas, componentes com Angular Material/CDK,
  tabelas, dialogos, formularios, navegacao, responsividade, acessibilidade e estados visuais.
---

# UI Frontend (Angular)

Runbook para criacao e revisao da camada visual do frontend Angular (`frontend/`), cobrindo telas da loja (`storefront`) e do painel administrativo (`admin`), componentes baseados em Angular Material/CDK ou HTML semantico estilizado, tabelas, dialogos, formularios, navegacao, responsividade (desktop e mobile), acessibilidade (WCAG A/AA e teclado) e estados visuais.

Para o guia detalhado de componentes, tokens, acessibilidade e checklist de revisao, consulte [padroes-visuais-acessibilidade.md](./references/padroes-visuais-acessibilidade.md).

## 1. Consulta Obrigatoria aos Padroes Visuais Existentes

Antes de criar qualquer componente, tela, dialogo ou regra de estilo (`.scss`), inspecione obrigatoriamente os artefatos existentes para reutilizar padroes e manter consistencia visual:

1. **Guias e Design System**:
   - Leia [`AGENTS.md`](../../../AGENTS.md) na raiz e [`frontend/AGENTS.md`](../../../frontend/AGENTS.md).
   - Leia [`docs/design/DESIGN.md`](../../../docs/design/DESIGN.md) para conferir tokens de cor, tipografia, raios de borda, espacamentos e classes de componentes.
   - Inspecione o HTML e o CSS de referencia da tela em `docs/design/prototipo/` (`assets/site.css` para loja e `assets/admin.css` para painel).
2. **Estilos, layouts e componentes compartilhados no codigo**:
   - Verifique os estilos globais e tokens em `frontend/src/styles/` e `frontend/src/styles.scss`.
   - Verifique os componentes, diretivas e pipes ja existentes em `frontend/src/app/shared/` e os containers em `frontend/src/app/layout/` antes de criar novos elementos visuais.
   - Consulte telas semelhantes ja entregues no mesmo modulo (`frontend/src/app/admin/` ou `frontend/src/app/storefront/`) para manter a mesma estrutura de template e sinais reativos.
3. **Verificacao de conformidade E2E**:
   - Consulte `frontend/e2e/conformidade/telas.ts` e `frontend/e2e/conformidade/esqueleto.ts` para garantir que a hierarquia de seletores (`.crumb`, `h1`, `.sh > h2`, `.lbl`, `.btn`, `aside.rail > section.card`, `.actionbar` e colunas de tabela) permaneca identica ao prototipo.

## 2. Diretrizes de Interface, Componentes e Consistencia Visual

1. **Tokens e Tema Unico ("Noite + Campo")**:
   - Utilize exclusivamente as variaveis CSS do projeto (`--bg`, `--bg2`, `--surf`, `--surf2`, `--field`, `--ink`, `--ink2`, `--mute`, `--faint`, `--line`, `--line2`, `--amber`, `--ok`, `--warn`, `--info`).
   - Nao utilize cores hexadecimais ou `rgb()` soltas nos componentes, `box-shadow` em caixas (a separacao ocorre por borda `--line` e tom de superficie), peso tipografico acima de `500` ou `--amber` como fundo de botao primario (`.btn.p` usa fundo `--ink` e texto `--bg`).
2. **Angular Material / CDK e Componentes Nativos**:
   - Ao usar utilitarios do Angular CDK (FocusTrap, Overlay, Dialog, Portal, A11y) ou componentes Material, aplique os tokens e classes do Design System para nao introduzir estilos externos conflitantes nem quebrar a verificacao de `esqueleto.ts`.
   - Prefira elementos HTML semanticos (`<table>`, `<dialog>`, `<nav>`, `<main>`, `<aside>`, `<fieldset>`) combinados com as classes padrao do projeto.
3. **Tabelas, Dialogos, Formularios e Navegacao**:
   - **Tabelas**: mantenha exatamente as colunas e a ordem do prototipo, cabecalhos `<th scope="col">`, alinhamento consistente para valores numericos/monetarios e suporte responsivo sem estourar a largura da viewport.
   - **Dialogos e Confirmacoes**: exiba titulo claro, descricao da consequencia (especialmente em acoes destrutivas `.btn.d`), foco inicial controlado, fechamento via `Escape` e retorno do foco ao elemento acionador.
   - **Formularios**: use `ReactiveFormsModule` (`FormBuilder.nonNullable.group`), mascaras/pipes de formatacao (`cep`, `cnpj`, `telefone`, `mascara.directive.ts`), agrupamento em cards e barra de acoes (`.actionbar`) ou coluna lateral (`.rail`) com card de pendencias (`.card.pend`) nos cadastros com rascunho.
   - **Navegacao**: mantenha breadcrumbs (`.crumb`), menu principal, abas de modulo (`.tb`) e filtros laterais/gaveta mobile sincronizados com a rota ativa (`RouterLink`, `RouterLinkActive`).

## 3. Responsividade e Acessibilidade (Desktop e Mobile)

1. **Desktop e Mobile sem Rolagem Horizontal (D20)**:
   - Valide o layout em desktop (`1280px`) e em viewports moveis (`360px`, `390px` e `480px`), respeitando os pontos de quebra do design (`1100px`, `640px` e `480px`).
   - Nenhuma tela, tabela, formulario ou barra de acoes pode gerar overflow horizontal (`scrollWidth > clientWidth`) em `390px`.
2. **Navegacao por Teclado**:
   - Todos os elementos interativos (botoes, links, filtros, abas, opcoes `.choice`/`.rad`, paginacao e controles de dialogo) devem ser alcancaveis via `Tab`/`Shift+Tab` em ordem logica e acionaveis por `Enter` ou `Espaco`.
   - Preserve indicadores visiveis de foco (`:focus-visible` com destaque em `--amber` ou `--line2`) e gerencie o foco ao abrir/fechar dialogos ou menus moveis.
3. **Labels, Contraste e Semantica (WCAG A/AA)**:
   - Todo campo de formulario (`input`, `select`, `textarea`) deve possuir `<label for="...">` explicito ou `aria-label` / `aria-labelledby`.
   - Grupos de selecao unica customizados devem declarar `role="radiogroup"` e opcoes acessiveis; status visuais (`.st.ok`, `.st.warn`, `.st.amber`, `.st.info`, `.st.mute`) nunca dependem apenas de cor, exibindo sempre ponto colorido + texto descritivo.
   - Garanta contraste adequado usando os pares de tokens aprovados (`--ink` e `--ink2` sobre `--bg`, `--bg2`, `--surf` e `--field`).

## 4. Estados Visuais e Prevencao de Acoes Duplicadas

Toda tela ou componente interativo deve tratar explicitamente seus estados visuais:

1. **Carregamento (`carregando`)**:
   - Exiba feedback visual claro durante a busca inicial ou atualizacao de dados e marque a regiao com `[attr.aria-busy]="carregando()"`, evitando saltos bruscos de layout.
2. **Estado Vazio (`vazio`)**:
   - Quando uma listagem, busca ou tabela nao retornar itens, apresente mensagem orientativa clara (sem exibir tabela quebrada ou area em branco sem explicacao).
3. **Mensagens de Erro e Validacao (`erroGeral` e `errosCampo`)**:
   - Mapeie erros `ProblemDetail` da API (`err.error as ProblemDetail`) preenchendo `errosCampo` (`problem.errors`) e `erroGeral` (`problem.detail`).
   - Nos controles invalidos, aplique `[attr.aria-invalid]="!!mensagemErro('campo')"` e vincule a mensagem exibida com `role="alert"`.
4. **Prevencao de Acoes Duplicadas (`salvando` / `enviando`)**:
   - Controle o estado de submissao com signal booleano (`salvando = signal(false)`).
   - Enquanto uma requisicao de criacao, edicao, exclusao ou mudanca de status estiver em andamento (` finalize(() => this.salvando.set(false))`), bloqueie novas submissoes no metodo e aplique `[disabled]="salvando()"` nos botoes de acao para impedir cliques duplos e requisicoes concorrentes.

## 5. Limites Estritos (O Que Nao Modificar)

Ao atuar com esta skill na camada visual, respeite rigorosamente os seguintes limites:

- **Nao modificar regras de negocio**: nao altere calculos de dominio, maquinas de estado, regras de rascunho/publicacao (`D21`, `D28`) nem crie campos visuais que nao existam no modelo/contrato (`D36`, `D40`).
- **Nao modificar contratos de API**: nao altere caminhos de endpoints, verbos HTTP, query params ou estrutura dos tipos de requisicao/resposta em `core/models/*.model.ts` e `core/services/*.service.ts` fora do contrato especificado na issue (`docs/issues/NNN.md` e `docs/API.md`).
- **Nao modificar fluxos de autorizacao e seguranca**: nao altere nem contorne `guards` (`core/guards/`), `auth.interceptor.ts`, verificacoes de perfil/permissao de modulo ou regras de `SECURITY.md` (proibido usar `bypassSecurityTrust*` ou `[innerHTML]` nao sanitizado).
- **Nao afrouxar testes de conformidade ou unitarios**: nao remova seletores nem desative verificacoes em `frontend/e2e/conformidade/` ou `*.spec.ts` para forcar a aprovacao da build.

## 6. Passo a Passo de Execucao e Verificacao

1. **Inspecionar**: leia o contrato da issue (`docs/issues/NNN.md`), o prototipo correspondente em `docs/design/prototipo/` e os componentes/estilos existentes em `frontend/src/`.
2. **Implementar ou revisar**:
   - Construa ou revise o template (`.html`), estilos (`.scss`) e logica de estado visual (`.ts` usando `signal`, `computed`, `@if`, `@for`, `inject()`).
   - Confira todos os itens do checklist em [padroes-visuais-acessibilidade.md](./references/padroes-visuais-acessibilidade.md).
3. **Executar validacoes automatizadas**:
   ```bash
   cd frontend && npx ng test --watch=false
   cd frontend && npm run build
   cd frontend && npm run prototipo:verificar
   cd frontend && npm run e2e -- --grep @issue-<numero>
   ```
4. **Registrar evidencias**: liste os arquivos criados ou revisados, relate o resultado dos testes unitarios, build e E2E (incluindo axe WCAG A/AA e ausencia de overflow em `390px`) e, em revisoes, classifique os pontos em **Bloqueante**, **Sugestao** ou **Pergunta**.
