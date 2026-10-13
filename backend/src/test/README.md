# Testes do backend

Testes de API com RestAssured contra a aplicação Quarkus de pé (`@QuarkusTest`), num PostgreSQL 16 descartável. Cada teste é etiquetado com a issue que ele cobre, e o CI usa essas etiquetas para decidir o que bloqueia o merge.

## Como rodar

Precisa do Docker aberto: no perfil `%test`, o Dev Services sobe o Postgres sozinho.

```bash
cd backend
./mvnw verify                                  # tudo
./mvnw test -Dgroups=issue-9                   # só os testes de uma issue
./mvnw test -Dgroups=base                      # só as entregas anteriores às issues
./mvnw test -Dtest=MarcaResourceTest           # uma classe
./mvnw test -Dtest=MarcaResourceTest#apagaMarca  # um teste
```

No CI o banco vem do próprio job (`QUARKUS_DATASOURCE_*`), sem Dev Services.

## Fluxo por issue

1. Os testes da issue são escritos a partir do **Contrato de API** e dos **Critérios de aceite** dela, antes ou junto com o código, com `@Tag("issue-<numero>")` na classe ou no método.
2. Enquanto a issue não foi entregue, os testes dela rodam no CI só como informação.
3. No PR que entrega a issue, a etiqueta entra em [`issues-entregues.txt`](issues-entregues.txt). A partir daí, se algum teste dela falhar, o merge fica bloqueado.

Quando a issue se contradiz ou deixa algo em aberto, o teste fica escrito, mas pulado, com o motivo:

```java
@Disabled("conflito: o critério diz X e o contrato diz Y")
@Disabled("lacuna: a issue não diz o que acontece quando Z")
```

Quando a decisão sair, basta tirar o `@Disabled` e ajustar o teste, se preciso.

## Estrutura

| Onde | Para quê |
|---|---|
| `java/br/com/droneshop/<modulo>/` | Um pacote por módulo, espelhando o `src/main`; um arquivo por recurso da API |
| `testes/Dados.java` | Sufixo aleatório, CNPJ válido e ids de estado/município. O banco é compartilhado com a carga do `import.sql`, então cada teste cria os próprios registros e nunca depende do total da tabela |
| `endereco/ViaCepFalso.java` | ViaCEP falso que só existe nos testes. O `%test` aponta o REST Client para ele, então nenhum teste depende da internet |
| `issues-entregues.txt` | Etiquetas que bloqueiam o merge no CI |

## Matriz issue × testes

| Etiqueta | Arquivos | Testes |
|---|---|---:|
| `base` (entregas anteriores às issues) | `catalogo/MarcaResourceTest`, `CategoriaResourceTest`, `SubcategoriaResourceTest`, `FornecedorResourceTest`, `ExclusaoEmUsoTest`, `endereco/CepResourceTest`, `EstadoMunicipioResourceTest` | 70 |

## Testes pulados

Nenhum no momento.

## Frontend

As specs ficam ao lado do código (`*.spec.ts`) e rodam com `npx ng test --watch=false` dentro de `frontend/`. Hoje cobrem os services de `core/services` (método, URL, parâmetros e corpo de cada chamada à API).
