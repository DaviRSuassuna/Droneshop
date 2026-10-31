# AGENTS.md (Backend)

Guia para agentes de IA que implementam ou revisam código em `backend/` (`droneshop-api`). Complementa o [`AGENTS.md` da raiz](../AGENTS.md), o [`docs/PRD-Aeria.md`](../docs/PRD-Aeria.md), o [`docs/API.md`](../docs/API.md) e o [`SECURITY.md`](../SECURITY.md). Escreva código, mensagens de validação e comentários em **português**, sem usar travessão nem emojis.

## Stack

- **Java 25**, **Quarkus 3**, Maven (`./mvnw`), PostgreSQL 16.
- **Hibernate ORM com Panache** (padrão Repository), **Hibernate Validator** (Jakarta Bean Validation), **Quarkus REST + Jackson**, **SmallRye JWT**, **SmallRye OpenAPI**, **Quarkus Mailer** / **Qute**.
- Testes com **JUnit 5** (`@QuarkusTest`) e **RestAssured**.

## Organização por módulos e camadas (Padrão SGA)

O código fica em `src/main/java/br/com/droneshop`, dividido por módulo de domínio (`administracao`, `atendimento`, `catalogo`, `compra`, `devolucao`, `endereco`, `estoque`, `identidade`, `notificacao`, `pagamento`, `pedido`) e pacotes transversais (`model`, `dto`, `exception`, `resource`, `util`).

Cada módulo segue as camadas do projeto de referência SGA:

1. **`model`**:
   - Toda entidade JPA herda de `DefaultEntity` (`id`, `dataCadastro`, `dataAlteracao` com `@PrePersist` / `@PreUpdate`).
   - Herança de produto (`Produto` → `Drone`, `Acessorio`, `Kit`) usa `@Inheritance(strategy = InheritanceType.JOINED)` (D27, D40).
   - **Enums de domínio (D31)**: possuem `ID` (`Long`) e `NOME` (`String`), anotados com `@JsonFormat(shape = JsonFormat.Shape.OBJECT)`, método `public static Xxx valueOf(Long id)` e conversor em `model/converterjpa/XxxConverter.java` (`@Converter(autoApply = true)`). Os ids começam em `1` seguindo a ordem do diagrama `.puml`.
   - Mudou entidade ou enum: atualize o diagrama em `docs/diagramas/aeria-*.puml` e o `src/main/resources/import.sql`.

2. **`dto`**:
   - Entrada: `record XxxRequestDTO` e saída: `record XxxResponseDTO`.
   - **Mensagens do Bean Validation** na entrada sempre terminam com `(back).` (ex.: `"O campo deve conter entre 2 e 80 caracteres (back)."`).
   - **Chaves estrangeiras**: no request recebem `Long idXxx` (`idMarca`, `idStatus`, `idSubcategoria`); no response devolvem o objeto ou resumo aninhado (`marca`, `status: {id, nome}`).
   - **Rascunho no RequestDTO (D21, D28)**: quando o cadastro aceita `RASCUNHO`, os campos obrigatórios apenas na publicação **não** levam `@NotBlank` / `@NotNull` no record. Em vez disso, o construtor compacto normaliza strings em branco para `null` com `Textos.nuloSeVazio(campo)` para que `@Length` / `@Email` só validem quando houver conteúdo.
   - **Valores monetários**: sempre `BigDecimal` com escala `2` (inclusive `0.00`). Documentos (CPF, CNPJ, CEP, telefone) trafegam **apenas com dígitos** (`Documentos.apenasDigitos`).

3. **`mapper`**:
   - Classes utilitárias (`XxxResponseMapper`, `XxxAlteracaoMapper`) com métodos estáticos `toResponse(...)` para converter entidades em DTOs de resposta sem vazar entidades JPA no resource.

4. **`repository`**:
   - Implementa `PanacheRepository<Entidade>` com `@ApplicationScoped`.
   - Consultas usam parâmetros nomeados ou posicionais (`?1`, `:nome`). **Nunca concatene strings vindas do usuário em HQL/JPQL ou SQL** (`SECURITY.md`).
   - Filtros de listagem ignoram parâmetros vazios ou nulos; buscas por nome não diferenciam maiúsculas/minúsculas (D23).

5. **`service`**:
   - Interface `XxxService` + implementação `XxxServiceImpl` (`@ApplicationScoped`).
   - Métodos de escrita (`create`, `update`, `delete`, ações em lote, mudanças de status) levam `@Transactional`.
   - **Validação de rascunho vs. publicação (D21, D28)**: no `ServiceImpl`, se o status for `RASCUNHO` (id `1`), aceita campos vazios; se for `ATIVO`/`INATIVO`, acumula os campos faltantes em `List<Problem.FieldError>` (com mensagem `"O campo deve ser informado (back)."`) e lança `ValidationException`.
   - **Erros de domínio**:
     - `ValidationException` → responde **400** no formato `Problem` com `errors[].field` idêntico ao nome do campo no JSON do contrato.
     - `ResourceNotFoundException` → responde **404** em consulta, alteração ou exclusão de registro inexistente (D22).
     - `ConflitoException` → responde **409** quando o registro está em uso ao tentar excluir (D24, orientando inativar e informando a contagem) ou em transição de status inválida (D30). **Nunca use 422**.

