---
numero: null
titulo: "[catalogo] Cada versão vira produto próprio e GrupoProduto junta as versões (D40)"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:catalogo"]
fase: 1
---

### Módulo

catalogo (com reflexo em estoque, compra e pedido)

### Classes do diagrama

Produto, GrupoProduto (nova), Kit (nova), ComponenteKit; sai VarianteProduto

### Diagrama de referência

`docs/diagramas/aeria-catalogo.puml` (e os stubs em `aeria-estoque`, `aeria-compra` e `aeria-pedido`)

### Requisitos, regras e decisões (PRD)

RF03, RF04, RF11, RF57, RN18, D7, D16, D26, D28, D40. As issues abertas que citavam variante (#21 a #25, #30, #34, #41) já foram ajustadas ao contrato novo.

### O que entregar

Refatoração do catálogo: sai `VarianteProduto`, e cada versão vendável passa a ser um produto próprio. Produtos parecidos se juntam num `GrupoProduto`, que vira uma página de venda com seletor de versão.

1. **Produto vendável.** `Produto` ganha `preco`, `custoAquisicao`, `estoqueFisico`, `estoqueMinimo`, `pesoEmbalagemKg`, `alturaEmbalagemCm`, `larguraEmbalagemCm` e `comprimentoEmbalagemCm`. `modelo` vira `sku` (único em toda a loja). As regras da variante passam para o produto: ATIVO/INATIVO exige preço > 0 e embalagem (RN18); RASCUNHO aceita incompleto (D28); `estoqueFisico` só muda na criação (saldo inicial com movimento ENTRADA) ou por movimento; `margemPercentual` calculada no backend (D26).
2. **Grupo.** Entidade `GrupoProduto` (`nome`, `slug` único, `descricaoCurta`, `ativo`) com CRUD em `/grupos-produto`. `Produto` ganha `grupo` (opcional), `rotuloNoGrupo` (obrigatório com grupo, único dentro do grupo) e `ordemNoGrupo`. Um grupo pode misturar tipos (drone e kit, por exemplo). Excluir grupo com produtos responde 409 (D24).
3. **Kit.** Terceiro tipo da herança JOINED: `Kit extends Produto`, `TipoProduto.KIT = 3`, rotas `/kits` e `/kits/{id}` como as de drone e acessório (D27). `ComponenteKit` aponta para `Produto` (`idProduto`), nunca para outro kit. `estoqueFisico` nulo; disponibilidade e embalagem derivadas dos componentes, salvo sobrescrita (D7, D16).
4. **Referências.** Trocam de `VarianteProduto` para `Produto`: `ItemPedido`, `MovimentoEstoque`, `Promocao` (`idsProdutos`), `Cupom` (`idsProdutos`), e os itens de carrinho e lista de desejos quando existirem. Tabelas `promocao_variante` e `cupom_variante` viram `promocao_produto` e `cupom_produto`.
5. **Remoções.** `VarianteProduto`, `VarianteResource`, `VarianteService`, os DTOs e o mapper de variante. A rota `/variantes` sai; o select de produto usa `GET /produtos?busca=&vendavel=true`.
6. **Carga de dados.** `import.sql` sem as variantes "Padrão": cada produto já traz preço, estoque e embalagem; o Fly More vira o kit "Falcon X2 Pro Fly More"; grupo "Falcon X2" com X2 Pro, X2 S e o Fly More, e grupo "Nimbus 3" com Nimbus 3 e Nimbus 3 Combo.
7. **Loja (contrato para a #21).** `/loja/produtos` devolve um item por grupo ("a partir de" o menor preço ativo) e os produtos sem grupo; `/loja/produtos/{slug}` devolve `grupo` com as versões ativas (`rotulo`, `slug`, `preco`, `disponivel`). A avaliação continua por produto; a página mostra a média do grupo.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md).

```
POST /drones  (e /acessorios)  { ...produto..., "sku": "DRN-FX2P-0042", "preco": 12499.00,
   "custoAquisicao": 8124.35, "estoqueFisico": 12, "estoqueMinimo": 2, "pesoEmbalagemKg": 2.1,
   "alturaEmbalagemCm": 15, "larguraEmbalagemCm": 28, "comprimentoEmbalagemCm": 38,
   "idGrupo": 3, "rotuloNoGrupo": "Pro", "ordemNoGrupo": 1 }
-> 200 { ..., "sku", "preco", "custoAquisicao", "margemPercentual": 35.00, "estoqueFisico",
         "disponivel": 12, "grupo": {"id": 3, "nome": "Falcon X2"}, "rotuloNoGrupo": "Pro" }
-> 400 errors[].field = "sku"              SKU repetido
-> 400 errors[].field = "preco"            ATIVO sem preço ou preço <= 0
-> 400 errors[].field = "pesoEmbalagemKg"  ATIVO sem peso/dimensões (RN18)
-> 400 errors[].field = "rotuloNoGrupo"    grupo sem rótulo, ou rótulo repetido no grupo
   RASCUNHO aceita tudo vazio (D28). estoqueFisico do PUT é ignorado.

POST /kits  { ...produto sem estoqueFisico..., "componentes": [ {"idProduto": 1, "quantidade": 1},
              {"idProduto": 9, "quantidade": 2} ] }
-> 400 errors[].field = "componentes[1].idProduto"  componente é kit, ou repetido
-> 400 errors[].field = "componentes"               kit ATIVO sem componente

GET POST /grupos-produto?nome=&page=&pageSize=   { "nome", "slug", "descricaoCurta", "ativo" }
GET PUT DELETE /grupos-produto/{id}  -> DELETE com produtos: 409

GET /produtos?busca=&vendavel=true  busca por nome ou SKU; cada item com "sku", "preco",
                                    "estoqueFisico", "tipo", "grupo"

POST /promocoes, /cupons: "idsVariantes" vira "idsProdutos"
POST /estoque/movimentos: "idVariante" vira "idProduto" (kit: 400)
GET /variantes, /variantes/{id}: removidas (404)
```

### Protótipo

[`admin/produto-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/produto-novo.html), admin/acessorio-novo.html, admin/kit-novo.html, admin/grupos.html, admin/grupo-novo.html, admin/estoque.html, admin/estoque-movimento.html, admin/promocao-nova.html, admin/cupom-novo.html; loja: produto.html (seletor de versão) e drones.html (card do grupo)

### Backend (padrão SGA)

- [ ] Entidades herdando `DefaultEntity`; `Kit` na herança JOINED com discriminador 3
- [ ] `TipoProduto.KIT` na rota `/tipos-produto` (D31)
- [ ] Repository, DTOs `record` com Bean Validation, mapper, service `@Transactional` e resource com `PageResponse`
- [ ] Erros no formato Problem (400, 404, 409)
- [ ] Carga de dados (`import.sql`) refeita sem variantes
- [ ] Diagramas `.puml` (já atualizados na D40)
- [ ] Testes de variante reescritos para produto, kit e grupo; `issue-produto-sem-variante` em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] Cadastro de drone e acessório com o bloco "Preço, estoque e embalagem" e o bloco "Página de venda" (grupo, rótulo, ordem), no lugar da tabela de variantes
- [ ] Telas novas: cadastro de kit, listagem e cadastro de grupos
- [ ] Estoque, movimento, promoção, cupom, pedido e dashboard selecionando e mostrando produto
- [ ] Segue o protótipo e o `DESIGN.md`, conferido no desktop e no celular (360 a 480px, D20)
- [ ] Specs (`*.spec.ts`) dos services alterados

### Critérios de aceite

- [ ] Nenhuma referência a `VarianteProduto` no backend, no frontend nem na carga de dados
- [ ] SKU único; produto ATIVO sem preço ou embalagem não publica (RN18)
- [ ] Rótulo obrigatório e único dentro do grupo
- [ ] Kit sem estoque próprio, componente nunca é kit, disponibilidade derivada
- [ ] Pedido, movimento, promoção e cupom apontam para produto
- [ ] Testes já entregues (#9, #10, #13, #14, #15, #16, #18) migrados e passando; mocks do `frontend/e2e/conformidade/telas.ts` sem variante

### Testes

Backend: reescrever `catalogo/VarianteResourceTest` como `ProdutoVendavelResourceTest` e `GrupoProdutoResourceTest`, e ajustar `KitResourceTest`, `PromocaoResourceTest`, `CupomResourceTest`, os de estoque, pedido (#16) e dashboard (#18, `estoqueBaixo` por produto), a partir deste contrato, antes do código. Rodar: `./mvnw test -Dgroups=issue-produto-sem-variante`.

### Depende de

#9, #10
