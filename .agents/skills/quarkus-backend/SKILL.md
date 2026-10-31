---
name: quarkus-backend
description: >-
  Orienta a implementacao e a revisao de funcionalidades backend em Java com Quarkus.
  Use esta skill ao criar, alterar ou revisar endpoints REST, Resources, Services,
  Repositories, DTOs, Mappers, validacoes, contratos HTTP, regras de negocio e transacoes.
---

# Quarkus Backend

Runbook para implementacao e revisao de funcionalidades backend em Java com Quarkus, seguindo a arquitetura em camadas (`Resource -> Service -> Repository -> Entity`), separacao por modulo de dominio e uso estrito de DTOs nos contratos HTTP.

Para exemplos detalhados de implementacao de cada camada e checklist de revisao, consulte [padroes-arquitetura.md](./references/padroes-arquitetura.md).

## 1. Inspecao Obrigatoria Antes de Alterar Arquivos

Antes de criar ou editar qualquer arquivo no backend, execute obrigatoriamente esta sequencia de leitura:

1. **Guias e regras do repositorio**:
   - Leia [`AGENTS.md`](../../../AGENTS.md) na raiz e [`backend/AGENTS.md`](../../../backend/AGENTS.md).
   - Leia [`SECURITY.md`](../../../SECURITY.md) para requisitos de autenticacao, autorizacao e protecao de dados.
2. **Documentacao de contexto e dominio**:
   - Consulte [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md) para entender o estado atual e as decisoes arquiteturais.
   - Consulte [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) para identificar as regras aprovadas (`[IMPLEMENTADO]` e `[PENDENTE]`).
   - Consulte [`docs/modelo.plantuml`](../../../docs/modelo.plantuml) e os diagramas em `docs/diagramas/` para conferir entidades, tipos, enums e relacionamentos.
   - Consulte [`docs/API.md`](../../../docs/API.md) e o contrato especifico da issue em `docs/issues/NNN.md`.
3. **Inspecao do modulo alvo no codigo**:
   - Liste e leia as classes existentes do modulo (`br.com.droneshop.<modulo>`) e os testes correspondentes em `backend/src/test/java/br/com/droneshop/<modulo>/` antes de propor mudancas.
   - Identifique o que ja esta aprovado no contrato e se ha decisoes pendentes ou ambiguidades. Se houver lacuna ou contradicao no contrato, pare e pergunte ao mantenedor antes de implementar.

## 2. Arquitetura em Camadas e Separacao por Dominio

Toda funcionalidade deve respeitar o fluxo unidirecional `Resource -> Service -> Repository -> Entity` dentro do seu modulo de dominio:

- **Resource (`resource/`)**:
  - Camada fina de entrada/saida HTTP (`@Path`, `@GET`, `@POST`, `@PUT`, `@PATCH`, `@DELETE`, `@Produces`, `@Consumes`).
  - Recebe `XxxRequestDTO` (ou parametros de rota/query) e retorna exclusivamente `XxxResponseDTO`, `PageResponse<XxxResponseDTO>` ou `void` (`204 No Content`).
  - **Regra inviolavel**: nunca retorne nem receba entidades JPA diretamente pela API. Toda conversao ocorre via `Mapper` ou no `Service`.
  - Nao contem regra de negocio, acesso direto a `Repository` nem controle manual de transacao.
- **Service (`service/`)**:
  - Contem as regras de negocio, validacoes de dominio, verificacao de integridade/conflitos e demarcacao transacional (`@Transactional` em metodos de escrita).
  - Orquestra chamadas a repositories do proprio modulo ou de outros modulos necessarios.
  - Converte falhas de regra de negocio nas excecoes padronizadas (`ValidationException` para `400`, `ResourceNotFoundException` para `404`, `ConflitoException` para `409`), retornadas no formato RFC 7807/9457 (`Problem`). Nunca use HTTP `422`.
- **Repository (`repository/`)**:
  - Implementa `PanacheRepository<Entidade>` (`@ApplicationScoped`).
  - Encapsula consultas HQL/JPQL sempre com parametros nomeados ou posicionais (`?1`, `:param`). Nunca concatene strings vindas do usuario.
- **Entity (`model/`)**:
  - Representa o modelo persistido, herdando de `DefaultEntity`.
  - Enums de dominio possuem `id` e `nome`, conversor JPA em `model/converterjpa/` e rota publica de leitura quando previsto no contrato.
- **DTO e Mapper (`dto/` e `mapper/`)**:
  - `record XxxRequestDTO` com Bean Validation (`@NotNull`, `@Length`, etc.) e normalizacao de texto no construtor compacto quando aplicavel.
  - `record XxxResponseDTO` refletindo exatamente o JSON do contrato da issue (`idXxx` na entrada; objetos aninhados e `{id, nome}` na saida).
  - `XxxResponseMapper` estatico para mapear `Entity -> ResponseDTO`.

