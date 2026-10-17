# Endpoints da API

Mapa de todas as rotas que o frontend (admin e storefront) precisa, por módulo, com a issue que entrega cada uma. O contrato detalhado de cada rota (corpo, resposta, erros) está na seção **Contrato de API** da issue, versionada em [`docs/issues/`](issues/); os testes (`backend/src/test/README.md`) seguem esse contrato.

- **pronto**: existe no código e tem teste (`@Tag("base")`).
- **a fazer**: rota com contrato fechado na issue e testes já escritos (etiqueta `issue-N`), aguardando o código.

As telas citadas são as do [protótipo](design/README.md).

## Convenções

| Tema | Regra |
|---|---|
| Formato | JSON. CPF, CNPJ, CEP e telefone trafegam só com dígitos; a máscara é do front |
| Listagem | `?page=0&pageSize=10` (padrão 10). Resposta `PageResponse`: `items`, `page`, `pageSize`, `totalItems`, `totalPages`. Não há rota `/count` |
| Referências | No corpo, chave estrangeira é `idXxx` (`idStatus`, `idCategoria`, `idMarca`...); na resposta, o relacionamento vem aninhado (`produto.marca.nome`). Filtros por relacionamento usam o mesmo nome (`?idCategoria=`) |
| Enums | Todo enum de domínio tem id e nome (D31). O corpo envia `idXxx`; a resposta traz `xxx: {id, nome}`. Os valores ficam numa rota pública de leitura em kebab-case (`/status-marca`, `/tipos-movimento`, `/papeis-administrativos`...), com ids a partir de 1 na ordem do diagrama |
| Dinheiro | Número JSON sempre com duas casas (`BigDecimal` com escala 2), inclusive zero (`0.00`) |
| Sucesso | `GET` e `POST` 200 com o registro; `PUT` e `DELETE` 204 |
| Erros (formato Problem) | 400 validação (`errors[].field`), 401 sem login ou token inválido, 403 sem permissão, 404 não existe ou rota desconhecida (D22), 405 método que a rota não tem, 409 em uso (D24) ou transição de estado inválida (D30), 503 integração externa fora. A API não usa 422 |
| Referência | O projeto SGA do professor é a fonte da verdade para o formato das rotas, paginação, DTOs, mappers e enums |
| Exclusão | excluir o que não existe responde 404 (D22) |
| Rascunho | `RASCUNHO` (id 1) aceita campos vazios; obrigatórios só ao publicar (D21, D28) |
| Rotinas | Rotinas agendadas também rodam por `POST /admin/rotinas/{nome}?referencia=` (D33) |
| Integrações | Mercado Pago, agregador de frete, Resend e Google têm REST Client com URL configurável; nos testes apontam para falsos em `src/test` (D34) |
| Autenticação | JWT (#4). Rotas do admin exigem perfil administrativo e a permissão do módulo (#5); rotas de `/conta` exigem perfil de cliente. Até a #4, nada é protegido. Nos testes, `testes/Api.java` entra como o admin semente `admin@aeria.com.br` (D25) |
| Prefixos | cadastro do admin na raiz (`/marcas`); leitura pública da loja em `/loja`; dados do cliente logado em `/conta` |

## Endereço

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/estados` · `/estados/{id}` · `/estados/sigla/{sigla}` · `/estados/nome/{nome}` | select de Estado | base | pronto |
| GET | `/municipios?idEstado=` · `/municipios/{id}` · `/municipios/nome/{nome}` | select de Município | base | pronto |
| GET | `/ceps/{cep}` | preencher endereço pelo CEP (ViaCEP no backend) | base | pronto |

## Catálogo (admin)

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET POST | `/marcas` | Marcas, Cadastrar marca | base | pronto |
| GET PUT DELETE | `/marcas/{id}` | editar / excluir marca | base | pronto |
| GET | `/marcas/nome/{nome}` · `/marcas/slug/{slug}` | busca | base | pronto |
| GET | `/status-marca` · `/status-categoria` · `/status-fornecedor` · `/status-produto` (+ `/{id}`) | selects de status | base | pronto |
| GET POST · GET PUT DELETE | `/categorias` · `/categorias/{id}` (+ `nome`, `slug`) | Categorias, Cadastrar categoria (com `idTipo` desde a #8, D39) | base / #8 | pronto |
| GET POST · GET PUT DELETE | `/subcategorias?idCategoria=` · `/subcategorias/{id}` | subcategorias | base | pronto |
| GET POST · GET PUT DELETE | `/fornecedores` · `/fornecedores/{id}` (+ `nome`, `cnpj/{cnpj}`) | Fornecedores, Cadastrar fornecedor | base | pronto |
| GET | `/produtos?idTipo=&nome=&idMarca=&idCategoria=&idSubcategoria=&idStatus=` | Produtos (filtros; cada item com `tipo`) | base / #8 | pronto |
| GET · GET DELETE | `/produtos` · `/produtos/{id}` | Produtos (consulta traz os campos do tipo) | base / #7 / #8 | pronto; POST e PUT respondem 405 desde a #8 (D27) |
| GET | `/produtos/slug/{slug}` · `/tipos-produto` (+ `/{id}`) | busca por slug, select de tipo | #7, #8 | pronto |
| POST · PUT | `/drones` · `/drones/{id}` | Cadastrar drone (specs fixas, D14) | #8 | pronto |
| POST · PUT | `/acessorios` · `/acessorios/{id}` | Cadastrar acessório | #8 | pronto |
| (corpo) | `variantes[]` dentro do drone/acessório | tabela de variantes na própria tela (D13) | #9 | pronto |
| GET · GET PUT | `/variantes?busca=` · `/variantes/{id}` | select de variante (estoque, promoção, kit); preço e margem (D26) | #9 | pronto |
| (corpo) | `componentes[]` na variante kit; toda variante responde `disponivel`, e o kit `pesoSobrescrito`/`dimensoesSobrescritas` | Kit ou combo (D7, D16) | #10 | pronto |
| (corpo) | `idsCompativeis[]` no acessório; resposta com `compativeis[]` e, em todo produto, `acessoriosCompativeis[]` (lado recíproco) | Compatível com (D17) | #11 | pronto |
| GET · GET POST · PATCH · DELETE | `/tipos-midia` · `/produtos/{id}/midias` (multipart) · `/midias/{id}` · `/produtos/{id}/midias/ordem` · `/midias/arquivos/{nome}` | imagem e manual por upload, vídeo por URL (D29) | #12 | pronto |
| GET POST · GET PUT DELETE | `/promocoes?situacao=` · `/promocoes/{id}` | Promoções (abas Vigentes/Agendadas/Encerradas), Criar promoção | #14 | a fazer |
| GET POST · GET PUT DELETE | `/cupons?situacao=&codigo=` · `/cupons/{id}` | Cupons, Criar cupom | #15 | a fazer |
| GET | `/cupons/{id}/usos` | Exportar usos | #15 | a fazer |

## Estoque (admin)

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/estoque?situacao=&busca=` | Estoque: saldo por variante (físico, reservado, disponível, mínimo) | #13 | a fazer |
| GET POST | `/estoque/movimentos?idVariante=&idTipo=` | aba Movimentos; Novo movimento (origem e autor, D11) | #13 | a fazer |
| GET | `/estoque/reservas?idVariante=` | aba Reservas ativas | #23 | a fazer |
| POST | `/admin/rotinas/expirar-reservas` · `/admin/rotinas/carrinhos-abandonados` | rotinas sob demanda (D33) | #23, #24 | a fazer |
| GET POST | `/unidades?serie=` · `/pedidos/{id}/itens/{idItem}/unidades` | aba Números de série; vincular série na expedição (D10) | #34 | a fazer |

## Pedidos (admin)

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/pedidos?idStatus=&dataInicio=&dataFim=&busca=` · `/pedidos/resumo-status` | Pedidos (abas com contagem por status, filtro de período) | #16 | a fazer |
| GET | `/pedidos/{id}` | Detalhe do pedido (itens, endereço congelado D12, pagamento) | #16 | a fazer |
| PATCH | `/pedidos/{id}/status` | Marcar como enviado, Cancelar pedido (transição inválida 409, D30) | #17 | pronto |
| GET | `/pedidos/{id}/historico` | linha do tempo do pedido | #17 | pronto |
| GET POST · PUT | `/pedidos/{id}/rastreio` · `/pedidos/{id}/entrega/codigo-rastreio` | Rastreio (eventos) | #31 | a fazer |
| GET | `/pedidos/{id}/pagamentos` | pagamentos do pedido | #27 | a fazer |
| GET POST | `/pedidos/{id}/nota-fiscal` | NF-e simulada (venda e devolução) | #35 | a fazer |

## Administração

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/admin/dashboard?periodo=` | Visão geral: KPIs, vendas por produto, estoque baixo, pedidos pendentes | #18 | a fazer |
| GET POST · GET PUT | `/admin/usuarios?nome=&idPapel=&ativo=&page=&pageSize=` · `/admin/usuarios/{id}` | Usuários, Novo usuário; perfil 0..1 e permissões por módulo | #5 | pronto |
| GET | `/papeis-administrativos` · `/modulos-sistema` · `/admin/papeis/{id}/permissoes-padrao` | select de papel e permissões padrão por módulo | #5 | pronto |
| GET PUT | `/admin/configuracao` | parâmetros da loja (registro único) | #6 | pronto |
| GET | `/loja/configuracao` | o que a loja mostra da configuração | #21 | a fazer |
| GET | `/admin/auditoria?idUsuario=&entidade=` | Ver log de auditoria | #41 | a fazer |
| POST · DELETE · POST | `/conta/2fa` · `/conta/2fa/confirmar` · `/auth/login/2fa` | 2FA opcional do admin | #45 | a fazer |
| GET PUT · GET | `/admin/templates-email` · `/admin/templates-email/{codigo}` · `/admin/notificacoes` | templates editáveis e histórico de envio | #32 | a fazer |
| GET | `/admin/termos-buscados` | relatório de buscas | #43 | a fazer |

## Identidade e conta

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| POST | `/auth/login` · `/auth/logout` | Entrar (e-mail e senha, D1) | #4 | a fazer |
| GET | `/auth/me` | quem está logado e perfis disponíveis | #4 | a fazer |
| POST | `/auth/perfil-ativo` | escolher perfil cliente/admin no login | #20 | a fazer |
| GET DELETE | `/conta/sessoes` · `/conta/sessoes/{id}` | sessões ativas e revogação | #20 | a fazer |
| POST | `/clientes` | Criar conta | #19 | a fazer |
| GET PUT | `/conta/perfil` | Perfil e dados | #19 | a fazer |
| PUT | `/conta/senha` | Atualizar senha | #19 | a fazer |
| GET POST · PUT DELETE · PATCH | `/conta/enderecos` · `/conta/enderecos/{id}` · `/conta/enderecos/{id}/padrao` | Endereços | #19 | a fazer |
| POST | `/auth/verificar-email/enviar` · `/auth/verificar-email` · `/auth/recuperar-senha` · `/auth/redefinir-senha` | tokens de e-mail e senha | #33 | a fazer |
| POST | `/auth/google` | login com Google por ID token (D6, D37) | #47 | a fazer |
| GET PUT · POST | `/conta/consentimentos` · `/conta/exclusao` | LGPD: consentimentos e exclusão/anonimização | #46 | a fazer |
| GET PUT · GET | `/conta/preferencias-notificacao` · `/descadastro/{token}` | preferências e descadastro de e-mail | #42 | a fazer |

## Loja (storefront, leitura pública)

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/loja/categorias?tipo=` · `/loja/marcas?destaque=` | menu, home (marcas em destaque) | #21 | a fazer |
| GET | `/loja/produtos?categoria=&subcategoria=&marca=&precoMin=&precoMax=&alcance=&autonomia=&ordem=&page=` | Drones, Acessórios (só ATIVO, RN17) | #21 | a fazer |
| GET | `/loja/filtros?categoria=` | contagens de cada filtro (facetas) | #21 | a fazer |
| GET | `/loja/produtos/{slug}` | Produto (variantes, specs, mídias) | #21 | a fazer |
| GET | `/loja/produtos/{slug}/compativeis` | Acessórios compatíveis | #21 | a fazer |
| GET | `/loja/ofertas` | Ofertas e Kits com desconto | #21 | a fazer |
| GET | `/loja/busca?q=` | busca (registra o termo) | #43 | a fazer |
| GET | `/loja/comparar?ids=` | comparação de especificações | #44 | a fazer |
| GET POST | `/loja/produtos/{slug}/avaliacoes` | Avaliações (compra verificada) | #39 | a fazer |
| GET POST | `/loja/produtos/{slug}/perguntas` | perguntas e respostas | #40 | a fazer |

## Compra

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET POST DELETE | `/conta/desejos` · `/conta/desejos/{idVariante}` | Lista de desejos | #22 | a fazer |
| GET POST · POST · PATCH DELETE | `/carrinho` · `/carrinho/itens` · `/carrinho/itens/{id}` (header `X-Carrinho-Token`) · `/conta/carrinho` | Carrinho anônimo por token | #24 | a fazer |
| POST | `/carrinho/reservar` | reserva ao entrar no checkout | #23 | a fazer |
| POST | `/carrinho/vincular` | junta o carrinho anônimo ao do cliente no login | #24 | a fazer |
| POST DELETE | `/carrinho/cupom` | Aplicar cupom | #25 | a fazer |
| POST · CRUD | `/frete/cotacao` · `/admin/regras-frete` | Calcular frete (CEP + itens; agregador com fallback, D4) | #30 | a fazer |
| POST | `/checkout` | Finalizar compra: cria o pedido a partir do carrinho reservado | #26, #29 | a fazer |
| POST | `/pedidos/{id}/pagamentos` · `/webhooks/mercadopago` | pagar (sandbox, D2) e receber a confirmação | #27 | a fazer |
| GET POST DELETE · PATCH | `/conta/cartoes` · `/conta/cartoes/{id}` · `/conta/cartoes/{id}/padrao` | Meios de pagamento (token do gateway) | #28 | a fazer |
| GET | `/conta/pedidos` · `/conta/pedidos/{id}` | Meus pedidos, Pedido confirmado | #29 | a fazer |

## Pós-venda e atendimento

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET POST · GET | `/conta/devolucoes` · `/conta/devolucoes/{id}` | pedir devolução ou troca | #37 | a fazer |
| GET · PATCH | `/admin/devolucoes` · `/admin/devolucoes/{id}/status` | tratar devolução | #37 | a fazer |
| POST · GET | `/pagamentos/{id}/estorno` · `/pagamentos/{id}/estornos` | estorno total ou parcial | #36 | a fazer |
| POST | `/chamados` | Suporte: Enviar chamado | #38 | a fazer |
| GET · GET POST | `/conta/chamados` · `/chamados/{id}/mensagens` | histórico do chamado | #38 | a fazer |
| GET · PATCH | `/admin/chamados` · `/admin/avaliacoes/{id}` · `/admin/perguntas/{id}` | atendimento e moderação | #38, #39, #40 | a fazer |

## Rotas que as telas pedem (issues novas, D36)

Estas rotas não tinham issue. Viraram duas issues novas, com contrato em `docs/issues/nova-lote-csv.md` e `docs/issues/nova-empresas.md`.

| Tela / ação | Rota | Issue |
|---|---|---|
| Contagem de produtos nas listagens de marca, categoria e fornecedor | campo `quantidadeProdutos` na resposta | lote-csv |
| Produtos: Ativar, Inativar, Reajustar preço, Mover subcategoria (em lote) | `PATCH /produtos/lote` | lote-csv |
| Categorias: Reordenar | `PATCH /categorias/ordem` | lote-csv |
| Exportar CSV (marcas, fornecedores, produtos, estoque, pedidos) | `GET .../exportar` | lote-csv |
| Pedidos: Imprimir etiqueta | `GET /pedidos/{id}/etiqueta` | lote-csv |
| Empresas: Solicitar proposta | `POST /leads-empresas` | empresas |

## Protótipo × modelo

Resolvido pela D36 (o modelo vence): "Categoria pai" saiu das telas de categoria; "Inscrição estadual", "Observações internas" e "Disponível para vincular" saíram do cadastro de fornecedor; telefone, equipe, observações, foto e o status "Suspenso" saíram do cadastro de usuário (D35).
