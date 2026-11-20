---
numero: 86
titulo: "[compra] Favoritos: queda de preço, mover tudo para o carrinho e lista compartilhada"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:compra", "trilha:a"]
fase: 3
---

### Módulo

compra

### Classes do diagrama

ListaDesejos (ganha `tokenCompartilhamento`), ItemListaDesejos (ganha `precoAoAdicionar`)

### Diagrama de referência

`docs/diagramas/aeria-compra.puml`

### Requisitos, regras e decisões (PRD)

RN17, D40

### O que entregar

A tela de favoritos do protótipo novo (`conta-favoritos.html`) mostra três coisas que a #22 não tem:

1. **Queda de preço.** "6 produtos salvos · 2 com queda de preço": cada item guarda o preço do produto quando entrou na lista e avisa quando o preço atual está menor.
2. **Mover tudo para o carrinho.** Leva cada item disponível para o carrinho do cliente, com quantidade 1, e tira da lista. O que não pode ir (produto não ATIVO ou sem estoque) fica na lista e volta com o motivo.
3. **Compartilhar lista.** Gera um link público, só de leitura, que mostra os produtos ATIVO da lista. O cliente pode desligar o link; o token antigo deixa de valer.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas do admin exigem token de administrador com permissão no módulo (#4, #5); rotas `/conta` exigem cliente logado.

```
GET /conta/desejos -> cada item ganha "precoAoAdicionar" e "quedaDePreco"
   quedaDePreco = preço atual do produto < precoAoAdicionar

POST /conta/desejos/mover-para-carrinho
-> 200 { "carrinho": { ...carrinho ATIVO do cliente (#24)... },
         "naoAdicionados": [ { "idProduto": 9, "motivo": "Sem estoque" } ] }
   cada item disponível entra com quantidade 1 (soma se já estiver no carrinho) e sai da lista
   lista vazia -> 200 com naoAdicionados = []

POST /conta/desejos/compartilhar -> 200 { "token": "uuid", "url": "/lista/<token>" }
   repetir devolve o mesmo token enquanto o link estiver ligado
DELETE /conta/desejos/compartilhar -> 204   (o token deixa de valer)

GET /loja/listas/{token}   (público)
-> 200 { "nomeCliente": "Marina", "itens": [ { "produto": { "id","nome","slug","preco","disponivel" } } ] }
   nomeCliente é só o primeiro nome (LGPD); só produtos ATIVO (RN17)
-> 404 token inexistente ou desligado
Sem token de cliente nas rotas /conta -> 401
```

### Protótipo

[`conta-favoritos.html`](https://davirsuassuna.github.io/droneshop/prototipo/conta-favoritos.html)

### Backend (padrão SGA)

- [ ] Campos novos em `ListaDesejos` e `ItemListaDesejos`; `precoAoAdicionar` gravado no POST da #22
- [ ] DTOs `record`, mapper e service com `@Transactional` nas escritas
- [ ] Mover para o carrinho reaproveitando o serviço de carrinho da #24 (mesmas validações)
- [ ] Erros no formato Problem (401, 404)
- [ ] Diagrama `aeria-compra.puml` atualizado
- [ ] Testes `@Tag("issue-favoritos-compartilhar")` passando e `issue-favoritos-compartilhar` acrescentada em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] Contador de queda de preço, "Mover tudo para o carrinho" e "Compartilhar lista" (copiar link e desligar)
- [ ] Página pública da lista compartilhada, no visual da loja
- [ ] Conformidade de `conta-favoritos.html` passando (`npm run e2e -- --grep @issue-22`), no desktop e no celular (D20)
- [ ] Spec do service de lista de desejos

### Critérios de aceite

- [ ] Queda de preço compara com o preço do dia em que o item entrou
- [ ] Mover leva só o que está disponível e informa o resto
- [ ] Link compartilhado não mostra dado do cliente além do primeiro nome
- [ ] Desligar o link invalida o token

### Testes

Backend: `compra/FavoritosCompartilharResourceTest` em `backend/src/test/java/br/com/droneshop/`, escrito a partir deste contrato, antes do código. Rodar: `./mvnw test -Dgroups=issue-favoritos-compartilhar`.

Frontend: conformidade de `conta-favoritos.html` (`npm run e2e -- --grep @issue-22`).

### Depende de

#22, #24

### Trilha

**A**, posição 21 de 23. Anterior: #45. Próxima: #88. Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
