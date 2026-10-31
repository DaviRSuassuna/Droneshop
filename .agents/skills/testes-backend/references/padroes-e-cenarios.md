# Padroes de Escrita e Cenarios de Teste (Quarkus / REST Assured)

Referencia complementar da skill `testes-backend` com padroes praticos do repositorio `droneshop-api`, estrutura das classes de apoio e checklist de cenarios obrigatorios.

## 1. Utilitarios de Teste do Repositorio (`br.com.droneshop.testes`)

Sempre reutilize os utilitarios existentes em `backend/src/test/java/br/com/droneshop/testes/` em vez de recriar autenticacao ou massa de dados manualmente:

| Classe | Metodos Principais | Uso |
|---|---|---|
| [`Api`](../../../backend/src/test/java/br/com/droneshop/testes/Api.java) | `Api.given()` | Requisicao autenticada como o admin semente (`admin@aeria.com.br`). |
| [`Api`](../../../backend/src/test/java/br/com/droneshop/testes/Api.java) | `Api.anonimo()` | Requisicao sem token (rotas publicas `/loja/**` e testes de `401`). |
| [`Api`](../../../backend/src/test/java/br/com/droneshop/testes/Api.java) | `Api.como(token)` | Requisicao autenticada com o token de um cliente ou admin especifico. |
| [`Api`](../../../backend/src/test/java/br/com/droneshop/testes/Api.java) | `Api.carrinho(tokenCarrinho)` / `Api.carrinho(tokenCliente, tokenCarrinho)` | Requisicao enviando o header `X-Carrinho-Token`. |
| [`Dados`](../../../backend/src/test/java/br/com/droneshop/testes/Dados.java) | `Dados.sufixo()`, `Dados.cnpjValido()`, `Dados.ID_INEXISTENTE` | Geracao de nomes, slugs, SKUs e CNPJs unicos a cada execucao; constante `999_999_999L` para `404`. |
| [`Fixtures`](../../../backend/src/test/java/br/com/droneshop/testes/Fixtures.java) | `Fixtures.arvore()`, `criarDrone(...)`, `criarAcessorio(...)`, `criarKit(...)` | Criacao completa de catalogo via API respeitando D40 (produto sem variante) e D39 (tipo x categoria). |
| [`Fixtures`](../../../backend/src/test/java/br/com/droneshop/testes/Fixtures.java) | `Fixtures.novoCliente()`, `Fixtures.adminComPapel(idPapel)` | Criacao de cliente autenticado ou operador administrativo com papel especifico (`PAPEL_SOMENTE_LEITURA`, `PAPEL_CATALOGO`, etc.). |
| [`Fixtures`](../../../backend/src/test/java/br/com/droneshop/testes/Fixtures.java) | `Fixtures.pedidoPendente(...)`, `Fixtures.pedidoPago(...)`, `Fixtures.pedidoDaCarga(status)` | Criacao ou obtencao de pedidos para testes de transicao de status, pos-venda e devolucao. |

## 2. Exemplos de Padroes por Categoria de Cenario

### 2.1. Validacao (`400`), Rascunho vs. Publicacao (D21, D28) e Nao Encontrado (`404`)

- Em cadastros que suportam `RASCUNHO` (`idStatus = 1`), envie apenas `idStatus: 1` (ou campos parciais) e valide `200 OK`.
- Ao publicar (`idStatus = 2`), envie o payload sem os campos obrigatorios e verifique `400 Bad Request` e a presenca de cada campo em `errors.field`:

```java
given().contentType(ContentType.JSON)
        .body(Map.of("idStatus", Dados.ATIVA))
        .when().post("/marcas")
        .then().statusCode(400)
        .body("errors.field", hasItems("nome", "paisOrigem"));

given().when().get("/marcas/{id}", Dados.ID_INEXISTENTE)
        .then().statusCode(404);
```

### 2.2. Autenticacao (`401`), Autorizacao (`403`) e Autoprotecao (`409`)

- Teste sempre quem **nao** pode executar a operacao:

```java
// Sem token -> 401
Api.anonimo().when().get("/admin/usuarios").then().statusCode(401);

// Cliente em rota admin -> 403
Fixtures.Cliente cli = Fixtures.novoCliente();
Api.como(cli.token()).when().get("/admin/usuarios").then().statusCode(403);

// Papel SOMENTE_LEITURA -> lista (200), mas nao cria/edita/exclui (403)
String tokenLeitura = Fixtures.adminComPapel(Fixtures.PAPEL_SOMENTE_LEITURA);
Api.como(tokenLeitura).when().get("/marcas").then().statusCode(200);
Api.como(tokenLeitura).contentType(ContentType.JSON)
        .body(Map.of("idStatus", Dados.RASCUNHO))
        .when().post("/marcas").then().statusCode(403);
```