## 3. Autorizacao e Isolamento de Dados (Tenants / Contas / Perfis)

Toda rota e operacao de servico deve validar explicitamente quem executa a acao e quais registros podem ser acessados:

1. **Rotas administrativas**:
   - Devem declarar `@RolesAllowed("ADMINISTRATIVO")` e `@ModuloAdmin(ModuloSistema.XXX)` correspondente ao modulo para acionar o filtro de permissao por modulo (`ver`, `editar`, `excluir`).
2. **Isolamento entre contas/tenants e rotas de cliente (`/conta/**`)**:
   - O identificador do titular, organizacao/tenant ou cliente logado deve ser extraido exclusivamente do contexto de seguranca (`JsonWebToken` / `SecurityContext`), nunca aceito via path param, query param ou corpo da requisicao.
   - Toda consulta, alteracao ou exclusao de recurso pertencente a um cliente ou tenant deve filtrar pelo identificador autenticado no banco (impedindo IDOR / acesso cruzado entre contas).
3. **Rastreabilidade de autoria**:
   - Em movimentos de estoque manuais, mudancas manuais de status e auditoria, o `autor` vem sempre do token autenticado. Em rotinas de sistema ou webhooks (`SISTEMA`, `INTEGRACAO`), o `autor` e nulo.
4. **Rotas publicas (`/loja/**`)**:
   - Nunca expoem dados internos (custo de aquisicao, margem, estoque minimo, dados de outros clientes ou operadores) nem registros em status `RASCUNHO` ou `INATIVO`.

## 4. Limites Estritos (O Que Nao Fazer Sem Aprovacao Explicita)

O agente **nao deve**, sob nenhuma hipotese, realizar as acoes abaixo sem pedido ou aprovacao explicita do mantenedor:

- **Nao inventar endpoints, rotas, parametros ou campos de DTO** que nao estejam previstos no contrato da issue (`docs/issues/NNN.md`) ou em `docs/API.md`.
- **Nao criar nem alterar entidades JPA, colunas, tabelas, enums ou relacionamentos** fora do que esta especificado no diagrama de classes e no contrato da issue.
- **Nao criar nem editar migrations ou scripts de schema/carga (`import.sql`, `localidades.sql`)** sem que a mudanca de modelo faja parte do escopo aprovado.
- **Nao alterar regras de negocio existentes** nem remover validacoes de seguranca ou de conflito (`409`) para contornar falhas.
- **Nao afrouxar nem desativar testes existentes** em `backend/src/test` para fazer a suite passar.

## 5. Passo a Passo de Execucao e Registro de Evidencias

Ao implementar ou revisar uma tarefa com esta skill:

1. **Mapear o escopo**: confirme os endpoints, DTOs, regras e criterios de aceite na issue e em `docs/REGRAS_NEGOCIO.md`.
2. **Implementar ou revisar camada por camada**:
   - Verifique `Model` -> `Repository` -> `DTO` / `Mapper` -> `Service` (`@Transactional`, validacoes, conflitos) -> `Resource` (seguranca, status HTTP `200`/`204`, `PageResponse`).
   - Use o checklist em [padroes-arquitetura.md](./references/padroes-arquitetura.md) para validar cada arquivo.
3. **Executar os testes automatizados**:
   - Rode os testes especificos da issue ou classe alterada:
     ```bash
     cd backend && ./mvnw test -Dgroups=issue-<numero>
     ```
   - Rode a verificacao das issues entregues para garantir ausencia de regressao:
     ```bash
     cd backend && ./mvnw verify
     ```
4. **Sincronizar a documentacao obrigatoria**:
   - Se uma regra `[PENDENTE]` foi implementada, atualize `docs/REGRAS_NEGOCIO.md` para `[IMPLEMENTADO]`.
   - Se o modelo mudou (com aprovacao), atualize `docs/modelo.plantuml` e o `.puml` do modulo em `docs/diagramas/`.
   - Se uma issue foi concluida, atualize `docs/CONTEXTO_PROJETO.md` e `backend/src/test/issues-entregues.txt`.
5. **Registrar evidencias na resposta**:
   - Liste os arquivos criados/alterados com links diretos.
   - Informe os comandos de teste executados, a quantidade de testes rodados e o resultado obtido.
   - Em revisoes de PR, classifique cada apontamento como **Bloqueante**, **Sugestao** ou **Pergunta**, declarando explicitamente se nao houver itens bloqueantes.
