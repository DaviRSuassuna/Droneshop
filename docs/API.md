# Endpoints da API

Mapa de todas as rotas que o frontend (admin e storefront) precisa, por módulo, com a issue que entrega cada uma. Serve de ponto de partida para a seção **Contrato de API** das issues e, a partir dela, para os testes (`backend/src/test/README.md`).

- **pronto**: existe no código e tem teste (`@Tag("base")`).
- **a fazer**: rota proposta. O formato final (corpo, campos, erros) é fechado no contrato da issue.

As telas citadas são as do [protótipo](design/README.md).

## Convenções

| Tema | Regra |
|---|---|
| Formato | JSON. CPF, CNPJ, CEP e telefone trafegam só com dígitos; a máscara é do front |
| Listagem | `?page=0&pageSize=10` (padrão 10). Resposta `PageResponse`: `items`, `page`, `pageSize`, `totalItems`, `totalPages`. Não há rota `/count` |
| Referências | No corpo, chave estrangeira é `idXxx` (`idStatus`, `idCategoria`, `idMarca`...); na resposta, o relacionamento vem aninhado (`produto.marca.nome`). Filtros por relacionamento usam o mesmo nome (`?idCategoria=`) |
| Status | Enum com id e nome. O corpo envia `idStatus`; a resposta traz `status: {id, nome}`. Os valores ficam em `GET /status-marca`, `/status-categoria`, `/status-fornecedor` e `/status-produto` |
| Sucesso | `GET` e `POST` 200 com o registro; `PUT` e `DELETE` 204 |
| Erros (formato Problem) | 400 validação (`errors[].field`), 404 não existe ou rota desconhecida (D22), 409 em uso (D24), 503 integração externa fora |
| Referência | O projeto SGA do professor é a fonte da verdade para o formato das rotas, paginação, DTOs, mappers e enums |
| Exclusão | excluir o que não existe responde 404 (D22) |
| Rascunho | `RASCUNHO` (id 1) aceita campos vazios; obrigatórios só ao publicar (D21) |
| Autenticação | JWT (#4). Rotas do admin exigem perfil administrativo e a permissão do módulo (#5); rotas de `/conta` exigem perfil de cliente. Até a #4, nada é protegido |
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
| GET POST · GET PUT DELETE | `/categorias` · `/categorias/{id}` (+ `nome`, `slug`) | Categorias, Cadastrar categoria | base | pronto |
| GET POST · GET PUT DELETE | `/subcategorias?idCategoria=` · `/subcategorias/{id}` | subcategorias | base | pronto |
| GET POST · GET PUT DELETE | `/fornecedores` · `/fornecedores/{id}` (+ `nome`, `cnpj/{cnpj}`) | Fornecedores, Cadastrar fornecedor | base | pronto |
| GET | `/produtos?nome=&idMarca=&idCategoria=&idSubcategoria=&idStatus=` | Produtos (filtros) | base | pronto |
| GET POST · GET PUT DELETE | `/produtos` · `/produtos/{id}` | Produtos | base / #7 | pronto; #7 acrescenta SEO, garantia e lançamento |
| POST · PUT | `/drones` · `/drones/{id}` | Cadastrar drone (specs fixas, D14) | #8 | a fazer |
| POST · PUT | `/acessorios` · `/acessorios/{id}` | Cadastrar acessório | #8 | a fazer |
| (corpo) | `variantes[]` dentro do drone/acessório | tabela de variantes na própria tela (D13) | #9 | a fazer |
| GET | `/variantes?busca=` | select de variante (estoque, promoção, kit) | #9 | a fazer |
| (corpo) | `componentes[]` na variante kit | Kit ou combo (D7) | #10 | a fazer |
| (corpo) | `compativeis[]` no acessório | Compatível com (D17) | #11 | a fazer |
| POST · PATCH · DELETE | `/produtos/{id}/midias` · `/midias/{id}` | imagem principal, galeria, vídeo, manual (upload, ordem, principal) | #12 | a fazer |
| GET POST · GET PUT DELETE | `/promocoes?status=` · `/promocoes/{id}` | Promoções (abas Ativas/Agendadas/Encerradas), Criar promoção | #14 | a fazer |
| GET POST · GET PUT DELETE | `/cupons?status=` · `/cupons/{id}` | Cupons, Criar cupom | #15 | a fazer |
| GET | `/cupons/{id}/usos` | Exportar usos | #15 | a fazer |

## Estoque (admin)

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/estoque?situacao=&busca=` | Estoque: saldo por variante (físico, reservado, disponível, mínimo) | #13 | a fazer |
| GET POST | `/estoque/movimentos?idVariante=` | aba Movimentos; Novo movimento (origem e autor, D11) | #13 | a fazer |
| GET | `/estoque/reservas` | aba Reservas ativas | #23 | a fazer |
| GET POST | `/unidades?serie=` · `/pedidos/{id}/itens/{idItem}/unidades` | aba Números de série; vincular série na expedição (D10) | #34 | a fazer |

## Pedidos (admin)

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/pedidos?idStatus=&busca=` · `/pedidos/resumo-status` | Pedidos (abas com contagem por status) | #16 | a fazer |
| GET | `/pedidos/{id}` | Detalhe do pedido (itens, endereço congelado D12, pagamento) | #16 | a fazer |
| PATCH | `/pedidos/{id}/status` | Marcar como enviado, Cancelar pedido | #17 | a fazer |
| GET | `/pedidos/{id}/historico` | linha do tempo do pedido | #17 | a fazer |
| GET POST | `/pedidos/{id}/rastreio` | Rastreio (eventos) | #31 | a fazer |
| GET | `/pedidos/{id}/nota-fiscal` | NF-e simulada (venda e devolução) | #35 | a fazer |

## Administração

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/admin/dashboard?periodo=` | Visão geral: KPIs, vendas por produto, estoque baixo, pedidos pendentes | #18 | a fazer |
| GET POST · GET PUT | `/admin/usuarios` · `/admin/usuarios/{id}` | Usuários, Novo usuário | #5 | a fazer |
| GET | `/admin/papeis` | select de papel e permissões padrão por módulo | #5 | a fazer |
| GET PUT | `/admin/configuracao` | parâmetros da loja (registro único) | #6 | a fazer |
| GET | `/admin/auditoria?idUsuario=&entidade=` | Ver log de auditoria | #41 | a fazer |
| POST | `/conta/2fa` · `/conta/2fa/confirmar` · DELETE `/conta/2fa` | 2FA opcional do admin | #45 | a fazer |
| GET PUT | `/admin/templates-email` · `/admin/templates-email/{id}` | templates editáveis | #32 | a fazer |

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
| POST | `/auth/verificar-email` · `/auth/recuperar-senha` · `/auth/redefinir-senha` | tokens de e-mail e senha | #33 | a fazer |
| GET | `/auth/google` · `/auth/google/callback` | login com Google (D6) | #47 | a fazer |
| GET PUT · POST | `/conta/consentimentos` · `/conta/exclusao` | LGPD: consentimentos e exclusão/anonimização | #46 | a fazer |
| GET PUT · GET | `/conta/preferencias-notificacao` · `/descadastro/{token}` | preferências e descadastro de e-mail | #42 | a fazer |

## Loja (storefront, leitura pública)

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET | `/loja/categorias` · `/loja/marcas?destaque=` | menu, home (marcas em destaque) | #21 | a fazer |
| GET | `/loja/produtos?categoria=&subcategoria=&marca=&precoMin=&precoMax=&alcance=&autonomia=&ordem=&page=` | Drones, Acessórios (só ATIVO, RN17) | #21 | a fazer |
| GET | `/loja/filtros?categoria=` | contagens de cada filtro (facetas) | #21 | a fazer |
| GET | `/loja/produtos/{slug}` | Produto (variantes, specs, mídias) | #21 | a fazer |
| GET | `/loja/produtos/{slug}/compativeis` | Acessórios compatíveis | #11 | a fazer |
| GET | `/loja/ofertas` | Ofertas e Kits com desconto | #14 | a fazer |
| GET | `/loja/busca?q=` | busca (registra o termo) | #43 | a fazer |
| GET | `/loja/comparar?ids=` | comparação de especificações | #44 | a fazer |
| GET POST | `/loja/produtos/{slug}/avaliacoes` | Avaliações (compra verificada) | #39 | a fazer |
| GET POST | `/loja/produtos/{slug}/perguntas` | perguntas e respostas | #40 | a fazer |

## Compra

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET POST DELETE | `/conta/desejos` · `/conta/desejos/{idVariante}` | Lista de desejos | #22 | a fazer |
| GET · POST · PATCH DELETE | `/carrinho` · `/carrinho/itens` · `/carrinho/itens/{id}` | Carrinho anônimo por token | #24 | a fazer |
| POST | `/carrinho/vincular` | junta o carrinho anônimo ao do cliente no login | #24 | a fazer |
| POST DELETE | `/carrinho/cupom` | Aplicar cupom | #25 | a fazer |
| POST | `/frete/cotacao` | Calcular frete (CEP + itens; agregador com fallback, D4) | #30 | a fazer |
| POST | `/checkout` | Finalizar compra: reserva estoque (#23) e cria o pedido | #26, #29 | a fazer |
| POST | `/pagamentos` · `/webhooks/mercadopago` | pagar (sandbox, D2) e receber a confirmação | #27 | a fazer |
| GET POST DELETE · PATCH | `/conta/cartoes` · `/conta/cartoes/{id}` · `/conta/cartoes/{id}/padrao` | Meios de pagamento (token do gateway) | #28 | a fazer |
| GET | `/conta/pedidos` · `/conta/pedidos/{id}` | Meus pedidos, Pedido confirmado | #29 | a fazer |

## Pós-venda e atendimento

| Método | Rota | Uso | Issue | Situação |
|---|---|---|---|---|
| GET POST · GET | `/conta/devolucoes` · `/conta/devolucoes/{id}` | pedir devolução ou troca | #37 | a fazer |
| GET · PATCH | `/admin/devolucoes` · `/admin/devolucoes/{id}/status` | tratar devolução | #37 | a fazer |
| POST | `/pagamentos/{id}/estorno` | estorno total ou parcial | #36 | a fazer |
| POST | `/chamados` | Suporte: Enviar chamado | #38 | a fazer |
| GET · GET POST | `/conta/chamados` · `/chamados/{id}/mensagens` | histórico do chamado | #38 | a fazer |
| GET · PATCH | `/admin/chamados` · `/admin/avaliacoes/{id}` · `/admin/perguntas/{id}` | atendimento e moderação | #38, #39, #40 | a fazer |

## Telas que pedem rota sem issue

| Tela / ação | Rota sugerida |
|---|---|
| Contagem de produtos nas listagens de marca, categoria e fornecedor | campo `quantidadeProdutos` na resposta da listagem |
| Produtos: Ativar, Desativar, Alterar preço, Mover categoria (em lote) | `PATCH /produtos/lote` |
| Categorias: Reordenar | `PATCH /categorias/ordem` |
| Exportar CSV (marcas, fornecedores, produtos, estoque, pedidos) | `GET .../exportar?formato=csv` |
| Pedidos: Imprimir etiqueta(s) | `GET /pedidos/{id}/etiqueta` |
| Empresas: Solicitar proposta | `POST /leads-empresas` |

## Protótipo × modelo

O protótipo tem campos que o modelo não tem; a implementação segue o modelo até haver decisão:

- Cadastrar categoria e Categorias mostram **Categoria pai**, que saiu do modelo (RF02: categoria e subcategoria separadas).
- Cadastrar fornecedor pede **Inscrição estadual**, **Observações internas** e **Disponível para vincular a novos produtos**, que não existem em `Fornecedor`.
