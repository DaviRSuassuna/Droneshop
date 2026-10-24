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

1. Os testes da issue são escritos a partir do **Contrato de API** e dos **Critérios de aceite** dela, antes ou junto com o código, com `@Tag("issue-<numero>")` na classe ou no método. Issue nova ainda sem número usa o nome do arquivo em `docs/issues/` (ex.: `issue-lote-csv`).
2. Enquanto a issue não foi entregue, os testes dela rodam no CI só como informação, e só no PR da própria issue: o CI descobre a issue pelo número no começo do nome da branch (`feat/10-...` ou `10-...` roda `issue-10`). A suíte inteira só roda quando pedida à mão (Actions > backend > Run workflow > "todas").
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
| `testes/Api.java` | Ponto de partida das requisições: `given()` entra como o admin semente (`admin@aeria.com.br`) e manda o token; antes da #4, manda sem token (D25). Também `anonimo()`, `como(token)` e `carrinho(token)` |
| `testes/Fixtures.java` | Massa de dados montada só pela API: árvore de catálogo, drone, acessório e kit (produto, D40), cliente logado, carrinho, pedido pendente e pago. Ids dos enums (D31) |
| `testes/Totp.java` | Código TOTP para os testes de 2FA (#45) |
| `endereco/ViaCepFalso.java` | ViaCEP falso que só existe nos testes. O `%test` aponta o REST Client para ele, então nenhum teste depende da internet |
| `pagamento/MercadoPagoFalso.java`, `pedido/FreteFalso.java`, `notificacao/ResendFalso.java`, `identidade/GoogleFalso.java` | Os outros serviços externos, no mesmo esquema do ViaCEP (D34). Cada um documenta como simular recusa ou serviço fora do ar |
| `issues-entregues.txt` | Etiquetas que bloqueiam o merge no CI |

## Matriz issue × testes

Testes escritos a partir do contrato de cada issue (`docs/issues/NNN.md`), antes do código. Enquanto a issue não está em `issues-entregues.txt`, os testes dela falham por não haver código e só informam no CI.

<!-- matriz:inicio -->

| Etiqueta | Situação no CI | Arquivos | Testes |
|---|---|---|---:|
| `base` | bloqueia o merge | `catalogo/CategoriaResourceTest`, `catalogo/FornecedorResourceTest`, `catalogo/MarcaResourceTest`, `catalogo/SubcategoriaResourceTest`, `endereco/CepResourceTest`, `endereco/EstadoMunicipioResourceTest`, `exception/RotaInexistenteTest` | 77 |
| `issue-4` | bloqueia o merge | `identidade/AutenticacaoResourceTest` | 12 |
| `issue-5` | bloqueia o merge | `administracao/UsuarioAdministrativoResourceTest` | 14 |
| `issue-6` | bloqueia o merge | `administracao/ConfiguracaoLojaResourceTest` | 4 |
| `issue-7` | bloqueia o merge | `catalogo/ProdutoSeoResourceTest` | 7 |
| `issue-8` | bloqueia o merge | `catalogo/AcessorioResourceTest`, `catalogo/CategoriaResourceTest`, `catalogo/DroneResourceTest`, `catalogo/ExclusaoEmUsoTest`, `catalogo/ProdutoResourceTest`, `catalogo/SubcategoriaResourceTest` | 29 |
| `issue-11` | bloqueia o merge | `catalogo/CompatibilidadeResourceTest` | 6 |
| `issue-12` | bloqueia o merge | `catalogo/MidiaProdutoResourceTest` | 14 |
| `issue-13` | bloqueia o merge | `estoque/MovimentoEstoqueResourceTest` | 9 |
| `issue-14` | bloqueia o merge | `catalogo/PromocaoResourceTest` | 7 |
| `issue-15` | bloqueia o merge | `compra/CupomResourceTest` | 6 |
| `issue-16` | bloqueia o merge | `pedido/PedidoResourceTest` | 7 |
| `issue-17` | bloqueia o merge | `pedido/StatusPedidoResourceTest` | 5 |
| `issue-18` | bloqueia o merge | `administracao/DashboardResourceTest` | 4 |
| `issue-19` | informativo | `identidade/ClienteResourceTest`, `identidade/EnderecoClienteResourceTest` | 17 |
| `issue-20` | informativo | `identidade/SessaoResourceTest` | 7 |
| `issue-21` | informativo | `catalogo/LojaProdutoResourceTest`, `catalogo/LojaVitrineResourceTest` | 13 |
| `issue-22` | informativo | `compra/ListaDesejosResourceTest` | 3 |
| `issue-23` | informativo | `estoque/ReservaResourceTest` | 7 |
| `issue-24` | informativo | `compra/CarrinhoResourceTest` | 9 |
| `issue-25` | informativo | `compra/CupomCarrinhoResourceTest` | 8 |
| `issue-26` | informativo | `compra/CheckoutResourceTest` | 6 |
| `issue-27` | informativo | `pagamento/PagamentoResourceTest` | 8 |
| `issue-28` | informativo | `pagamento/CartaoSalvoResourceTest` | 6 |
| `issue-29` | informativo | `pedido/CriacaoPedidoResourceTest` | 5 |
| `issue-30` | informativo | `pedido/FreteResourceTest` | 8 |
| `issue-31` | informativo | `pedido/RastreioResourceTest` | 3 |
| `issue-32` | informativo | `notificacao/NotificacaoResourceTest` | 6 |
| `issue-33` | informativo | `identidade/TokenEmailResourceTest` | 4 |
| `issue-34` | informativo | `estoque/UnidadeIndividualResourceTest` | 6 |
| `issue-35` | informativo | `pedido/NotaFiscalResourceTest` | 4 |
| `issue-36` | informativo | `pagamento/EstornoResourceTest` | 3 |
| `issue-37` | informativo | `devolucao/DevolucaoResourceTest` | 5 |
| `issue-38` | informativo | `atendimento/ChamadoResourceTest` | 5 |
| `issue-39` | informativo | `atendimento/AvaliacaoResourceTest` | 4 |
| `issue-40` | informativo | `atendimento/PerguntaResourceTest` | 4 |
| `issue-41` | informativo | `administracao/AuditoriaResourceTest` | 6 |
| `issue-42` | informativo | `notificacao/PreferenciaNotificacaoResourceTest` | 6 |
| `issue-43` | informativo | `catalogo/TermoBuscadoResourceTest` | 4 |
| `issue-44` | informativo | `catalogo/ComparacaoResourceTest` | 2 |
| `issue-45` | informativo | `administracao/DoisFatoresResourceTest` | 4 |
| `issue-46` | informativo | `identidade/LgpdResourceTest` | 5 |
| `issue-47` | informativo | `identidade/GoogleResourceTest` | 6 |
| `issue-61` | informativo | `administracao/AcoesAdminResourceTest`, `administracao/ComplementosAdminResourceTest`, `pedido/service/EtiquetaPedidoServiceTest`, `util/CsvTest` | 20 |
| `issue-80` | bloqueia o merge | `catalogo/GrupoProdutoResourceTest`, `catalogo/KitResourceTest`, `catalogo/ProdutoDadosComerciaisResourceTest`, `catalogo/SubcategoriaResourceTest` | 37 |
| `issue-catalogo-complementos` | informativo | `catalogo/CatalogoComplementosResourceTest` | 7 |
| `issue-dashboard-indicadores` | informativo | `administracao/DashboardIndicadoresResourceTest` | 8 |
| `issue-empresas` | informativo | `atendimento/LeadEmpresaResourceTest` | 2 |
| `issue-favoritos-compartilhar` | informativo | `compra/FavoritosCompartilharResourceTest` | 6 |
| `issue-imagem-produto` | informativo | `catalogo/ImagemProdutoResourceTest` | 8 |
| `issue-meus-drones` | informativo | `estoque/MeusDronesResourceTest` | 6 |
| `issue-pedidos-painel` | informativo | `pedido/PainelPedidosResourceTest` | 6 |

Total: 475 testes. Gerado por `scripts/vincular_testes_issues.py --matriz`.

<!-- matriz:fim -->

## Testes pulados

<!-- pulados:inicio -->

Nenhum no momento.

<!-- pulados:fim -->

## Frontend

- **Unitários:** as specs ficam ao lado do código (`*.spec.ts`) e rodam com `npx ng test --watch=false` dentro de `frontend/`. Hoje cobrem os services de `core/services` (método, URL, parâmetros e corpo de cada chamada à API).
- **Conformidade com o protótipo:** `frontend/e2e` (Playwright). Cada tela do protótipo tem uma rota e uma etiqueta de issue (`e2e/conformidade/telas.ts`); as telas em `e2e/telas-entregues.txt` bloqueiam o merge. Detalhes em [`frontend/e2e/README.md`](../../../frontend/e2e/README.md).
