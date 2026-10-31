---
name: testes-frontend
description: >-
  Orienta a criacao, revisao e execucao de testes do frontend Angular (Vitest e Playwright).
  Use esta skill ao criar, alterar ou revisar testes de componentes, servicos HTTP, formularios,
  rotas, guards, interceptores, sessao, fluxos de erro e regressoes no frontend.
---

# Testes Frontend Angular

Runbook para criacao, revisao e execucao de testes automatizados no frontend Angular 22 (`frontend/`), cobrindo testes unitarios e de integracao de componentes/servicos com **Vitest** (`ng test`) e testes E2E de conformidade com **Playwright** (`npm run e2e`).

Para exemplos praticos de implementacao por artefato e matriz detalhada de cenarios, consulte [padroes-testes-frontend.md](./references/padroes-testes-frontend.md).

## 1. Principios Inegociaveis

1. **Priorize comportamento observavel**:
   - Teste o que o usuario ve, interage ou o que sai/entra pelas fronteiras do sistema (DOM renderizado, textos, atributos de acessibilidade `role="alert"` / `aria-invalid`, navegacao no `Router`, requisicoes em `HttpTestingController`, armazenamento em `localStorage`/`sessionStorage`, downloads disparados).
   - Evite acoplar testes a detalhes internos privados quando o efeito puder ser verificado pelo DOM ou pela fronteira publica do componente/servico.
2. **Inspecione os testes existentes antes de escrever ou revisar**:
   - Antes de criar ou editar qualquer `*.spec.ts`, leia os testes ja presentes em `frontend/src/app/` e `frontend/e2e/` para manter o mesmo estilo de configuracao do `TestBed`, mocks do Vitest (`vi.fn()`, `vi.spyOn()`, `vi.stubGlobal()`), helpers de armazenamento e assercoes.
   - Leia tambem [`AGENTS.md`](../../../AGENTS.md), [`frontend/AGENTS.md`](../../../frontend/AGENTS.md), [`SECURITY.md`](../../../SECURITY.md) e [`frontend/e2e/README.md`](../../../frontend/e2e/README.md).
3. **Execucao obrigatoria e registro de resultados reais**:
   - **Nunca declare cobertura, sucesso ou aprovacao sem evidencia real de execucao**.
   - Todo teste criado, alterado ou revisado deve ser efetivamente executado no terminal.
   - Registre na resposta o comando exato executado, a quantidade de arquivos/testes rodados e o resultado real obtido (passou/falhou). Se algum ambiente impedir a execucao, declare explicitamente o bloqueio e nao de a tarefa como aprovada.
4. **Proibido afrouxar testes existentes**:
   - Nunca remova assercoes, desative specs (`it.skip`, `describe.skip`) nem afrouxe verificacoes em `frontend/src` ou `frontend/e2e/conformidade` para fazer a suite passar. Quando um teste falhar por bug na aplicacao, corrija o codigo da aplicacao.

## 2. Inspecao Obrigatoria Antes de Implementar ou Revisar

Siga esta sequencia antes de tocar em qualquer arquivo:

1. **Contrato e regras da issue**:
   - Consulte `docs/issues/NNN.md`, `docs/API.md` e `docs/REGRAS_NEGOCIO.md` para confirmar URLs, verbos HTTP, query params, payload de envio (`idXxx`), formato de resposta, regras de rascunho (D21, D28) e erros `ProblemDetail` (D22, D30).
2. **Codigo-alvo e specs vizinhas**:
   - Leia o componente, servico, guard ou interceptor sob teste e localize specs semelhantes ja existentes em `frontend/src/app/`.
   - Verifique como `HttpTestingController`, `provideHttpClient()`, `provideHttpClientTesting()`, `provideRouter()` e `TestBed.runInInjectionContext()` sao utilizados no projeto.

## 3. Matriz Obrigatoria de Cobertura por Tipo de Artefato

Ao criar ou revisar testes, garanta a cobertura explicita dos cenarios abaixo conforme o artefato:

### 3.1. Componentes de Listagem e Visualizacao
- **Carregamento (`loading`)**: estado inicial `carregando()` verdadeiro, exibicao de indicador visual (`Carregando...`) e desabilitacao de acoes sensiveis durante a carga.
- **Renderizacao com sucesso**: exibicao correta das linhas/cards, formatacao (pipes de CNPJ, CEP, telefone, status) e paginacao/contadores.
- **Estado vazio (`empty state`)**: exibicao da mensagem de lista vazia (`Nenhum registro encontrado.`) quando a API retorna `items: []` ou quando os filtros locais nao encontram combinacoes.
- **Interacao e filtros**: busca textual, seletores de filtro, mudanca de pagina, ordenacao, selecao em lote e exportacao (ex.: CSV/PDF), verificando se mudancas de filtro reiniciam a pagina para `0`.
- **Erro de carregamento e recuperacao**: exibicao de alerta (`role="alert"`) quando a chamada falha e funcionamento do botao de tentar novamente.
- **Exclusao e conflito (`409`)**: confirmacao antes de excluir, atualizacao da lista em caso de `204` e exibicao da mensagem de `problem.detail` quando a API responde `409 Conflict` (registro em uso, D24).

### 3.2. Componentes de Formulario e Cadastro
- **Modo criacao vs. modo edicao**: leitura do parametro `:id` da rota, carga inicial do registro na edicao, tratamento de `404 Not Found` ao buscar registro inexistente e diferenca entre `POST` (criar) e `PUT` (atualizar).
- **Rascunho vs. Publicacao (D21, D28)**:
  - Inicio em `RASCUNHO` (`1`), calculo reativo de `pendencias()` no card lateral (`.card.pend`) conforme os campos sao preenchidos.
  - "Salvar rascunho" envia com `idStatus = RASCUNHO` permitindo campos vazios.
  - "Publicar" promove `RASCUNHO` para `ATIVA` (`2`) antes de enviar.
