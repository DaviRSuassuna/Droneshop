---
name: persistencia-postgresql
description: >-
  Orienta alteracoes de persistencia com PostgreSQL, Hibernate ORM, JPA, Panache Repository e Flyway.
  Use esta skill para criacao ou alteracao de entidades, mapeamentos JPA, repositories,
  consultas PostgreSQL, migrations, indices, restricoes, transacoes, isolamento entre tenants e concorrencia.
---

# Persistencia PostgreSQL, JPA, Panache e Flyway

Runbook para criacao, alteracao e revisao da camada de persistencia em PostgreSQL com Hibernate ORM, Jakarta Persistence (JPA), Quarkus Panache Repository e Flyway.

Para exemplos detalhados de mapeamento JPA, consultas seguras, migrations SQL versionadas, controle de concorrencia e checklist de revisao, consulte [padroes-persistencia.md](./references/padroes-persistencia.md).

## 1. Leitura Obrigatoria Antes de Implementar

Antes de criar ou alterar qualquer entidade, repository, consulta, migration ou script de carga, execute obrigatoriamente esta sequencia de verificacao:

1. **Contexto e arquitetura do projeto**:
   - Leia [`AGENTS.md`](../../../AGENTS.md), [`backend/AGENTS.md`](../../../backend/AGENTS.md) e [`SECURITY.md`](../../../SECURITY.md).
   - Consulte [`docs/CONTEXTO_PROJETO.md`](../../../docs/CONTEXTO_PROJETO.md) para verificar a stack, as decisoes arquiteturais (`D1` a `D40`) e o estado atual das entregas.
2. **Regras de negocio e contrato**:
   - Consulte [`docs/REGRAS_NEGOCIO.md`](../../../docs/REGRAS_NEGOCIO.md) para conferir invariantes de dominio, maquinas de estado, obrigatoriedades por status (`RASCUNHO` vs. publicado) e regras de exclusao/inativacao.
   - Consulte o contrato da issue em `docs/issues/NNN.md` e [`docs/API.md`](../../../docs/API.md).
3. **Diagramas UML e entidades existentes**:
   - Consulte [`docs/modelo.plantuml`](../../../docs/modelo.plantuml) e o diagrama especifico do contexto em `docs/diagramas/aeria-*.puml`.
   - Inspecione as entidades (`model/`), conversores (`model/converterjpa/`) e repositories (`repository/`) ja existentes no modulo antes de propor qualquer alteracao, evitando duplicacao de tabelas, colunas ou consultas.

## 2. Governanca de Dominio e Schema (Aprovacao Obrigatoria)

- **Proibido criar ou alterar entidades, schema ou migrations sem aprovacao quando a mudanca representar uma nova decisao de dominio**:
  - Se um atributo, tabela, relacionamento, indice, restricao ou migration nao estiver previsto no contrato da issue (`docs/issues/NNN.md`), no PRD (`docs/PRD-Aeria.md`) ou nos diagramas UML (`docs/modelo.plantuml` e `docs/diagramas/`), **pare imediatamente e solicite aprovacao ao mantenedor** antes de codificar.
  - O modelo de dominio vence o prototipo (D36): nunca adicione colunas ou tabelas para sustentar elementos visuais que nao constam no modelo aprovado.
  - Nunca crie `VarianteProduto` (D40): a unidade de venda e `Produto`.
  - Sempre que uma mudanca de modelo for aprovada e implementada, sincronize no mesmo PR `docs/modelo.plantuml`, o `.puml` correspondente em `docs/diagramas/` e os scripts de carga (`backend/src/main/resources/import.sql`).

## 3. Entidades JPA e Mapeamentos (`model/`)

1. **Heranca base e encapsulamento**:
   - Toda entidade JPA deve herdar de `DefaultEntity` (`id` gerado com `GenerationType.IDENTITY`, `dataCadastro` e `dataAlteracao` mantidos via `@PrePersist` / `@PreUpdate`).
   - **Atributos privados e acessores explicitos**: todos os campos persistidos ou transientes devem ser `private`, com metodos `get`/`set` (ou `is` para `boolean`) explicitamente declarados. Nao use campos `public` (Active Record) nas entidades.
