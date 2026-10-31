---
name: testes-backend
description: >-
  Orienta a criacao, revisao e execucao de testes de backend Java/Quarkus.
  Use esta skill ao criar, revisar ou executar testes unitarios, de integracao,
  API com REST Assured, persistencia, autorizacao, isolamento entre tenants e contas,
  concorrencia e analise de regressoes, exigindo PostgreSQL real via Dev Services
  e proibindo declarar testes como aprovados sem executa-los.
---

# Testes de Backend (Java / Quarkus)

Runbook para criacao, revisao e execucao de testes de backend Java 25 / Quarkus 3 com JUnit 5, REST Assured e PostgreSQL 16 real (via Dev Services local ou servico do CI). Prioriza o **comportamento observavel** da aplicacao e exige que **somente resultados realmente executados sejam reportados**.

Para padroes de escrita, uso de utilitarios (`Api`, `Fixtures`, `Dados`), dubles de integracao e checklist detalhado de cobertura, consulte [padroes-e-cenarios.md](./references/padroes-e-cenarios.md).

## 1. Regra de Ouro: Execucao Real Obrigatoria

1. **Nunca declare testes como aprovados sem executa-los**:
   - Escrever, revisar ou inspecionar estaticamente o codigo de teste **nao** substitui a execucao.
   - Um teste so pode ser reportado como "aprovado" ou "passando" se o comando Maven (`./mvnw test` ou `./mvnw verify`) tiver sido efetivamente executado nesta sessao e retornado `BUILD SUCCESS` com `Failures: 0, Errors: 0`.
2. **Transparencia quando a execucao nao for possivel**:
   - Se o Docker / Dev Services ou o banco PostgreSQL nao estiver disponivel no ambiente, informe explicitamente o erro obtido e declare que os testes **foram escritos/revisados, mas nao puderam ser executados**.
3. **Proibido afrouxar testes existentes**:
   - Os testes em `backend/src/test` refletem o contrato fechado de cada issue (`docs/issues/NNN.md`). Nunca remova assercoes, mude codigos HTTP esperados nem adicione `@Disabled` para mascarar falhas de implementacao.

## 2. Inspecao Obrigatoria Antes de Criar ou Alterar Testes

Antes de criar ou editar qualquer teste em `backend/src/test`, leia:

1. **Guias e matriz de testes**:
   - [`AGENTS.md`](../../../AGENTS.md), [`backend/AGENTS.md`](../../../backend/AGENTS.md), [`SECURITY.md`](../../../SECURITY.md) e [`backend/src/test/README.md`](../../../backend/src/test/README.md).
2. **Contrato e regras de negocio**:
   - [`docs/API.md`](../../../docs/API.md), [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) e a issue correspondente em `docs/issues/NNN.md`.
3. **Infraestrutura de testes existente**:
   - [`Api.java`](../../../backend/src/test/java/br/com/droneshop/testes/Api.java) (`Api.given()`, `Api.anonimo()`, `Api.como(token)`, `Api.carrinho(...)`).
   - [`Fixtures.java`](../../../backend/src/test/java/br/com/droneshop/testes/Fixtures.java) e [`Dados.java`](../../../backend/src/test/java/br/com/droneshop/testes/Dados.java) (criacao de massa de dados isolada via API com sufixos unicos e documentos validos).
   - Dubles locais (D34): `ViaCepFalso`, `MercadoPagoFalso`, `FreteFalso`, `ResendFalso`, `GoogleFalso`. Nenhum teste pode depender de rede externa.
   - Testes existentes do mesmo modulo em `backend/src/test/java/br/com/droneshop/<modulo>/`.

## 3. Tipos de Teste e Foco em Comportamento Observavel

Priorize sempre o que pode ser observado pela fronteira publica (status HTTP, payload JSON de resposta, cabecalhos, estado subsequente via `GET` ou banco real), evitando acoplar testes a detalhes internos de implementacao:

- **Testes de API e Integracao (`@QuarkusTest` + REST Assured)**:
  - Exercitam o fluxo completo `Resource -> Service -> Repository -> PostgreSQL`.
  - Toda classe ou metodo de teste de issue deve ter `@Tag("issue-<numero>")` (ou `@Tag("base")`).
  - Como o banco de teste inicia com `import.sql` e e compartilhado entre os testes da execucao, **nunca dependa da contagem total da tabela** sem filtrar pelos registros criados no proprio teste (use `Dados.sufixo()`).
