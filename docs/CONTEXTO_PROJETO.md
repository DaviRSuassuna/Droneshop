# Contexto do projeto: Aeria Drones

> Documento vivo. Resume **o que é o projeto, como ele é construído, que decisões já foram tomadas, o que está pronto e o que falta**. Deve ser lido antes de qualquer nova implementação e atualizado no mesmo PR sempre que o progresso, a stack ou uma decisão mudar.
>
> Fontes detalhadas: [`PRD-Aeria.md`](PRD-Aeria.md) (requisitos e decisões completas), [`API.md`](API.md) (endpoints), [`issues/`](issues/) (contrato de cada issue), [`REGRAS_NEGOCIO.md`](REGRAS_NEGOCIO.md) (domínio) e [`modelo.plantuml`](modelo.plantuml) (modelo de dados implementado).

Última atualização: 31/10/2026.

---

## 1. Visão

A **Aeria Drones** é um e-commerce de drones e acessórios com duas faces:

- **Loja (storefront)**: o cliente pesquisa, compara fichas técnicas, compra e acompanha o pedido.
- **Painel administrativo**: a operação cadastra catálogo, controla estoque, processa pedidos, trata devoluções e atende clientes.

Drone não é varejo comum: alto valor unitário, ficha técnica extensa, garantia relevante e, em parte dos modelos, registro na ANAC. Por isso o projeto prevê rastreio por número de série, specs comparáveis e um pós-venda mais exigente.

**É um projeto de estudo**, não uma operação comercial:

| Integração | Situação |
|---|---|
| Pagamento (Mercado Pago) | **Sandbox permanente** (D2), sem dinheiro real |
| NF-e | **Simulada** localmente (D3), sem SEFAZ nem certificado |
| E-mail (Resend) | **Envio real** (D5), única integração que sai para o mundo real |
| CEP (ViaCEP) | Real em dev; falso nos testes |
| Frete (agregador), Google (login) | Previstos, com REST Client e dublês nos testes (D34) |

Não-objetivos: marketplace, app nativo, PDV, vendas internacionais, assinatura.

## 2. Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 25, Quarkus 3.38, Maven (`./mvnw`), Hibernate ORM com Panache, Hibernate Validator, Quarkus REST + Jackson, REST Client, SmallRye JWT (+ build), Elytron (BCrypt), SmallRye OpenAPI, Mailer + Qute |
| Banco | PostgreSQL 16 (Docker Compose em dev; Dev Services nos testes; serviço do job no CI) |
| Frontend | Angular 22 (standalone, lazy loading, signals), TypeScript 6, SCSS, Vitest, Playwright |
| Testes backend | JUnit 5 + `@QuarkusTest` + RestAssured, etiquetados por issue (`@Tag("issue-N")`) |
| Documentação | PRD em Markdown, PlantUML (`docs/diagramas/` e `docs/modelo.plantuml`), protótipo HTML navegável (`docs/design/prototipo`) |
| CI | GitHub Actions (`backend-ci.yml`, `frontend-ci.yml`, publicação do protótipo, sincronização de issues) |

### Estrutura do repositório

```
backend/    API REST (droneshop-api): br.com.droneshop.<modulo>.{model,dto,mapper,repository,service,resource}
frontend/   Angular (droneshop-web): admin/, storefront/, core/, shared/, layout/
docs/       PRD, API, issues, diagramas, design/protótipo e estes documentos de contexto
scripts/    sincronização e vínculo das issues com testes e protótipo (Python)
.github/    workflows, templates de issue/PR, rulesets
```

Módulos do backend: `administracao`, `atendimento`, `catalogo`, `compra`, `devolucao`, `endereco`, `estoque`, `identidade`, `notificacao`, `pagamento`, `pedido` (+ transversais `model`, `dto`, `exception`, `resource`, `util`).

## 3. Decisões arquiteturais

As decisões `D1` a `D40` do PRD **valem como regra** (contrariá-las é bloqueante em revisão). As que mais moldam o código:

### Arquitetura e padrões (RNF01, RNF12, D31)
- **Padrão SGA** (projeto de referência do professor) em todos os módulos: `DefaultEntity` (`@MappedSuperclass` com `id`, `dataCadastro`, `dataAlteracao`), `XxxRequestDTO`/`XxxResponseDTO` como `record`, mapper estático, `PanacheRepository`, service interface + `Impl` com `@Transactional`, `PageResponse` (`items`, `page`, `pageSize`, `totalItems`, `totalPages`).
- **Enums de domínio com id e nome** (D31): banco guarda o id (conversor JPA), API recebe `idXxx` e devolve `{id, nome}`, cada enum tem rota pública de leitura em kebab-case (`/status-marca`, `/tipos-produto`...).
- **Referências**: no corpo `idXxx`; na resposta, objeto aninhado.
- **Erros Problem (RFC 7807/9457)**: 400 validação (`errors[].field`), 401, 403, 404 inexistente (D22), 405, 409 em uso ou transição inválida (D24, D30), 503 integração fora. **Nunca 422.**
- **Sucesso**: `GET`/`POST` 200 com corpo; `PUT`/`DELETE` 204.
- **Prefixos de rota**: admin na raiz (`/marcas`) e `/admin/**`; vitrine pública em `/loja/**`; cliente logado em `/conta/**`.

