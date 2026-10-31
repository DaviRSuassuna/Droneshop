# Padroes de Arquitetura e Checklist Quarkus Backend

Referencia complementar da skill `quarkus-backend` com os padroes de codigo e o checklist de revisao das camadas `Resource -> Service -> Repository -> Entity`.

## 1. Padrao por Camada

### Resource (`br.com.droneshop.<modulo>.resource`)

- Anotacoes de classe:
  - `@Path("...")`
  - `@Produces(MediaType.APPLICATION_JSON)` e `@Consumes(MediaType.APPLICATION_JSON)`
  - Em rotas administrativas: `@RolesAllowed("ADMINISTRATIVO")` e `@ModuloAdmin(ModuloSistema.XXX)`
- Convencoes de retorno HTTP:
  - `GET` (listagem): retorna `PageResponse<XxxResponseDTO>` com `@QueryParam("page") @DefaultValue("0") int page` e `@QueryParam("pageSize") @DefaultValue("10") int pageSize`.
  - `GET` (por id ou chave unica): retorna `XxxResponseDTO` (`200 OK`).
  - `POST`: retorna `XxxResponseDTO` (`200 OK` conforme convencao SGA do projeto).
  - `PUT`, `PATCH` (sem corpo) e `DELETE`: retornam `void` (`204 No Content`).
- Proibido no Resource:
  - Retornar ou receber classe anotada com `@Entity`.
  - Injetar `PanacheRepository` diretamente.
  - Declarar `@Transactional`.

### Service (`br.com.droneshop.<modulo>.service`)

- Interface `XxxService` e implementacao `XxxServiceImpl` anotada com `@ApplicationScoped`.
- Metodos que alteram estado (`create`, `update`, `delete`, mudancas de status, acoes em lote) devem ter `@Transactional`.
- Validacoes de dominio:
  - Conferir existencia de chaves estrangeiras (`idMarca`, `idSubcategoria`, `idProduto`, etc.) antes de persistir.
  - Em cadastros com suporte a `RASCUNHO` (id `1`), permitir campos vazios no rascunho e cobrar os obrigatorios apenas ao publicar (`ATIVO`/`INATIVO`), acumulando todos os erros em `List<Problem.FieldError>` e lancando `ValidationException`.
  - Em exclusoes (`delete`), verificar se o registro esta em uso. Se estiver, lancar `ConflitoException` (`409`) informando a contagem e orientando a inativacao. Se nao existir, lancar `ResourceNotFoundException` (`404`).

### Repository (`br.com.droneshop.<modulo>.repository`)

- Classe `@ApplicationScoped` implementando `PanacheRepository<Entidade>`.
- Toda query usa parametros vinculados (`?1`, `:nome`), sem concatenacao de strings.
- Buscas textuais de listagem usam `LOWER(...)` para nao diferenciar maiusculas de minusculas.

### DTO e Mapper (`br.com.droneshop.<modulo>.dto` e `mapper`)

- `XxxRequestDTO`:
  - Declarado como `record`.
  - Mensagens de Bean Validation terminam com `(back).`.
  - Referencias a outras entidades ou enums usam `Long idXxx`.
  - Construtor compacto normaliza strings vazias para `null` (`Textos.nuloSeVazio(campo)`) nos cadastros que aceitam rascunho.
- `XxxResponseDTO`:
  - Declarado como `record`.
  - Nao expoe campos sensiveis (`senhaHash`, segredos 2FA, tokens) nem campos internos em rotas da loja (`custoAquisicao`, `margemPercentual`, `estoqueMinimo`).
  - Valores monetarios em `BigDecimal` com escala `2`.
- `XxxResponseMapper`:
  - Classe utilitaria com construtor privado ou metodos estaticos `toResponse(Entidade e)` que constroem o `XxxResponseDTO`.

## 2. Checklist de Revisao e Implementacao

Use esta lista antes de concluir qualquer entrega ou revisao:

1. **Contrato e Escopo**:
   - [ ] Os caminhos `@Path`, verbos HTTP, query params e nomes de campos JSON batem exatamente com `docs/issues/NNN.md` e `docs/API.md`?
   - [ ] Nenhuma entidade JPA e exposta diretamente no retorno ou parametro do `Resource`?
   - [ ] Nenhuma classe, endpoint ou regra fora do escopo aprovado foi adicionada?
2. **Transacoes e Regras de Negocio**:
   - [ ] Todos os metodos de escrita no `ServiceImpl` estao anotados com `@Transactional`?
   - [ ] Cadastros em `RASCUNHO` aceitam campos vazios e cobram obrigatorios ao publicar?
   - [ ] Exclusao de registro em uso responde `409 Conflict` sem exclusao em cascata indevida?
   - [ ] Registro nao encontrado em `GET`, `PUT` ou `DELETE` responde `404 Not Found`?
   - [ ] Transicao de status invalida responde `409 Conflict` (nunca `422`)?
3. **Seguranca e Isolamento**:
   - [ ] Resources administrativos possuem `@RolesAllowed("ADMINISTRATIVO")` e `@ModuloAdmin(...)`?
   - [ ] Recursos de cliente/tenant filtram pelo usuario extraido do token JWT, impedindo acesso a dados de terceiros?
   - [ ] Consultas no repository usam apenas parametros vinculados?
4. **Testes e Documentacao**:
   - [ ] Testes RestAssured com `@Tag("issue-N")` passam sem depender da contagem total da tabela nem de acesso externo a internet?
   - [ ] `docs/CONTEXTO_PROJETO.md`, `docs/REGRAS_NEGOCIO.md` e `docs/modelo.plantuml` continuam sincronizados com o codigo?
