---
numero: 85
titulo: "[administracao] Dashboard: comparação com o período anterior, vendas por dia e mais vendidos"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:administracao", "trilha:b"]
fase: 1
---

### Módulo

administracao

### Classes do diagrama

Nenhuma nova: indicadores calculados a partir de `Pedido` e `ItemPedido`

### Diagrama de referência

`docs/diagramas/aeria-administrativo.puml` e `docs/diagramas/aeria-pedido.puml`

### Requisitos, regras e decisões (PRD)

RF46, D19, D38

### O que entregar

O dashboard do protótipo novo (`admin/index.html`) mostra mais do que a #18 entrega. Esta issue completa a mesma rota com o que falta na tela:

1. **Comparação com o período anterior.** Faturamento, quantidade de pedidos e ticket médio do período anterior de mesmo tamanho, imediatamente antes de `dataInicio`, e a variação em % de cada um ("▲ +12,4% vs. 30 dias anteriores"). Sem valor no período anterior, a variação é `null`.
2. **Vendas por dia.** Um ponto por dia do período, inclusive os dias sem venda, com faturamento e pedidos do dia e do dia correspondente no período anterior (gráfico "Receita / Pedidos", linhas "Últimos 30 dias" e "Período anterior").
3. **Pedidos aguardando ação.** Quantidade e até 5 pedidos nos status PENDENTE, PAGO ou SEPARACAO, do mais antigo para o mais novo ("18 aguardando ação" e o bloco "Pedidos aguardando ação").
4. **Mais vendidos.** Até 5 produtos com maior receita no período, com unidades, receita e participação no faturamento (%).
5. **Exportar relatório.** CSV com as vendas por dia, no padrão da #61.

Mesma regra de faturamento da #18: pedidos PAGO, SEPARACAO, ENVIADO ou ENTREGUE criados no período (datas inclusivas). O seletor de período da tela ("Últimos 30 dias", "Últimos 7 dias", "Este ano") só calcula `dataInicio` e `dataFim` no front.

O card "Conversão" não entra: não existe registro de visitas no modelo. Ele sai do protótipo na issue `nova-ajuste-prototipo`.


**Protótipo 02 (D41):** entram também o card de receita por categoria (rosca com legenda e valores) e o gráfico de vendas desenhado na entrada e lido no hover (linha guia, ponto e balão), blocos B, C e H de `assets/admin-fx.*`. O contrato abaixo ganha a receita por categoria no período, no mesmo formato dos outros indicadores. Ao entregar, tire "Receita por categoria" do `ignorar` da tela `admin/index.html` em `frontend/e2e/conformidade/telas.ts`.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas do admin exigem token de administrador com permissão no módulo (#4, #5); rotas `/conta` exigem cliente logado.

```
GET /admin/dashboard?dataInicio=2026-10-01&dataFim=2026-10-30
-> 200 { ...campos da #18...,
         "comparacao": { "periodoAnterior": { "dataInicio": "2026-09-01", "dataFim": "2026-09-30" },
                         "faturamento": 1263000.00, "quantidadePedidos": 289, "ticketMedio": 4370.24,
                         "variacaoFaturamento": 12.4, "variacaoPedidos": 8.1, "variacaoTicketMedio": 3.9 },
         "vendasPorDia": [ { "data": "2026-10-01", "faturamento": 41200.00, "quantidadePedidos": 9,
                             "faturamentoAnterior": 38900.00, "quantidadePedidosAnterior": 8 } x 30 ],
         "quantidadeAguardandoAcao": 18,
         "pedidosAguardandoAcao": [ { "id", "numero", "cliente", "status", "valorTotal", "dataCadastro" } x até 5 ],
         "maisVendidos": [ { "produto": { "id", "nome", "sku" }, "unidades": 184,
                             "receita": 789000.00, "participacao": 22.0 } x até 5 ] }
   variacao = (atual - anterior) / anterior * 100, uma casa; anterior = 0 -> null
   vendasPorDia: um item por dia de dataInicio a dataFim, em ordem; soma de faturamento = faturamento
   maisVendidos: ordenado por receita (quantidade x preço congelado do item, RN06), maior primeiro;
                 participacao = receita / faturamento * 100, uma casa
   pedidosAguardandoAcao: status PENDENTE, PAGO ou SEPARACAO, mais antigo primeiro (independe do período)

GET /admin/dashboard/exportar?dataInicio=&dataFim=
-> 200 text/csv; separador ";", UTF-8 com BOM (#61)
   cabeçalho: data;faturamento;pedidos;faturamentoAnterior;pedidosAnterior
-> 400 errors[].field = "dataFim"   antes de dataInicio
```

### Protótipo

[`admin/index.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/index.html)

### Backend (padrão SGA)

- [ ] DTOs `record` (comparação, ponto do dia, mais vendido) no `DashboardResponseDTO`
- [ ] Consultas agregadas no repository, sem carregar pedidos um a um
- [ ] Exportação com o utilitário de CSV da #61
- [ ] Erros no formato Problem (400)
- [ ] Testes `@Tag("issue-dashboard-indicadores")` passando e `issue-dashboard-indicadores` acrescentada em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] Cards com variação, gráfico de vendas por dia, "Pedidos aguardando ação", "Mais vendidos" e "Exportar relatório" no dashboard
- [ ] Em `frontend/e2e/conformidade/telas.ts` (tela `admin/index.html`), tirar do `ignorar` "Exportar relatório", "Vendas no período", "Receita", "Pedidos", "Período anterior", "Pedidos aguardando ação", "Mais vendidos" e "Ver produtos", e a tabela de `tabelasSemApi`
- [ ] Conformidade da tela passando (`npm run e2e -- --grep @issue-18`), no desktop e no celular (D20)
- [ ] Spec do `dashboard.service` cobrindo a exportação

### Critérios de aceite

- [ ] Período anterior com o mesmo número de dias, terminando na véspera de `dataInicio`
- [ ] Um ponto por dia, inclusive sem venda, e a soma bate com o faturamento
- [ ] Mais vendidos por receita, no máximo 5, com participação
- [ ] Aguardando ação só com PENDENTE, PAGO ou SEPARACAO, mais antigo primeiro
- [ ] CSV abre certo no Excel (BOM e ponto e vírgula)

### Testes

Backend: `administracao/DashboardIndicadoresResourceTest` em `backend/src/test/java/br/com/droneshop/`, escrito a partir deste contrato, antes do código. Usa os pedidos da carga de dados (#16). Rodar: `./mvnw test -Dgroups=issue-dashboard-indicadores`.

Frontend: conformidade de `admin/index.html` (`npm run e2e -- --grep @issue-18`).

### Depende de

#18, #61

### Trilha

**B**, posição 12 de 20. Anterior: #44. Próxima: #41. Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