### 2.3. Isolamento entre Contas / Clientes (Anti-IDOR)

- Crie dois titulares independentes (`dono` e `outro`). Gere o recurso como `dono` e tente ler, alterar ou apagar usando o token de `outro`:

```java
Fixtures.Cliente dono = Fixtures.novoCliente();
Fixtures.Cliente outro = Fixtures.novoCliente();
long idEndereco = Fixtures.enderecoCliente(dono);

Api.como(outro.token()).when().delete("/conta/enderecos/{id}", idEndereco)
        .then().statusCode(404);

// Confirma que o recurso do dono permanece intacto
Api.como(dono.token()).when().get("/conta/enderecos")
        .then().statusCode(200)
        .body("id", hasItem((int) idEndereco));
```

### 2.4. Referencias Cruzadas e Exclusao em Uso (`409 Conflict` - D24)

- Ao tentar excluir um registro referenciado por outro (ex.: marca/subcategoria/fornecedor com produto, ou promocao aplicada em pedido), exija `409` e comprove que o registro continua existindo:

```java
given().when().delete("/marcas/{id}", idMarcaEmUso)
        .then().statusCode(409)
        .body("detail", containsString("1 produto"));

given().when().get("/marcas/{id}", idMarcaEmUso)
        .then().statusCode(200);
```

### 2.5. Historico, Rastreabilidade (D11) e Imutabilidade (D12, RN15)

- Apos uma transicao de status ou movimento de estoque, consulte a rota de historico/listagem e valide `origem`, `autor`, `statusAnterior`, `statusNovo` e `saldoResultante`.
- Para imutabilidade (ex.: endereco do pedido congelado), altere o cadastro original (`PUT /conta/enderecos/{id}`) e comprove via `GET /pedidos/{id}` que os dados do pedido nao mudaram.

### 2.6. Concorrencia, Idempotencia e Disputa de Saldo

- **Disputa sequencial deterministica**: dois carrinhos/clientes adicionam a totalidade do estoque disponivel; o primeiro reserva/compra com `200`, o segundo recebe `409`, e o saldo disponivel final e `0` (nunca negativo).
- **Disputa paralela real** (quando exigido para validar locks/transacoes concorrentes):

```java
int threads = 4;
ExecutorService pool = Executors.newFixedThreadPool(threads);
CountDownLatch pronta = new CountDownLatch(threads);
CountDownLatch largada = new CountDownLatch(1);
List<Future<Integer>> resultados = new ArrayList<>();

for (int i = 0; i < threads; i++) {
    String tokenCarrinho = carrinhos.get(i);
    resultados.add(pool.submit(() -> {
        pronta.countDown();
        largada.await(5, TimeUnit.SECONDS);
        return Api.carrinho(tokenCarrinho).when().post("/carrinho/reservar").statusCode();
    }));
}
pronta.await(5, TimeUnit.SECONDS);
largada.countDown();
pool.shutdown();
```

Valide que a contagem de status `200` nao ultrapassa o estoque fisico e que as demais requisicoes receberam `409`.

## 3. Checklist de Revisao de Testes

Antes de finalizar a criacao ou revisao de uma suite de testes, confira:

- [ ] A classe ou metodo possui `@Tag("issue-N")` correspondente a issue?
- [ ] Nenhum teste depende de `totalItems` fixo da tabela global sem filtrar pelo sufixo/registro criado no proprio teste?
- [ ] Nenhum teste faz chamadas reais a servicos externos na internet (usa `ViaCepFalso`, `MercadoPagoFalso`, `FreteFalso`, `ResendFalso`, `GoogleFalso`)?
- [ ] Foram testados os casos de sucesso (`200` / `204`) verificando tambem a persistencia real via `GET` subsequente?
- [ ] Foram testadas as validacoes de entrada (`400` com `errors.field`), registros inexistentes (`404`) e conflitos de negocio/exclusao em uso/transicao invalida (`409`, nunca `422`)?
- [ ] Foram testados os controles de acesso (`401` anonimo, `403` papel sem permissao ou cliente em rota admin) e o isolamento entre clientes/contas?
- [ ] Em operacoes que falham (`400`, `404`, `409`), o teste comprova que o estado anterior permaneceu inalterado (transacao fez rollback)?
- [ ] Os testes foram **realmente executados** via `./mvnw test` ou `./mvnw verify` e o resultado exato foi reportado ao usuario?
