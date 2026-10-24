---
numero: null
titulo: "[pedido] Painel de pedidos: indicadores, busca por CPF/CNPJ, filtro por pagamento e etiquetas em lote"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:pedido"]
fase: 4
---

### Módulo

pedido

### Classes do diagrama

Nenhuma nova: `Pedido`, `Pagamento` (#27) e `Devolucao` (#37)

### Diagrama de referência

`docs/diagramas/aeria-pedido.puml`

### Requisitos, regras e decisões (PRD)

RF46, D12, D38

### O que entregar

A listagem de pedidos do protótipo novo (`admin/pedidos.html`) mostra mais do que a #16 e a #61 entregam:

1. **Indicadores no topo.** "A separar" (pedidos PAGO), "Aguardando pagamento" (PENDENTE, com o valor em aberto), "Em trânsito" (ENVIADO, com o prazo médio de entrega dos últimos 30 dias) e "Devoluções abertas" (devoluções que não estão RECUSADA nem CONCLUIDA). Os números são de agora, como as abas de `/pedidos/resumo-status`.
2. **Busca por CPF/CNPJ.** O campo "Buscar por número, cliente ou CPF/CNPJ" passa a achar também pelo documento do cliente, com ou sem máscara.
3. **Filtro e coluna de pagamento.** Filtro pelo método do pagamento vigente (Pix, Cartão, Boleto) e coluna "Pagamento" na listagem. Pedido sem pagamento mostra a coluna vazia.
4. **Imprimir etiquetas.** Um PDF com uma etiqueta por pedido selecionado, no mesmo modelo da etiqueta individual da #61.

O filtro "Canal" e o número de parcelas ("Cartão 10×") não entram: só existe a loja online e a #27 não tem parcelamento. Os dois saem do protótipo na issue `nova-ajuste-prototipo`.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas do admin exigem token de administrador com permissão no módulo (#4, #5); rotas `/conta` exigem cliente logado.

```
GET /pedidos/indicadores
-> 200 { "aSeparar": 23,
         "aguardandoPagamento": { "quantidade": 18, "valorEmAberto": 142000.00 },
         "emTransito": { "quantidade": 41, "prazoMedioDias": 4.2 },
         "devolucoesAbertas": 3 }
   prazoMedioDias: média de dias entre ENVIADO e ENTREGUE (histórico, #17) dos pedidos entregues
                   nos últimos 30 dias, uma casa; nenhum entregue -> null

GET /pedidos?idStatus=&dataInicio=&dataFim=&busca=&idMetodoPagamento=&page=&pageSize=
   busca: número do pedido, nome do cliente ou CPF/CNPJ (pontuação ignorada)
   idMetodoPagamento: método do pagamento vigente (#27); filtro vazio é ignorado
-> 200 PageResponse; cada item ganha "metodoPagamento": { "id": 1, "nome": "PIX" } | null
   GET /pedidos/exportar aceita os mesmos filtros (#61)

POST /pedidos/etiquetas { "ids": [12, 15] }
-> 200 application/pdf, uma página por pedido, na ordem dos ids
-> 400 errors[].field = "ids"   lista vazia
-> 404 algum pedido inexistente
-> 409 algum pedido sem endereço de entrega congelado; o Problem cita os números; nenhum PDF é gerado
```

### Protótipo

[`admin/pedidos.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/pedidos.html)

### Backend (padrão SGA)

- [ ] DTO `record` dos indicadores e `metodoPagamento` na listagem
- [ ] Filtros no repository (documento normalizado, método do pagamento vigente); filtros vazios ignorados
- [ ] Etiquetas em lote reaproveitando o serviço de etiqueta da #61
- [ ] Erros no formato Problem (400, 404, 409)
- [ ] Testes `@Tag("issue-pedidos-painel")` passando e `issue-pedidos-painel` acrescentada em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] Indicadores, busca, filtro de pagamento, coluna "Pagamento" e "Imprimir etiquetas" (com a seleção da tabela)
- [ ] Em `frontend/e2e/conformidade/telas.ts` (tela `admin/pedidos.html`), tirar do `ignorar` "Imprimir etiquetas", "Devoluções abertas", "Buscar por número, cliente ou CPF/CNPJ", "Período: 30 dias" e "Pagamento"
- [ ] Conformidade da tela passando (`npm run e2e -- --grep @issue-16`), no desktop e no celular (D20)
- [ ] Spec do `pedido.service` cobrindo indicadores, filtro e etiquetas

### Critérios de aceite

- [ ] Indicadores batem com a contagem por status
- [ ] CPF/CNPJ acha o pedido com ou sem máscara
- [ ] Filtro por método usa só o pagamento vigente
- [ ] Etiquetas em lote: tudo ou nada; pedido sem endereço congelado impede o PDF inteiro

### Testes

Backend: `pedido/PainelPedidosResourceTest` em `backend/src/test/java/br/com/droneshop/`, escrito a partir deste contrato, antes do código. Busca, indicadores e etiquetas usam os pedidos da carga de dados (#16); o filtro por pagamento passa pelo checkout (#26, #27). Rodar: `./mvnw test -Dgroups=issue-pedidos-painel`.

Frontend: conformidade de `admin/pedidos.html` (`npm run e2e -- --grep @issue-16`).

### Depende de

#16, #61, #27, #37