2. **Mapeamento de colunas e tipos**:
   - Defina explicitamente `@Table(name = "...")` e `@Column(name = "...", length = ..., nullable = ..., unique = ...)` em aderencia ao diagrama UML e as regras de rascunho (D21, D28: campos obrigatorios apenas na publicacao aceitam `nullable = true` no banco quando o registro puder ser salvo como `RASCUNHO`).
   - Valores monetarios devem usar `BigDecimal` com `@Column(precision = 12, scale = 2)`.
   - Heranca de `Produto` (`Drone`, `Acessorio`, `Kit`) utiliza `@Inheritance(strategy = InheritanceType.JOINED)` e `@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.INTEGER)` (D27, D40).
3. **Enums de dominio (D31)**:
   - Possuem `id` (`Long`) e `nome` (`String`), metodo `valueOf(Long id)` e `AttributeConverter<Enum, Long>` em `model/converterjpa/` anotado com `@Converter(autoApply = true)`.
4. **Relacionamentos e integridade referencial**:
   - Use `@JoinColumn(name = "id_<entidade>")` explicito em associacoes `@ManyToOne` e `@OneToOne`.
   - **Proibido `CascadeType.REMOVE` ou `orphanRemoval = true` em dados de negocio compartilhados** (D24, D28): registros em uso (marcas, categorias, subcategorias, fornecedores, produtos com pedidos/reservas/kits, promocoes aplicadas, cupons utilizados) nunca sao excluidos em cascata; o servico deve bloquear a exclusao com `ConflitoException` (`409 Conflict`).
   - Cascade de composicao estrita so e permitido quando o ciclo de vida do filho pertence exclusivamente ao agregado pai e o contrato autoriza (ex.: `ComponenteKit` ao excluir o proprio `Kit`).

## 4. Isolamento entre Tenants / Contas e Prevencao de Relacoes Cruzadas

1. **Tenant / titular obrigatorio**:
   - Toda entidade cujo ciclo de vida pertence a um tenant, organizacao ou conta de cliente/usuario deve possuir vinculo obrigatorio (`nullable = false`) com o respectivo titular ou tenant.
   - O identificador do tenant ou usuario autenticado deve vir sempre do contexto de seguranca (`JsonWebToken` / `SecurityContext`), nunca do payload ou query param.
2. **Prevencao de relacoes cruzadas (Cross-Tenant / Cross-Account)**:
   - Antes de associar duas entidades em uma operacao de escrita (ex.: vincular endereco, pedido, item, devolucao, movimento ou permissao), valide no banco que ambas pertencem ao mesmo tenant/titular ou escopo permitido.
   - Todas as consultas, atualizacoes e exclusoes em repositories e services devem filtrar explicitamente pelo `id` do tenant/titular autenticado (`WHERE e.id = :id AND e.usuario.id = :idUsuario`), impedindo IDOR e vazamento de dados entre contas.

## 5. Repositories e Consultas PostgreSQL (`repository/`)

1. **Padrao Panache Repository**:
   - Toda classe de acesso a dados deve ser `@ApplicationScoped` e implementar `PanacheRepository<Entidade>`.
2. **Consultas parametrizadas e seguras**:
   - Use sempre parametros posicionais (`?1`, `?2`) ou nomeados (`:param` com `Map<String, Object>`).
   - **Proibido concatenar strings vindas do usuario** em HQL/JPQL ou SQL nativo (`SECURITY.md`).
3. **Buscas, filtros e paginacao**:
   - Buscas textuais devem ser case-insensitive (`LOWER(campo) LIKE :busca` com parametro em minusculas via `Locale.ROOT`) e ignorar acentos quando especificado (D23).
   - Listagens retornam `PanacheQuery<Entidade>` com ordenacao deterministica (incluindo `id` como criterio de desempate, ex.: `ORDER BY nome, id`) para alimentar `PageResponse`.
   - Evite o problema de N+1 consultas usando `JOIN FETCH` ou consultas projetadas quando relacionamentos precisarem ser lidos em listagens.

