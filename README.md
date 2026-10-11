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
| Integrações | ViaCEP (consulta de CEP) · previstas: Mercado Pago sandbox, Resend, agregador de frete, login com Google |
| Documentação | PlantUML (diagramas de classes), PRD em Markdown |

## Estrutura do repositório

```
droneshop/
├── backend/     API REST em Quarkus (droneshop-api)
├── frontend/    Aplicação Angular (droneshop-web): painel admin e storefront
├── docs/
│   ├── PRD-Aeria.md      requisitos, regras de negócio, faseamento e decisões
│   ├── diagramas/        8 diagramas de classes por contexto (aeria-*.puml)
│   └── telas/            wireframes versionados (v2, v3, v4)
└── .github/     workflows, templates de issue/PR, rulesets
```

### Backend

Organizado por módulo de domínio em `br.com.droneshop`, cada um com as camadas `model`, `dto`, `repository`, `service` e `resource`:

| Módulo | Responsabilidade |
|---|---|
| `catalogo` | Marca, categoria, subcategoria, fornecedor, produto (drone e acessório), variantes e kits |
| `estoque` | Estoque por variante, reservas, movimentos, unidades serializadas |
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

- `admin/` — telas do painel administrativo
- `storefront/` — estrutura da loja (home, catálogo, compra, conta, suporte, empresas)
- `core/`, `shared/`, `layout/` — serviços, componentes compartilhados e layout

## Status

O desenvolvimento segue o faseamento do [PRD](docs/PRD-Aeria.md), com o **painel administrativo primeiro**. O backlog fica nas [issues](../../issues), agrupadas em milestones por fase.

| Fase | Entrega | Status |
|---|---|---|
| 1 | Painel administrativo: catálogo, estoque, promoções, cupons, pedidos, usuários e dashboard | 🚧 em andamento |
| 2 | Conta do cliente e vitrine | ⏳ |
| 3 | Compra, pagamento e entrega | ⏳ |
| 4 | Pós-venda: devolução, nota fiscal, atendimento, avaliações | ⏳ |
| 5 | Auditoria, marketing, busca, 2FA, LGPD, login com Google | ⏳ |

Já entregue na fase 1:

- CRUD de **marcas**, **categorias**, **subcategorias** e **fornecedores** (API + telas do admin), com paginação e filtros
- Cadastro de **fornecedor com endereço estruturado** (Estado e Município do IBGE) e preenchimento automático por CEP via ViaCEP
- Base de **produtos** (API e telas em construção)

## Como rodar

### Pré-requisitos

- JDK 25
- Node.js e npm
- Docker (para o PostgreSQL)

### Backend

```bash
cd backend
docker compose up -d postgres   # sobe o PostgreSQL em localhost:5432
./mvnw quarkus:dev              # API em http://localhost:8080
```

Em modo dev o schema é recriado a cada subida e carregado com dados de exemplo (`localidades.sql` com estados e municípios do IBGE, e `import.sql`).

- Swagger UI: http://localhost:8080/q/swagger-ui
- Dev UI do Quarkus: http://localhost:8080/q/dev/

### Frontend

```bash
cd frontend
npm install
npm start                       # http://localhost:4200
```

A API libera CORS para `http://localhost:4200`.

## Documentação

| Documento | Conteúdo |
|---|---|
| [PRD](docs/PRD-Aeria.md) | Escopo, requisitos funcionais e não-funcionais, regras de negócio, faseamento e decisões registradas |
| [Diagramas](docs/diagramas) | Diagramas de classes por contexto: identidade, catálogo, estoque, compra, pedido, pagamento, atendimento e administrativo |
| [Telas](docs/telas) | Wireframes do storefront e do painel admin (versão atual: v4) |

## Contribuindo

O fluxo é issue → branch a partir da `develop` → PR para a `develop` → PR `develop` → `main`, com commits em Conventional Commits em português.

1. **Branch:** na issue, "Development → Create a branch" com origem na `develop`. O nome sugerido pelo GitHub (`<numero>-<resumo>`) já serve.
2. **Commits:** `tipo(escopo): resumo`, ex.: `feat(catalogo): cria VarianteProduto com dados de embalagem`.
3. **PR para a `develop`** com `Closes #<numero>` na descrição; a issue fecha sozinha no merge.
4. **Revisão:** 1 aprovação de qualquer colaborador com escrita. O autor não aprova o próprio PR; o admin pode usar o bypass ("Merge without waiting for requirements to be met").
5. **CI:** os checks `backend` e `frontend` precisam passar.
6. **Release:** quando a fase estiver estável, PR de `develop` para `main`.

Push direto, force push e exclusão da `main` e da `develop` são bloqueados pelos rulesets (`.github/rulesets/*.json` guarda a configuração para reimportar).
