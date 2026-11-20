---
numero: null
titulo: "[design] Admin no protótipo 02: barra flutuante com menus agrupados, largura de 1320px e efeitos"
labels: ["tipo:feature", "camada:frontend", "modulo:administracao", "trilha:b"]
fase: 1
---

### Módulo

layout do admin (`frontend/src/app/layout/admin`) e estilos do admin (`frontend/src/styles/_admin*.scss`)

### Classes do diagrama

Nenhuma

### Diagrama de referência

Nenhum

### Requisitos, regras e decisões (PRD)

D20, D38, D41

### O que entregar

A D41 trocou a referência pelo protótipo 02, que muda a casca de todas as telas do admin já entregues. Esta issue traz essa casca para o Angular e devolve a conformidade das telas entregues:

1. **Barra do admin** (`admin-nav`) flutuante e arredondada, com os menus agrupados como no protótipo 02 (no protótipo ela é montada por `admin-fx.js`; aqui vira HTML do componente).
2. **Largura:** conteúdo e barra limitados a 1320px e centralizados, como na loja.
3. **Efeitos do admin** em `frontend/src/styles` e `shared/directives`, um por arquivo: entrada em cascata dos cards, medidores crescendo do zero, status "aguardando" pulsando, linhas de tabela entrando e acendendo no hover, borda do card acendendo sob o mouse, indicador "ao vivo".
4. **Reduzir movimento** respeitado (`DESIGN.md`, seção Movimento).
5. **Conformidade:** etiquetas das telas do admin que tenham saído de `frontend/e2e/telas-entregues.txt` com a troca do protótipo voltam neste PR.

Fica fora: card de receita por categoria e gráfico desenhado e lido no hover do dashboard (vão para a #85, que tem os dados).

### Contrato de API

Nenhum.

### Protótipo

Todas as telas de [`admin/`](https://davirsuassuna.github.io/droneshop/prototipo/admin/index.html). Efeitos: `assets/admin-fx.*` (blocos A, D, E, F, G, I, J e K).

### Frontend

- [ ] Barra flutuante com menus agrupados, no desktop e de 360 a 480px (D20)
- [ ] Largura máxima de 1320px
- [ ] Um efeito por arquivo, desligável sem quebrar a tela
- [ ] "Reduzir movimento" respeitado
- [ ] Etiquetas do admin de volta em `telas-entregues.txt`

### Critérios de aceite

- [ ] Todas as telas do admin já entregues passam na conformidade com o protótipo 02
- [ ] Nenhuma tela perde conteúdo com os efeitos desligados

### Testes

`npm run e2e` com as etiquetas do admin; specs de `layout/admin` e das diretivas novas.

### Depende de

Nenhuma

### Trilha

**B**, posição 2 de 20. Anterior: casca da loja (`nova-loja-prototipo-02.md`). Próxima: #87. Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
