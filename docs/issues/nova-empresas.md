---
numero: 60
titulo: "[atendimento] Proposta comercial para empresas"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:atendimento", "trilha:b"]
fase: 5
---

### Módulo

atendimento

### Classes do diagrama

LeadEmpresa (nova)

### Diagrama de referência

`docs/diagramas/aeria-atendimento.puml`

### Requisitos, regras e decisões (PRD)

D36

### O que entregar

A tela `empresas.html` tem o formulário "Solicitar proposta", sem entidade no modelo. Criar `LeadEmpresa` (razão social, CNPJ, nome, e-mail e telefone do contato, segmento, quantidade estimada, mensagem, status NOVO/EM_CONTATO/FECHADO/DESCARTADO) e atualizar `aeria-atendimento.puml`.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas do admin exigem token de administrador com permissão no módulo (#4, #5); rotas `/conta` exigem cliente logado.

```
POST /leads-empresas (público) { "razaoSocial","cnpj","nomeContato","emailContato","telefoneContato",
                                     "segmento","quantidadeEstimada","mensagem" } -> 204
-> 400 errors[].field = "cnpj" | "emailContato"
GET /admin/leads-empresas?idStatus= -> PageResponse ; PATCH /admin/leads-empresas/{id}/status -> 204
```

### Protótipo

[`empresas.html`](https://davirsuassuna.github.io/droneshop/prototipo/empresas.html)

### Backend (padrão SGA)

- [ ] Entidade herdando `DefaultEntity` (`id`, `dataCadastro`, `dataAlteracao`)
- [ ] Enums com id e nome, conversor JPA e rota de leitura (D31)
- [ ] Repository (`PanacheRepository`)
- [ ] `XxxRequestDTO` e `XxxResponseDTO` como `record`, Bean Validation na entrada e mapper
- [ ] Service com `@Transactional` e validações de negócio
- [ ] Resource REST com `page`/`pageSize` e resposta `PageResponse`
- [ ] Erros no formato Problem (400, 404, 409, 503), como no `exception/`
- [ ] Carga de dados em `import-b.sql` (sem id fixo; nunca no `import.sql`)
- [ ] Diagrama `.puml` atualizado, se o modelo mudou
- [ ] Testes `@Tag("issue-empresas")` passando e `issue-empresas` acrescentada em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] Tela(s) do storefront integradas à API
- [ ] Rotas do Angular conforme `frontend/e2e/conformidade/telas.ts`: `/empresas` (empresas.html)
- [ ] Conformidade com o protótipo passando (`npm run e2e -- --grep @issue-empresas`: estrutura, estilo e celular sem rolagem horizontal, D20, D38) e `issue-empresas` acrescentada em `frontend/e2e/telas-entregues.txt`
- [ ] Specs (`*.spec.ts`) dos services novos

### Critérios de aceite

- [ ] CNPJ validado
- [ ] Diagrama atualizado com `LeadEmpresa`
- [ ] Formulário de `empresas.html` integrado

### Testes

Backend: `atendimento/LeadEmpresaResourceTest` em `backend/src/test/java/br/com/droneshop/`, escritos a partir deste contrato, antes do código. Rodar: `./mvnw test -Dgroups=issue-empresas`.

Frontend: conformidade das telas com o protótipo em `frontend/e2e` (D38). Rodar: `npm run e2e -- --grep @issue-empresas`.

### Depende de

Nenhuma

### Trilha

**B**, posição 19 de 20. Anterior: #42. Próxima: #59. Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