- **Validacoes e erros da API (`ProblemDetail`)**:
  - Resposta `400` com `problem.errors` (`[{ field, message }]`) preenche `errosCampo`, aplica `[attr.aria-invalid]="true"` no input correspondente e renderiza `<span class="erro" role="alert">` com a mensagem do campo, alem do `erroGeral` com `problem.detail`.
  - Resposta de erro sem `errors` por campo exibe `problem.detail` ou mensagem padrao em `erroGeral` (`role="alert"`).
- **Normalizacao e mascaras**: envio de textos com `.trim()` e documentos (CPF, CNPJ, CEP, telefone) apenas com digitos para a API.
- **Prevencao de envio duplicado**:
  - Enquanto `salvando()`, `excluindo()`, `carregando()` ou `exportando()` estiver ativo, novas chamadas ao metodo de submissao/acao devem ser ignoradas imediatamente (guarda no inicio do metodo) e os botoes na interface devem permanecer `[disabled]`.
  - Teste explicitamente disparos consecutivos antes da conclusao do `Subject`/`Observable` para provar que apenas **uma** requisicao HTTP e emitida.
- **Navegacao pos-sucesso**: redirecionamento via `Router` para a rota de listagem ou detalhe esperada apos salvar/excluir com sucesso, e liberacao do estado `salvando()` em caso de erro para permitir nova tentativa.

### 3.3. Servicos HTTP (`core/services/*.service.spec.ts`)
- **Contrato HTTP completo**:
  - Metodo HTTP exato (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
  - URL exata baseada em `${environment.apiUrl}/...`, incluindo codificacao de parametros de caminho (`encodeURIComponent`).
  - Query params (`HttpParams`): valores padrao (`page=0`, `pageSize=10`), filtros opcionais omitidos quando vazios/nulos e presentes com `.trim()` quando informados.
  - Corpo da requisicao (`req.request.body`) igual ao DTO esperado e `responseType` (`json`, `blob`) quando aplicavel.
- **Verificacao de pendencias**: sempre executar `http.verify()` no `afterEach()` para garantir que nenhuma requisicao inesperada ficou aberta.

### 3.4. Rotas, Guards, Interceptores e Sessao (`core/guards/`, `core/interceptors/`, `AuthService`)
- **Autenticacao e armazenamento de sessao**:
  - Login grava o token em `sessionStorage` por padrao e em `localStorage` quando `manterConectado = true`.
  - Validacao de expiracao (`exp`) do JWT: tokens expirados ou malformados sao removidos automaticamente do storage e `autenticado()` retorna `false`.
  - Logout chama o endpoint e limpa ambos os storages mesmo em caso de falha (`finalize`).
- **Autorizacao e Guards (`adminAuthGuard` e similares)**:
  - Permite navegacao (`true`) quando ha sessao valida e perfil/permissao exigidos.
  - Bloqueia navegacao e retorna `UrlTree` redirecionando para `/entrar` com `returnUrl` preservando a URL destino quando nao autenticado.
- **Interceptor de Autenticacao e Sessao Expirada (`authInterceptor`)**:
  - Anexa cabecalho `Authorization: Bearer <token>` **somente** para requisicoes destinadas a `environment.apiUrl` (mesma origem) e que **nao** sejam `/auth/login`.
  - Nunca vaza o token para dominios externos (ex.: consulta de CEP ou CDNs).
  - Quando a API responde `401 Unauthorized` em rota autenticada, limpa a sessao (`limparSessao()`) e redireciona para `/entrar` com `returnUrl`.
- **Seguranca na navegacao pos-login (`open redirect`)**:
  - Garanta que `returnUrl` so e seguido se comecar com `/` e nao comecar com `//`, caindo na rota padrao caso contrario.

### 3.5. Regressoes e Conformidade E2E (`frontend/e2e/`)
- Todo bug corrigido deve ganhar um teste unitario ou de integracao que reproduza o cenario exato da falha (ex.: falha em pagina intermediaria de exportacao paginada nao pode gerar arquivo parcial).
- Alteracoes em estrutura HTML, tokens CSS, colunas de tabela, rotulos ou responsividade devem passar nos testes Playwright de conformidade com o prototipo (`npm run e2e -- --grep @<etiqueta>`).

## 4. Passo a Passo de Execucao e Verificacao

1. **Inspecionar**: leia o codigo-fonte e os specs existentes do modulo.
2. **Projetar os casos de teste**: liste os cenarios cobrindo renderizacao, interacao, validacoes, HTTP, sucesso, erro, loading, vazio, navegacao, autorizacao, sessao expirada e prevencao de envio duplicado aplicaveis ao escopo.
3. **Implementar os testes**: escreva os specs em portugues, sem usar `any`, sem travessao e sem emojis, seguindo os padroes detalhados em [padroes-testes-frontend.md](./references/padroes-testes-frontend.md).
4. **Executar os testes no terminal**:
   - Testes unitarios (Vitest):
     ```bash
     cd frontend && npx ng test --watch=false
     ```
   - Build para validar tipagem estrita dos componentes e templates:
     ```bash
     cd frontend && npm run build
     ```
   - Testes E2E de conformidade quando houver impacto visual/estrutural ou de rota:
     ```bash
     cd frontend && npm run e2e -- --grep @issue-<numero>
     ```
5. **Reportar com evidencias reais**:
   - Liste os arquivos `*.spec.ts` criados ou atualizados com links clicaveis.
   - Apresente o resumo real da execucao (comandos rodados, total de suites e testes aprovados) e, em revisoes, classifique os achados em **Bloqueante**, **Sugestao** ou **Pergunta**.