- **Testes Unitarios Puros (JUnit 5 sem `@QuarkusTest`)**:
  - Usados para utilitarios, geradores (ex.: `CsvTest`, `EtiquetaPedidoServiceTest`), calculos isolados e formatadores onde o container CDI/banco nao agrega valor.
- **Testes de Persistencia e Transacao**:
  - Verificam que operacoes validas persistem todas as alteracoes esperadas e que falhas no meio da operacao (`400`, `404`, `409`) fazem rollback completo sem deixar efeitos colaterais no banco.
  - Quando um teste precisar preparar ou limpar estado diretamente via JPA fora da API (ex.: simular vinculo historico em `PromocaoResourceTest`), use `QuarkusTransaction.requiringNew().run(...)` e limpe os dados no bloco `finally`.

## 4. Dimensoes Obrigatorias de Cobertura

Ao criar ou revisar testes de uma funcionalidade, garanta cobertura explicita para as dimensoes aplicaveis (detalhes e exemplos em [padroes-e-cenarios.md](./references/padroes-e-cenarios.md)):

1. **Sucesso e Contrato HTTP**:
   - `GET` e `POST` retornam `200 OK` com o JSON exato do contrato (`PageResponse` nas listagens; enums como `{id, nome}`; objetos aninhados na saida).
   - `PUT`, `PATCH` (sem corpo) e `DELETE` retornam `204 No Content`, seguidos de `GET` comprovando que a mudanca persistiu (ou que o item removido agora retorna `404`).
2. **Erros e Validacoes (`400`, `404`, `409`, nunca `422`)**:
   - **Validacao (`400 Bad Request`)**: formato RFC 7807/9457 (`Problem`), validando `errors.field` com o nome exato do campo no JSON e mensagens terminadas em `(back).`. Em cadastros com `RASCUNHO` (D21, D28), testar que o rascunho aceita campos vazios e que a publicacao (`ATIVO`/`INATIVO`) cobra todos os obrigatorios.
   - **Nao encontrado (`404 Not Found`)**: consulta, edicao, exclusao ou acao sobre `Dados.ID_INEXISTENTE`.
   - **Conflito (`409 Conflict`)**: duplicidade de chave unica (SKU, e-mail, CNPJ, slug) ou violacao de estado.
3. **Autenticacao e Autorizacao (`401` e `403`)**:
   - Requisicao sem token (`Api.anonimo()`) em rota protegida deve responder `401`.
   - Cliente autenticado tentando acessar rota administrativa (`/admin/**`, `/marcas`, etc.) deve responder `403`.
   - Administrador com papel restrito (`PAPEL_SOMENTE_LEITURA`, `PAPEL_CATALOGO`, `PAPEL_OPERADOR`, `PAPEL_ATENDIMENTO`) tentando executar acao ou acessar modulo sem permissao (`ver`, `editar`, `excluir`) deve responder `403`.
   - Autoprotecao administrativa: usuario nao pode desativar a propria conta, alterar o proprio papel nem remover a propria permissao (`409`).
4. **Isolamento entre Tenants / Contas / Clientes**:
   - Em rotas de conta (`/conta/**`), pedidos, enderecos, cartoes, favoritos, drones ou chamados, um titular (`Cliente A`) **nunca** pode ler, alterar ou excluir recursos de outro titular (`Cliente B`), recebendo `404` (ou `403` conforme o contrato da issue) e mantendo o recurso de `B` intacto.
   - Verificar que o backend ignora ou rejeita tentativas de forcar `idCliente`/`idUsuario` no corpo ou query param, usando exclusivamente o titular do JWT.
5. **Referencias Cruzadas e Integridade Referencial**:
   - Referenciar `idMarca`, `idSubcategoria`, `idFornecedor`, `idProduto` ou `idMunicipio` inexistente responde `400` (em validacao de campo) ou `404` conforme o contrato.
   - Coerencia entre tipo e categoria (D39): drone em categoria de acessorio (ou vice-versa) deve ser rejeitado com `400`.
   - Exclusao de registro em uso (D24): tentar excluir marca, categoria, subcategoria, fornecedor, produto ou promocao vinculada responde `409 Conflict` informando a contagem, sem apagar nada em cascata.
