---
numero: 88
titulo: "[estoque] Drones registrados na conta do cliente, com série, garantia e registro ANAC"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:estoque", "trilha:a"]
fase: 4
---

### Módulo

estoque (com leitura de pedido)

### Classes do diagrama

Nenhuma nova: `UnidadeIndividual` (#34) ligada ao `ItemPedido` do cliente

### Diagrama de referência

`docs/diagramas/aeria-estoque.puml`

### Requisitos, regras e decisões (PRD)

RN12, RF56, D40

### O que entregar

O perfil do cliente no protótipo novo (`conta-perfil.html`) tem o bloco "Drones registrados — Usado para garantia e SISANT", com cada drone comprado, o número de série, a data da compra e até quando vai a garantia.

A lista vem das unidades individuais (#34) já vinculadas a itens de pedidos do próprio cliente (status VENDIDA). O cliente pode informar o registro na ANAC/SISANT de cada unidade, que já existe em `UnidadeIndividual.registroAnac`. Não há cadastro manual de drone comprado fora da loja.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas do admin exigem token de administrador com permissão no módulo (#4, #5); rotas `/conta` exigem cliente logado.

```
GET /conta/drones
-> 200 [ { "idUnidade": 7, "produto": { "id", "nome", "slug" }, "numeroSerie": "SKL-NM3-88214",
           "registroAnac": "PP-1234567", "dataCompra": "2026-07-02",
           "validadeGarantia": "2027-07-07", "pedido": { "id", "numero" } } ]
   só unidades VENDIDA de pedidos do cliente do token; mais recente primeiro; sem unidade -> []
-> 401 sem token

PATCH /conta/drones/{idUnidade} { "registroAnac": "PP-1234567" } -> 204
-> 400 errors[].field = "registroAnac"   acima de 40 caracteres
-> 404 unidade inexistente ou de outro cliente
   texto em branco limpa o registro
```

### Protótipo

[`conta-perfil.html`](https://davirsuassuna.github.io/droneshop/prototipo/conta-perfil.html)

### Backend (padrão SGA)

- [ ] DTOs `record` (`MeuDroneResponseDTO`, `RegistroAnacRequestDTO`) com Bean Validation e mensagens com `(back)`
- [ ] Consulta no repository de unidade filtrando pelo cliente do pedido
- [ ] Service com `@Transactional` no PATCH
- [ ] Erros no formato Problem (400, 401, 404)
- [ ] Testes `@Tag("issue-meus-drones")` passando e `issue-meus-drones` acrescentada em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] Bloco "Drones registrados" em `/conta/perfil`, com edição do registro ANAC
- [ ] Conformidade de `conta-perfil.html` passando (`npm run e2e -- --grep @issue-19`), no desktop e no celular (D20)
- [ ] Spec do service novo

### Critérios de aceite

- [ ] Cliente só vê e altera as próprias unidades
- [ ] Garantia mostrada é a `validadeGarantia` calculada na expedição (#34)

### Testes

Backend: `estoque/MeusDronesResourceTest` em `backend/src/test/java/br/com/droneshop/`, escrito a partir deste contrato, antes do código. Passa pelo checkout (#26, #27) e pela expedição com série (#34). Rodar: `./mvnw test -Dgroups=issue-meus-drones`.

Frontend: conformidade de `conta-perfil.html` (`npm run e2e -- --grep @issue-19`).

### Depende de

#19, #29, #34

### Trilha

**A**, posição 22 de 23. Anterior: #86. Próxima: #89. Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
