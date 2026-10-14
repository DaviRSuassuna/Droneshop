---
numero: null
titulo: "[administracao] Ações em lote, reordenação de categorias, exportação CSV e etiqueta de pedido"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:administracao"]
fase: 1
---

### Módulo

administracao

### Classes do diagrama

Produto, Categoria, Marca, Fornecedor, Pedido

### Diagrama de referência

`docs/diagramas/aeria-catalogo.puml`, `docs/diagramas/aeria-pedido.puml`

### Requisitos, regras e decisões (PRD)

D36

### O que entregar

Rotas que as telas do admin pedem e não tinham issue (API.md, seção "Telas que pedem rota sem issue"): contagem de produtos nas listagens, ações em lote de produto, reordenação de categorias, exportação CSV e etiqueta de envio.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas do admin exigem token de administrador com permissão no módulo (#4, #5); rotas `/conta` exigem cliente logado.

```
GET /marcas, /categorias, /subcategorias, /fornecedores: cada item ganha "quantidadeProdutos"

PATCH /produtos/lote { "ids": [10, 11], "acao": "ATIVAR"|"INATIVAR"|"MOVER_SUBCATEGORIA"|"REAJUSTAR_PRECO",
                       "idSubcategoria": 3, "percentual": 5.0 }
-> 200 { "processados": 2, "falhas": [ { "id": 11, "motivo": "..." } ] }
   ATIVAR respeita as mesmas validações do cadastro; REAJUSTAR_PRECO vale para todas as variantes

PATCH /categorias/ordem { "ids": [3, 1, 2] } -> 204   (ordemExibicao = posição)

GET /marcas/exportar, /fornecedores/exportar, /produtos/exportar, /estoque/exportar, /pedidos/exportar
   (mesmos filtros da listagem) -> 200 text/csv; separador ";", UTF-8 com BOM, cabeçalho na 1ª linha

GET /pedidos/{id}/etiqueta -> 200 application/pdf (remetente da configuração, destinatário congelado)
-> 409 pedido sem entrega
```

### Protótipo

[`admin/produtos.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/produtos.html) (ações em lote, Exportar), [`admin/categorias.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/categorias.html) (Reordenar), [`admin/marcas.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/marcas.html), [`admin/fornecedores.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/fornecedores.html), [`admin/estoque.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/estoque.html), [`admin/pedidos.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/pedidos.html) (Imprimir etiqueta)

### Backend (padrão SGA)

- [ ] Entidade herdando `DefaultEntity` (`id`, `dataCadastro`, `dataAlteracao`)
- [ ] Enums com id e nome, conversor JPA e rota de leitura (D31)
- [ ] Repository (`PanacheRepository`)
- [ ] `XxxRequestDTO` e `XxxResponseDTO` como `record`, Bean Validation na entrada e mapper
- [ ] Service com `@Transactional` e validações de negócio
- [ ] Resource REST com `page`/`pageSize` e resposta `PageResponse`
- [ ] Erros no formato Problem (400, 404, 409, 503), como no `exception/`
- [ ] Carga de dados (`import.sql`) atualizada
- [ ] Diagrama `.puml` atualizado, se o modelo mudou
- [ ] Testes `@Tag("issue-lote-csv")` passando e `issue-lote-csv` acrescentada em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] Tela(s) do admin integradas à API
- [ ] Segue o protótipo e o `DESIGN.md`, conferido no desktop e no celular (360 a 480px, D20)
- [ ] Specs (`*.spec.ts`) dos services novos

### Critérios de aceite

- [ ] Listagens mostram a quantidade de produtos
- [ ] Lote informa o que falhou sem abortar o resto
- [ ] CSV abre certo no Excel (BOM e ponto e vírgula)
- [ ] Etiqueta usa o endereço congelado

### Testes

Backend: `administracao/AcoesAdminResourceTest` em `backend/src/test/java/br/com/droneshop/`, escritos a partir deste contrato, antes do código. Rodar: `./mvnw test -Dgroups=issue-lote-csv`.

### Depende de

#9, #16
