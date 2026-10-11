## O que muda

<!-- Resumo da mudança. -->

Closes #

## Como testar

<!-- Passos para o revisor conferir. -->

1. 

## Checklist

- [ ] Commits em Conventional Commits, em português, com escopo (ex.: `feat(catalogo): ...`)
- [ ] Testes passando localmente (`./mvnw verify` no backend / `ng test` e `ng build` no frontend)
- [ ] Segue o diagrama de classes e o padrão SGA (`DefaultEntity`, DTO record, Panache)
- [ ] Diagrama `.puml` atualizado, se o modelo mudou

**Se mexe em tela do frontend**
- [ ] Segue o protótipo (`docs/design/README.md` tem o link de cada tela)
- [ ] Conferido no navegador do desktop e no navegador do celular (360–480px)

**Se altera o protótipo (`docs/design/`)**
- [ ] Testei o protótipo localmente
- [ ] Atualizei o `DESIGN.md`, se mudou cor, fonte, espaçamento ou componente
- [ ] Só dados fictícios
