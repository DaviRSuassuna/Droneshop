## O que muda

<!-- Resumo da mudança. -->

Closes #

## Como testar

<!-- Passos para o revisor conferir. -->

1. 

## Trilha

<!-- A ou B (docs/agentes/README.md). -->

Trilha: 

**Decisão proposta** (o supervisor numera e escreve no PRD): <!-- ou "nenhuma" -->

## Checklist

- [ ] Commits em Conventional Commits, em português, com escopo (ex.: `feat(catalogo): ...`)
- [ ] Testes passando localmente (`./mvnw verify` no backend / `ng test` e `ng build` no frontend)
- [ ] Segue o diagrama de classes e o padrão SGA (`DefaultEntity`, `XxxRequestDTO` record, mapper, `PageResponse`, Panache)
- [ ] Diagrama `.puml` atualizado, se o modelo mudou
- [ ] Só mexi nos módulos e telas da minha trilha; carga de dados em `import-a.sql` ou `import-b.sql`, nunca no `import.sql`
- [ ] Etiquetas em `issues-entregues.txt` e `telas-entregues.txt` abaixo da linha da minha trilha

**Se mexe em tela do frontend**
- [ ] Segue o protótipo (`docs/design/README.md` tem o link de cada tela)
- [ ] Conferido no navegador do desktop e no navegador do celular (360 a 480px)

**Se altera o protótipo (`docs/design/`)**
- [ ] Testei o protótipo localmente
- [ ] Atualizei o `DESIGN.md`, se mudou cor, fonte, espaçamento ou componente
- [ ] Só dados fictícios
