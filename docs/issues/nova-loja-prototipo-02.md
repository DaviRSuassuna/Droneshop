---
numero: null
titulo: "[design] Casca da loja no protótipo 02: menu, faixa de promoção, mega menu, rodapé e efeitos"
labels: ["tipo:feature", "camada:frontend", "modulo:catalogo", "trilha:b"]
fase: 2
---

### Módulo

layout da loja (`frontend/src/app/layout/storefront`) e estilos globais (`frontend/src/styles`)

### Classes do diagrama

Nenhuma

### Diagrama de referência

Nenhum

### Requisitos, regras e decisões (PRD)

D20, D38, D41, RNF08

### O que entregar

A D41 trocou a referência das telas pelo protótipo 02. Esta issue leva para o Angular a parte da loja que **todas** as telas usam, para as duas trilhas construírem as telas da loja já em cima dela:

1. **Menu** (`storefront-nav`): some ao descer, volta ao subir e encolhe depois do topo; mega menu de Drones com as categorias ativas (`GET /categorias`); busca que leva para `/drones?q=<termo>` (as sugestões enquanto digita ficam para a #43).
2. **Faixa de promoção** acima do menu, com texto e cupom fixos no próprio componente e botão de fechar que vale para a sessão. Texto vindo da configuração da loja fica para uma issue futura.
3. **Rodapé** (`storefront-footer`) e menu da conta (`account-menu`) iguais ao protótipo 02.
4. **Camada de efeitos** em `frontend/src/styles` e em diretivas de `shared/directives`, um efeito por arquivo: revelação de título, brilho de selo, faixa de marcas, brilho do botão principal, "pop" da sacola, check animado, rótulo de campo, spinner de botão, borda do card acendendo sob o mouse. Efeitos só da capa (luz, raios, borda girando, depoimentos) ficam para a #21.
5. **Reduzir movimento:** com `prefers-reduced-motion: reduce`, laços param e entradas aparecem prontas (`DESIGN.md`, seção Movimento). O `liberarMovimento` do protótipo não vem para o Angular.

### Contrato de API

Nenhum novo. Usa `GET /categorias?status=2` (já existe).

### Protótipo

[`index.html`](https://davirsuassuna.github.io/droneshop/prototipo/index.html) (menu, faixa, mega menu, rodapé), [`conta-perfil.html`](https://davirsuassuna.github.io/droneshop/prototipo/conta-perfil.html) (menu da conta). Efeitos: `assets/spell.*` e `assets/navbar-aceternity.*`.

### Frontend

- [ ] Menu, faixa, mega menu e rodapé como no protótipo 02, no desktop e de 360 a 480px (D20)
- [ ] Um efeito por arquivo, com nome igual ao bloco do protótipo, desligável sem quebrar a tela
- [ ] "Reduzir movimento" respeitado
- [ ] Specs dos componentes e diretivas novos

### Critérios de aceite

- [ ] A casca da loja segue o protótipo 02 em todas as rotas da loja
- [ ] Nenhuma tela da loja perde conteúdo com os efeitos desligados
- [ ] Conformidade: as telas da loja já em `telas-entregues.txt` continuam passando

### Testes

Specs do Angular dos componentes de `layout/storefront` e das diretivas de efeito.

### Depende de

Nenhuma

### Trilha

**B**, posição 1 de 20. Anterior: nenhuma (é a primeira). Próxima: admin no protótipo 02 (`nova-admin-prototipo-02.md`). Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
