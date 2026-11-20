---
numero: 83
titulo: "[design] Tirar do protótipo o que contradiz o modelo: permissões por tela, rascunho de usuário, canal, conversão e parcelas"
labels: ["tipo:docs", "camada:frontend", "trilha:b"]
fase: null
---

### Módulo

design (`docs/design/prototipo`)

### Classes do diagrama

Nenhuma

### Diagrama de referência

Nenhum: o modelo fica como está

### Requisitos, regras e decisões (PRD)

D35, D36, D38, RF42

### O que entregar

Na revisão do protótipo novo contra as issues, cinco elementos não batem com o modelo. Como na D36, o modelo vence e o protótipo muda. Hoje a conformidade ignora esses textos (`ignorar` em `frontend/e2e/conformidade/telas.ts`).

| Tela | Sai do protótipo | Por quê | Fica no lugar |
|---|---|---|---|
| `admin/usuario-novo.html` | permissões por tela (Produtos, Marcas, Categorias, Pedidos, Estoque, Usuários, Relatórios) | a #5 dá permissão por módulo (`ModuloSistema`) | ver, editar e excluir para os 8 módulos: Catálogo, Estoque, Pedido, Pagamento, Devolução, Atendimento, Notificação, Administração |
| `admin/usuario-novo.html` | "Salvar rascunho" e "Rascunho salvo há 1 min" | usuário é ativo ou inativo (D35); não tem status de rascunho | só "Cancelar" e "Criar usuário" |
| `admin/pedidos.html` | filtro "Canal" | só existe a loja online | nada |
| `admin/pedidos.html` | parcelas na coluna "Pagamento" ("Cartão 10×") | a #27 não tem parcelamento | só o método: "Cartão" |
| `admin/index.html` | card "Conversão" | não há registro de visitas | os outros três cards (vendas, pedidos, ticket médio) |

Atenção: a D28 diz que o rascunho vale para todo cadastro do admin. Tirar o rascunho do usuário é uma exceção a ela e precisa ficar registrada no PRD (na D35 ou numa decisão nova).

Fluxo da D38: este PR muda só o protótipo. As telas reais já não mostram esses elementos, então as etiquetas `issue-5`, `issue-16` e `issue-18` continuam em `frontend/e2e/telas-entregues.txt`.

### Contrato de API

Nenhum.

### Protótipo

[`admin/usuario-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/usuario-novo.html), [`admin/pedidos.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/pedidos.html), [`admin/index.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/index.html)

### Frontend

- [ ] Protótipo alterado nas três páginas (`docs/design/prototipo`), sem mudar tokens
- [ ] `npm run prototipo:verificar` passando
- [ ] Em `frontend/e2e/conformidade/telas.ts`, tirar do `ignorar` os textos que saíram: "Salvar rascunho", "Produtos", "Marcas", "Categorias", "Pedidos", "Usuários" e "Relatórios" (`admin/usuario-novo.html`); "Canal" (`admin/pedidos.html`); "Conversão" (`admin/index.html`)
- [ ] PRD: registrar a exceção à D28 para usuário e citar esta issue na seção "Protótipo × modelo" do `docs/API.md`

### Critérios de aceite

- [ ] Os cinco elementos da tabela não aparecem mais no protótipo
- [ ] Conformidade de `admin/usuario-novo.html`, `admin/pedidos.html` e `admin/index.html` continua passando sem esses itens no `ignorar`

### Testes

Sem teste de backend. Conformidade das três telas: `npm run e2e -- --grep "@issue-5|@issue-16|@issue-18"`.

### Depende de

Nenhuma

### Trilha

**B**, posição 6 de 20. Anterior: #84. Próxima: #21. Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