## 6. Migrations SQL Versionadas (Flyway), Indices e Restricoes

1. **Migrations SQL versionadas e imutaveis**:
   - Quando o projeto utilizar migrations Flyway (`db/migration/V<versao>__<descricao>.sql`) ou scripts SQL de carga/schema (`localidades.sql`, `import.sql`), cada arquivo versionado ja aplicado e imutavel: novas alteracoes exigem nova migration incremental (ou atualizacao coordenada de `import.sql` enquanto o ambiente de dev/test operar com `drop-and-create`).
2. **Integridade referencial, restricoes e indices no PostgreSQL**:
   - Toda chave estrangeira deve ter constraint `FOREIGN KEY` explicita apontando para a tabela referenciada, sem `ON DELETE CASCADE` em tabelas de negocio protegidas pela regra de inativacao (D24).
   - Defina constraints `UNIQUE` (simples ou compostas, como `(id_grupo, rotulo_no_grupo)` em `produto`, `slug`, `sku`, `cnpj`, `email`, `documento`) e `CHECK` para garantir invariantes no nivel do banco.
   - Crie indices (`CREATE INDEX`) para colunas de chave estrangeira (`id_marca`, `id_subcategoria`, `id_fornecedor`, `id_usuario`, `id_tenant`), colunas de filtro frequente (`status`, `slug`, `sku`) e chaves de ordenacao.

## 7. Transacoes, Atomicidade e Controle de Concorrencia

1. **Atomicidade e demarcacao transacional**:
   - Toda operacao de escrita (`create`, `update`, `delete`, mudanca de status, movimentacao de estoque, processamento de pedido/pagamento, acao em lote) deve executar dentro de um metodo anotado com `@Transactional` na camada `ServiceImpl`.
   - Operacoes compostas (ex.: atualizar `Produto.estoqueFisico` e gravar `MovimentoEstoque` com `saldoResultante`; criar `Pedido`, congelar `EnderecoPedido`, registrar `UsoCupom` e reservar estoque) devem ser atomicas na mesma transacao: se qualquer passo falhar, toda a transacao sofre rollback.
   - Efeitos colaterais externos ao banco (ex.: remocao de arquivo de midia em disco após `DELETE`, envio de e-mail) so devem ocorrer apos a confirmacao da transacao ou de forma que nao corrompam a consistencia em caso de rollback.
2. **Controle de concorrencia**:
   - Em operacoes sujeitas a condicao de corrida (baixa/reserva de estoque, consumo de cupom com limite de uso, transicao concorrente de status de pedido/pagamento, reordenacao), aplique bloqueio pessimista (`LockModeType.PESSIMISTIC_WRITE` / `SELECT ... FOR UPDATE`) ou controle otimista (`@Version`) conforme a criticidade da operacao, garantindo que duas requisicoes simultaneas nao gerem saldo negativo nem ultrapassem limites de negocio.
   - Ao adquirir bloqueios pessimistas em multiplos registros (ex.: baixa de estoque dos itens de um pedido ou componentes de um `Kit`), ordene os IDs de forma ascendente antes do lock para prevenir deadlocks no PostgreSQL.

## 8. Fluxo de Verificacao

Ao concluir qualquer alteracao de persistencia:

1. Revise todos os itens no checklist de [padroes-persistencia.md](./references/padroes-persistencia.md).
2. Execute os testes da issue e a suite de verificacao do backend:
   ```bash
   cd backend && ./mvnw test -Dgroups=issue-<numero>
   cd backend && ./mvnw verify
   ```
3. Confirme que `docs/CONTEXTO_PROJETO.md`, `docs/REGRAS_NEGOCIO.md`, `docs/modelo.plantuml` e `docs/diagramas/aeria-*.puml` estao sincronizados.