### Domínio
- **D40 (cada versão é um `Produto`)**: não existe `VarianteProduto`. Versões se agrupam em `GrupoProduto`. Substitui D13 e as issues #9/#10.
- **D27 / D39 (herança `JOINED`)**: `Produto` -> `Drone`, `Acessorio`, `Kit`, com rotas próprias de escrita (`/drones`, `/acessorios`, `/kits`); `/produtos` só lista, consulta e exclui. Tipo define estrutura; categoria organiza vitrine dentro de um tipo.
- **D14 (specs do drone como colunas fixas)**: atributos tipados diretamente na tabela.
- **D7 (kit sem estoque próprio)**: baixa e disponibilidade derivadas dos componentes.
- **D21 / D28 (rascunho aceita campos vazios)**: obrigatórios só ao publicar.
- **D24 (em uso não se exclui, se inativa)**: responde 409 com contagem.
- **D11 (origem de registro)**: `MANUAL`, `SISTEMA` ou `INTEGRACAO`; autor só quando manual e sempre vindo do token.
- **D12 (endereço congelado no pedido)**: `EnderecoPedido` embutido.
- **D9 (uso de cupom só existe com pedido)**: `UsoCupom -> Pedido`.
- **D29 (mídia por upload)**: PNG/JPG/PDF até 20 MB, nome gerado no servidor; vídeo por URL.

