# Aeria Drones

E-commerce de drones e acessórios, com **loja** (storefront) para o cliente pesquisar, comparar e comprar, e **painel administrativo** para a operação cadastrar catálogo, controlar estoque, processar pedidos, tratar devoluções e atender clientes.

> Projeto de estudo, não uma operação comercial. O escopo funcional e o modelo de dados são completos, mas pagamento roda em **sandbox** (Mercado Pago) e a NF-e é **simulada**. O e-mail é a única integração com envio real.

Drone não é um produto de varejo comum: tem alto valor unitário, ficha técnica extensa, garantia relevante e, em parte dos modelos, registro na ANAC. Por isso o projeto prevê rastreio por número de série, catálogo com especificações comparáveis e um pós-venda mais exigente.

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 25, Quarkus 3, Maven, Hibernate ORM com Panache, Hibernate Validator, Quarkus REST + Jackson, REST Client, SmallRye JWT, SmallRye OpenAPI |
| Banco | PostgreSQL 16 (via Docker Compose) |
| Frontend | Angular 22 (standalone components, lazy loading), TypeScript, SCSS, Vitest |
| Integrações | ViaCEP (consulta de CEP) · previstas: armazenamento S3 com RustFS (D42), Mercado Pago sandbox, Resend, agregador de frete, login com Google |
| Documentação | PlantUML (diagramas de classes), PRD em Markdown |

## Estrutura do repositório

```
droneshop/
├── backend/     API REST em Quarkus (droneshop-api)
├── frontend/    Aplicação Angular (droneshop-web): painel admin e storefront
├── docs/
│   ├── PRD-Aeria.md      requisitos, regras de negócio, faseamento e decisões
│   ├── API.md            mapa de endpoints por módulo e issue
│   ├── diagramas/        8 diagramas de classes por contexto (aeria-*.puml)
│   ├── design/           protótipo navegável, DESIGN.md e PRODUCT.md
│   ├── issues/           corpo de cada issue (contrato de API), sincronizado com o GitHub
│   ├── agentes/          trabalho em paralelo: supervisor, duas trilhas e prompts
│   └── telas/            wireframes antigos (.zip), só histórico
├── scripts/     sincronização e vínculo das issues (testes, protótipo)
└── .github/     workflows, templates de issue/PR, rulesets
```

### Backend

Organizado por módulo de domínio em `br.com.droneshop`, cada um com as camadas `model`, `dto`, `repository`, `service` e `resource`:

| Módulo | Responsabilidade |
|---|---|
| `catalogo` | Marca, categoria, subcategoria, fornecedor, produto (drone, acessório e kit) e grupo de produto |
| `estoque` | Estoque por produto, reservas, movimentos, unidades serializadas |
| `compra` | Carrinho, cupons, lista de desejos |
| `pedido` | Pedido, histórico de status, entrega e rastreio, nota fiscal |
| `pagamento` | Tentativas de pagamento, estorno, cartões salvos (token) |
| `devolucao` | Fluxo de devolução e troca |
| `atendimento` | Chamados de suporte |
| `notificacao` | E-mails transacionais e de marketing, templates editáveis |
| `identidade` | Usuário, perfis de cliente e administrativo, sessões |
| `administracao` | Papéis, permissões, auditoria, configuração da loja |
| `endereco` | Estado, município (IBGE) e consulta de CEP via ViaCEP |

### Frontend

- `admin/`: telas do painel administrativo
- `storefront/`: estrutura da loja (home, catálogo, compra, conta, suporte, empresas)
- `core/`, `shared/`, `layout/`: serviços, componentes compartilhados e layout

## Status

O desenvolvimento segue o faseamento do [PRD](docs/PRD-Aeria.md), com o **painel administrativo primeiro**. O backlog fica nas [issues](../../issues), agrupadas em milestones por fase.

| Fase | Entrega | Status |
|---|---|---|
| 1 | Painel administrativo: catálogo, estoque, promoções, cupons, pedidos, usuários e dashboard | 🚧 em andamento |
| 2 | Conta do cliente e vitrine | ⏳ |
| 3 | Compra, pagamento e entrega | ⏳ |
| 4 | Pós-venda: devolução, nota fiscal, atendimento, avaliações | ⏳ |
| 5 | Auditoria, marketing, busca, 2FA, LGPD, login com Google | ⏳ |