6. **`resource`**:
   - `@Produces(MediaType.APPLICATION_JSON)` e `@Consumes(MediaType.APPLICATION_JSON)`.
   - **Segurança obrigatória no admin (`SECURITY.md`)**: todo resource administrativo deve ter `@RolesAllowed("ADMINISTRATIVO")` e `@ModuloAdmin(ModuloSistema.XXX)`. Resource administrativo novo sem essas anotações é **bloqueante**.
   - **Prefixos de rota**: admin na raiz (`/marcas`, `/produtos`, `/admin/...`), leitura pública da vitrine em `/loja/**` e dados do cliente autenticado em `/conta/**`.
   - **Rotas de enums (D31)**: todo enum de domínio tem uma rota pública de leitura em kebab-case (`/status-marca`, `/tipos-produto`, `/papeis-administrativos`) com `GET` (lista todos) e `GET /{id}`.
   - **Listagens paginadas**: `@QueryParam("page") @DefaultValue("0") int page` e `@QueryParam("pageSize") @DefaultValue("10") int pageSize`, retornando `PageResponse.of(lista, page, pageSize, totalItems, Mapper::toResponse)`. Não crie rotas `/count`.
   - **Códigos HTTP de sucesso**: `GET` e `POST` retornam `200 OK` com o DTO; `PUT`, `PATCH` (sem corpo de retorno) e `DELETE` retornam `204 No Content` (`void`).
   - **Exportação CSV (#61)**: usa `Csv.resposta("arquivo.csv", cabecalhos, streamDeLinhas)` com `@Produces("text/csv")`.

## Regras de negócio e segurança críticas no backend

- **Sem `VarianteProduto` (D40)**: preço, SKU, custo, estoque físico/mínimo e dimensões de embalagem pertencem a `Produto`. `Kit` não tem estoque físico próprio: a disponibilidade e a baixa são calculadas sobre seus `ComponenteKit` (D7).
- **Estoque (RF14, D11)**: `estoqueFisico` só é definido na criação do produto ou alterado via `MovimentoEstoque` (com `origem`: `MANUAL`, `SISTEMA` ou `INTEGRACAO`; `autor` preenchido apenas quando `MANUAL`, obtido do token JWT).
- **Autor e cliente vêm do token (`SECURITY.md`, D11)**: em `/conta/**`, movimentos manuais, histórico de status e auditoria, o usuário vem sempre do `JsonWebToken` / `SecurityContext`, nunca do corpo ou query param.
- **Upload de mídia (D29, `SECURITY.md`)**: somente PNG, JPG e PDF até 20 MB, salvos em `aeria.midias.diretorio` com nome de arquivo gerado no servidor (nunca use o nome enviado pelo cliente como caminho em disco).
- **Dados sensíveis**: senhas somente com hash BCrypt; dados de cartão nunca passam pelo backend (apenas token do gateway e últimos dígitos, RN11); logs e respostas de erro nunca expõem stack trace, SQL, token, senha ou CPF/CNPJ completo.

## Testes (`src/test`)

Consulte [`src/test/README.md`](src/test/README.md) para a matriz completa.

- **Etiquetas por issue**: todo teste de API usa `@QuarkusTest` e `@Tag("issue-N")` (ou `@Tag("base")`). Quando a issue é entregue, sua etiqueta deve ser adicionada a `src/test/issues-entregues.txt` para bloquear regressões no CI.
- **Isolamento de dados**: o banco de teste começa com `import.sql`, portanto **nenhum teste pode depender da contagem total da tabela**. Crie os registros necessários no próprio teste usando `testes/Fixtures.java`, `testes/Dados.java` (sufixos únicos, CNPJs válidos) e `testes/Api.java` (`Api.given()`, `Api.anonimo()`, `Api.como(token)`).
- **Integrações externas falsas (D34)**: nenhum teste acessa a internet. Use os dublês em `src/test`: `ViaCepFalso`, `MercadoPagoFalso`, `FreteFalso`, `ResendFalso` e `GoogleFalso`.
- **Proibido afrouxar testes**: os testes em `src/test` representam o contrato fechado da issue (`docs/issues/NNN.md`). Não remova asserções nem altere status esperados para fazer o teste passar sem mudança autorizada no contrato.

## Comandos úteis

```bash
# Subir banco para desenvolvimento local e rodar em modo dev
docker compose up -d postgres
java scripts/GerarChavesJwtDev.java
./mvnw quarkus:dev

# Rodar todos os testes (usa Dev Services se Docker estiver ativo)
./mvnw verify

# Rodar apenas os testes de uma issue ou classe específica
./mvnw test -Dgroups=issue-80
./mvnw test -Dtest=MarcaResourceTest
```