### Segurança e testes
- **JWT** (#4) com perfis `ADMINISTRATIVO`/cliente; rotas admin exigem `@RolesAllowed("ADMINISTRATIVO")` + `@ModuloAdmin(ModuloSistema.X)` (#5). Senha BCrypt. Ver [`../SECURITY.md`](../SECURITY.md).
- **D25**: testes entram como o admin semente `admin@aeria.com.br` via `testes/Api.java`.
- **D34**: integrações externas com REST Client de URL configurável e dublês em `src/test` (`ViaCepFalso`, `MercadoPagoFalso`, `FreteFalso`, `ResendFalso`, `GoogleFalso`).
- **D33**: rotinas agendadas também executáveis por `POST /admin/rotinas/{nome}`.

### Frontend e design
- **D38 (o protótipo é a referência das telas)**: cada tela é comparada ao protótipo (estrutura, tokens, estilo e ausência de rolagem horizontal em 390px) pela suíte Playwright em `frontend/e2e`.
- **D36 (o modelo vence o protótipo)**: campo visual sem respaldo no modelo sai da tela.
- **D20 (web responsiva)**: desktop e celular (360 a 480px), sem app nativo.
- Tema único escuro "Noite + Campo" com tokens de [`design/DESIGN.md`](design/DESIGN.md).

### Fluxo de entrega
- Issue -> branch de `develop` (`<numero>-<resumo>`) -> PR para `develop` com `Closes #N` -> release `develop` -> `main`.
- **Travas de regressão**: etiqueta `issue-N` em `backend/src/test/issues-entregues.txt` e, se houver tela, em `frontend/e2e/telas-entregues.txt`; essas etiquetas bloqueiam o merge.

## 4. Estado atual

O desenvolvimento segue o faseamento do PRD com o **painel administrativo primeiro (D19)**. A **Fase 1 está praticamente concluída** no backend e nas telas do admin.

### Backend: etiquetas que bloqueiam o merge (`issues-entregues.txt`)

| Etiqueta | Entrega |
|---|---|
| `base` | Marca, Categoria, Subcategoria, Fornecedor (com endereço IBGE), Estados/Municípios, consulta de CEP (ViaCEP), rota inexistente -> 404 |
| `issue-4` | Login por e-mail/senha com JWT (`/auth/login`, `/auth/me`) |
| `issue-5` | Usuário administrativo com papel e permissões por módulo |
| `issue-6` | Configuração da loja (registro único) |
| `issue-7` | Produto: SEO, garantia, lançamento, slug |
| `issue-8` | Drone e Acessório como especializações; tipo × categoria (D39) |
| `issue-9`, `issue-10` | Substituídas pela #80 (D40); etiquetas mantidas |
| `issue-11` | Compatibilidade N:N acessório <-> produto |
| `issue-12` | Mídias do produto (upload de imagem/PDF, vídeo por URL, ordem, principal) |
| `issue-13` | Movimento de estoque com origem e autor |
| `issue-14` | Promoções com vigência e escopo |
| `issue-15` | Cupons (cadastro, limites, restrições, usos) |
| `issue-16` | Pedido: listagem, detalhe, resumo por status (alimentado por carga de dados) |
| `issue-17` | Transições de status do pedido com histórico (409 em transição inválida) |
| `issue-18` | Dashboard do admin |
| `issue-80` | Modelo sem variante (D40): dados comerciais no produto, Kit, GrupoProduto |

Também implementado, com testes ainda **informativos**: **#61** (ações em lote em produtos, reordenação de categorias, exportação CSV, etiqueta PDF de pedido).

### Frontend: telas que bloqueiam o merge (`telas-entregues.txt`)

`base` (marcas, categorias, subcategorias, fornecedores), `issue-4` (login admin), `issue-5` (usuários), `issue-6` (configuração), `issue-8` / `issue-80` (produtos, drone, acessório, kit, grupos), `issue-13` (estoque e movimentos), `issue-14` (promoções), `issue-15` (cupons), `issue-16` (pedidos: lista e detalhe), `issue-18` (dashboard). O CSS legado foi removido; todas as telas implementadas usam os tokens do protótipo.

O `storefront/` tem a estrutura de pastas criada (auth, catálogo, compra, conta), mas **nenhuma tela da loja está entregue**.

### Entidades já persistidas

`Usuario`, `PerfilAdministrativo`, `PermissaoModulo`, `PerfilCliente` (mínimo, para pedidos da carga), `ConfiguracaoLoja`, `Marca`, `Categoria`, `Subcategoria`, `Fornecedor`, `Produto` (`Drone`, `Acessorio`, `Kit`), `ComponenteKit`, `GrupoProduto`, `MidiaProduto`, `Promocao`, `Cupom`, `UsoCupom`, `MovimentoEstoque`, `Pedido`, `ItemPedido`, `HistoricoStatusPedido`, `DescontoPedido`, `Estado`, `Municipio`. Detalhes em [`modelo.plantuml`](modelo.plantuml).

Os módulos `atendimento`, `devolucao`, `notificacao` e `pagamento` ainda estão vazios (só `.gitkeep`).

### Carga de dados (dev)

`localidades.sql` (estados e municípios IBGE) e `import.sql` (catálogo de exemplo, admin `admin@aeria.com.br` e cliente `cliente@aeria.com.br`, ambos com senha `aeria123` só para desenvolvimento, e 24 pedidos). O schema é recriado a cada subida em dev.

## 5. Pendências

### Fase 1 (resíduos)
- **#61**: promover a etiqueta `issue-61` a bloqueante.
- Issues novas vindas da revisão do protótipo (D38): `nova-ajuste-prototipo` (tirar do protótipo o que contradiz o modelo), `nova-catalogo-complementos` (logo por upload, peso do acessório, kit em Ofertas), `nova-dashboard-indicadores`, `nova-pedidos-painel`, `nova-imagem-produto`.
- Partes do dashboard sem API (série diária, comparação, conversão, ranking) estão fora da tela de propósito.

### Fase 2 (Conta do cliente e vitrine)
#19 PerfilCliente e endereços · #20 Sessões e perfil ativo · #21 Vitrine (listagem, filtros, página de produto, `/loja/**`) · #22 Lista de desejos.

### Fase 3 (Compra, pagamento e entrega)
#23 Reserva de estoque · #24 Carrinho anônimo · #25 Cupom no carrinho · #26 Checkout · #27 Pagamento Mercado Pago sandbox + webhook · #28 Cartões salvos · #29 Criação do pedido com descontos e uso de cupom · #30 Frete híbrido · #31 Rastreio · #32 E-mails via Resend com templates · #33 Verificação de e-mail e recuperação de senha.

### Fase 4 (Pós-venda)
#34 Unidade por número de série · #35 NF-e simulada · #36 Estorno · #37 Devolução e troca · #38 Chamados · #39 Avaliações · #40 Perguntas e respostas · `nova-meus-drones` · `nova-empresas` · `nova-suporte-faq` · `nova-favoritos-compartilhar`.

### Fase 5 (Auditoria, marketing e busca)
#41 Auditoria · #42 Preferências e descadastro · #43 Termos buscados · #44 Comparação · #45 2FA · #46 LGPD · #47 Login com Google.

### Infra e documentação
- `nova-deploy` (deploy de produção).
- **P3 do PRD**: as 16 telas do storefront ainda não foram revisadas contra o modelo; o contrato de cada issue da fase 2+ fecha os campos.

---

## Como manter este documento

Atualize no **mesmo PR** que:
- entrega uma issue (mova-a de "Pendências" para "Estado atual" e cite a etiqueta);
- registra ou altera uma decisão `Dn` no PRD;
- muda versão de framework, banco ou ferramenta da stack;
- cria um módulo ou entidade nova (atualize também [`modelo.plantuml`](modelo.plantuml) e [`REGRAS_NEGOCIO.md`](REGRAS_NEGOCIO.md)).