Já entregue na fase 1 (issues #4 a #18 e #80):

- Login do admin com JWT, perfis administrativos com permissão por módulo e configuração da loja
- Catálogo: marcas, categorias, subcategorias, fornecedores (com endereço por CEP via ViaCEP), produtos (drone, acessório e kit), grupos de produto, compatibilidade e galeria de mídias
- Estoque com movimentos, promoções, cupons, pedidos com transições de status e dashboard do admin

Falta na fase 1: telas do admin no protótipo 02 (D41), imagem principal nas listagens (#87), armazenamento S3 (D42) e complementos do catálogo (#84). O resto do backlog anda em duas trilhas paralelas: ver [`docs/agentes`](docs/agentes/README.md).

## Como rodar

### Pré-requisitos

- JDK 25
- Node.js e npm
- Docker (para o PostgreSQL)

### Backend

```bash
cd backend
docker compose up -d postgres   # sobe o PostgreSQL em localhost:5432 (a trilha B usa postgres-b, em 5433)
./mvnw quarkus:dev              # API em http://localhost:8080
```

Em modo dev o schema é recriado a cada subida e carregado com dados de exemplo: `localidades.sql` (estados e municípios do IBGE), `import.sql` (base) e `import-b.sql` e `import-a.sql` (dados novos de cada trilha).

- Swagger UI: http://localhost:8080/q/swagger-ui
- Dev UI do Quarkus: http://localhost:8080/q/dev/

### Frontend

```bash
cd frontend
npm install
npm start                       # http://localhost:4200
npm run prototipo               # protótipo em http://localhost:4300
npm run prototipo:verificar     # páginas, links, viewport e alt do protótipo
npm run e2e                     # telas x protótipo (Playwright); npm run e2e:relatorio abre as imagens
```

A API libera CORS para `http://localhost:4200`.

### Usuários de teste

Criados pela carga de dados (`import.sql`) a cada subida em modo dev. Só para desenvolvimento e testes.

| Perfil | E-mail | Senha | Onde entra |
|---|---|---|---|
| Administrador (papel `ADMIN`) | `admin@aeria.com.br` | `aeria123` | `http://localhost:4200/admin` |
| Cliente | `cliente@aeria.com.br` | `aeria123` | loja (`/entrar`), dono dos 24 pedidos da carga |

## Documentação

| Documento | Conteúdo |
|---|---|
| [PRD](docs/PRD-Aeria.md) | Escopo, requisitos funcionais e não-funcionais, regras de negócio, faseamento e decisões registradas |
| [Diagramas](docs/diagramas) | Diagramas de classes por contexto: identidade, catálogo, estoque, compra, pedido, pagamento, atendimento e administrativo |
| [Endpoints da API](docs/API.md) | Todas as rotas que o frontend usa, por módulo, com a issue de cada uma e o que já está pronto |
| [Design e protótipo](docs/design) | Protótipo navegável das 42 telas (publicado no GitHub Pages), design system com tokens e movimento, e resumo de produto |
| [Trabalho em paralelo](docs/agentes/README.md) | Supervisor e duas trilhas: dono de cada módulo, ordem das issues, portões, portas e prompts dos agentes |
| [Guia para agentes](AGENTS.md) e [Segurança](SECURITY.md) | Regras de revisão e implementação, e checklist de segurança |
| [Issues](docs/issues) | Corpo de cada issue com o contrato de API; o workflow `sincronizar-issues` leva as mudanças para o GitHub |
| [Testes do backend](backend/src/test/README.md) | Como rodar, matriz issue × testes e o que bloqueia o merge |
| [Conformidade com o protótipo](frontend/e2e/README.md) | Testes que comparam cada tela com o protótipo e o fluxo para mudar o design |

## Contribuindo

O fluxo é issue → branch a partir da `develop` → PR para a `develop` → PR `develop` → `main`, com commits em Conventional Commits em português. As issues abertas estão em duas trilhas (etiquetas `trilha:a` e `trilha:b`), cada uma com dono de módulo e ordem definidos em [`docs/agentes/README.md`](docs/agentes/README.md).

1. **Branch:** na issue, "Development → Create a branch" com origem na `develop`. O nome sugerido pelo GitHub (`<numero>-<resumo>`) já serve.
2. **Commits:** `tipo(escopo): resumo`, ex.: `feat(catalogo): cria GrupoProduto com rótulo e ordem das versões`.
3. **PR para a `develop`** com `Closes #<numero>` na descrição; a issue fecha sozinha no merge.
4. **Revisão:** 1 aprovação de qualquer colaborador com escrita. O autor não aprova o próprio PR; o admin pode usar o bypass ("Merge without waiting for requirements to be met").
5. **CI:** os checks `backend` e `frontend` precisam passar. Os testes da issue já existem (etiqueta `issue-<numero>`); no PR que a entrega, acrescente a etiqueta em `backend/src/test/issues-entregues.txt` e, se a issue tem tela, em `frontend/e2e/telas-entregues.txt`, sempre abaixo da linha da trilha da issue.
6. **Design:** mudança de tela começa no protótipo (`docs/design/README.md`, seção "Mudar o design").
7. **Issue mudou?** Edite `docs/issues/<numero>.md` no PR; depois do merge, rode o workflow `sincronizar-issues` na `develop`.
8. **Release:** quando a fase estiver estável, PR de `develop` para `main`.

Push direto, force push e exclusão da `main` e da `develop` são bloqueados pelos rulesets (`.github/rulesets/*.json` guarda a configuração para reimportar).