6. **Transacoes, Conflitos de Agenda / Vigencia e Maquinas de Estado**:
   - **Transicoes de status (D30)**: testar transicoes validas e garantir que transicoes invalidas (ex.: `ENTREGUE -> PAGO`, `CANCELADO -> *`) respondem `409` e **nao** alteram o status atual nem geram historico indevido.
   - **Agenda, janelas de vigencia e expiracao**: testar promocoes e cupons em estados `AGENDADA`, `VIGENTE`, `ENCERRADA` e `INATIVA` (`dataFim < dataInicio` -> `400`), janelas de reserva de estoque (`expiraEm`, rotina de expiracao devolvendo saldo) e conflitos de sobreposicao quando previstos na regra.
7. **Historico, Auditoria e Imutabilidade**:
   - **Historico e rastreabilidade (D11)**: mudancas de status de pedido e movimentos de estoque devem gravar historico em ordem cronologica com `origem` (`MANUAL`, `SISTEMA`, `INTEGRACAO`), `autor` preenchido quando manual (extraido do token) e `saldoResultante` / `statusAnterior` -> `statusNovo`.
   - **Imutabilidade**: registros historicos (movimentos de estoque, historico de status, usos de cupom, auditoria) nao sao editaveis nem removiveis; editar o endereco do cliente no cadastro nao altera o `enderecoEntrega` congelado em pedidos ja criados (D12, RN15).
8. **Concorrencia e Disputa de Recursos**:
   - Em operacoes sujeitas a disputa (reserva/baixa das ultimas unidades de estoque, uso simultaneo de cupom com limite de usos, cliques repetidos de reserva/pagamento), validar:
     - **Idempotencia**: chamar `POST /carrinho/reservar` duas vezes para o mesmo carrinho nao duplica a reserva.
     - **Disputa entre sessoes/clientes**: quando dois carrinhos ou threads disputam saldo insuficiente (ex.: estoque `2`, dois pedidos/reservas de `2`), apenas um tem sucesso (`200`) e o outro recebe `409 Conflict`, nunca deixando `estoqueFisico` ou `disponivel` negativo.
     - Quando testar concorrencia paralela real (`ExecutorService` + `CountDownLatch`), sincronize a largada das threads e valide ao final a soma exata de sucessos, falhas `409` e o saldo persistido no banco.

## 5. Fluxo de Execucao e Analise de Regressoes

Siga esta sequencia ao executar testes e analisar regressoes:

1. **Executar os testes da issue ou classe alvo**:
   ```bash
   cd backend && ./mvnw test -Dtest=NomeDaClasseTest
   cd backend && ./mvnw test -Dgroups=issue-<numero>
   ```
2. **Executar a suite de regressao das entregas bloqueantes**:
   - As etiquetas listadas em `backend/src/test/issues-entregues.txt` bloqueiam o merge no CI.
   - Rode a verificacao completa:
     ```bash
     cd backend && ./mvnw verify
     ```
3. **Diagnosticar falhas e regressoes**:
   - Se um teste de outra issue ja entregue quebrar, **nao altere o teste antigo para passar**. Inspecione o log do Surefire (`backend/target/surefire-reports/`), identifique qual mudanca de codigo ou efeito colateral de massa de dados no banco causou a regressao e corrija a causa.
   - Se a falha decorrer de sujeira deixada por outro teste no banco (ex.: pedido criado sem itens que afeta `Fixtures.pedidoDaCarga()`), isole a massa de dados ou limpe os registros no `finally`.
4. **Atualizar matriz e travas de entrega (quando aplicavel)**:
   - Se novos testes ou `@Tag` foram adicionados, atualize a matriz em `backend/src/test/README.md` (`python scripts/vincular_testes_issues.py --matriz`).
   - Ao concluir a entrega de uma issue de backend, adicione `issue-<numero>` em `backend/src/test/issues-entregues.txt`.

## 6. Formato Obrigatorio de Relato de Resultados

Toda resposta após criar, revisar ou rodar testes deve informar com precisao:

- **Arquivos de teste criados ou alterados**: links diretos para as classes e metodos.
- **Comandos executados**: comando exato rodado no terminal.
- **Resultado real da execucao**:
  - Quantidade de testes executados (`Tests run: X, Failures: Y, Errors: Z, Skipped: W`).
  - Status final (`APROVADO` somente se executado com 0 falhas/erros; `FALHOU` com o resumo da falha; ou `NAO EXECUTADO` com o motivo do bloqueio de ambiente).
- **Cobertura verificada**: resumo objetivo de quais cenarios (sucesso, validacao `400`, `401`/`403`, isolamento, `404`, conflito `409`, historico/imutabilidade, concorrencia) foram exercitados.
