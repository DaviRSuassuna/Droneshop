# Regras de negócio: Aeria Drones

> Documento vivo. Descreve **o que o sistema deve fazer**: entidades, fluxos, validações e regras de domínio. Consolida o PRD ([`PRD-Aeria.md`](PRD-Aeria.md), seções 4 a 6 e decisões `D1` a `D40`), os contratos das issues ([`issues/`](issues/)) e o que já está codificado no backend. Em caso de conflito, a ordem de precedência é: **decisão `Dn` do PRD -> contrato da issue -> este documento**. Atualize-o no mesmo PR que mudar uma regra.
>
> Legenda de situação: `[IMPLEMENTADO]` implementado e testado · `[PENDENTE]` contrato fechado, a implementar.

Modelo de dados implementado: [`modelo.plantuml`](modelo.plantuml). Modelo-alvo completo por contexto: [`diagramas/`](diagramas/).

---

## 1. Regras transversais

| # | Regra | Origem |
|---|---|---|
| T1 | Todo registro persistido tem `id` (`Long`), `dataCadastro` e `dataAlteracao` preenchidos automaticamente (`DefaultEntity`). | RNF01 |
| T2 | Todo enum de domínio tem id (a partir de 1, na ordem do diagrama) e nome. A API recebe `idXxx` e devolve `{id, nome}`; os valores ficam numa rota pública em kebab-case (`/status-marca`, `/tipos-movimento`...). | D31 |
| T3 | **Rascunho**: cadastros com status `RASCUNHO` (id 1) aceitam campos vazios; texto em branco é tratado como não informado. Os obrigatórios só valem para os status publicados (`ATIVO/ATIVA`, `INATIVO/INATIVA`), e todos os faltantes voltam num único 400 com `errors[]`. | D21, D28 |
| T4 | **Em uso não se exclui, se inativa**: excluir registro referenciado responde 409 com a contagem e a orientação de inativar. Nunca há exclusão em cascata de dados de negócio. | D24, D28 |
| T5 | Registro inexistente responde 404 em consulta, alteração e exclusão; rota desconhecida também. | D22 |
| T6 | Transição de status inválida responde 409. A API não usa 422. | D30 |
| T7 | Ids de outras entidades no corpo (`idMarca`, `idProduto`...) são conferidos no banco; inexistente -> 400 no campo correspondente. | SECURITY |
| T8 | Slugs são únicos por entidade; gerados a partir do nome quando não informados; rascunho sem nome recebe slug temporário. | Geral |
| T9 | Busca por nome não diferencia maiúsculas (marca, categoria, fornecedor); o filtro de produto também ignora acentos. | D23 |
| T10 | Dinheiro é `BigDecimal` com escala 2 (inclusive `0.00`). CPF, CNPJ, CEP e telefone trafegam só com dígitos; a máscara é do front. | API.md |
| T11 | Listagens paginam por `page`/`pageSize` (padrão 10) e respondem `PageResponse`. | API.md |
| T12 | Registros com **origem** (`MANUAL`, `SISTEMA`, `INTEGRACAO`) só têm autor quando manuais; o autor vem sempre do usuário do token, nunca do corpo. | D11 |
| T13 | Preço, frete e endereço são **congelados** no pedido; alterações posteriores no catálogo ou no cadastro do cliente não afetam pedidos existentes. | RN06, RN07, RN15, D12 |
| T14 | Só dados fictícios em carga de dados, testes e protótipo. | SECURITY |

---

## 2. Identidade e administração

### Entidades
- **Usuario**: conta única com `nome`, `email` (único, login), `senhaHash` (BCrypt; nulo em conta só-Google), `emailVerificado`, `ativo`.
- **PerfilAdministrativo** (0..1 por usuário): `papel` (`ADMIN`, `OPERADOR`, `CATALOGO`, `ATENDIMENTO`, `SOMENTE_LEITURA`), `twoFAHabilitado`, permissões.
- **PermissaoModulo**: por módulo (`CATALOGO`, `ESTOQUE`, `PEDIDO`, `PAGAMENTO`, `DEVOLUCAO`, `ATENDIMENTO`, `NOTIFICACAO`, `ADMINISTRACAO`) com `podeVer`, `podeEditar`, `podeExcluir`.
- **PerfilCliente** (0..1 por usuário): `tipoPessoa` (`PF`, `PJ`), `nome`, `documento` (CPF/CNPJ, único), `telefone`.
- **ConfiguracaoLoja**: registro único com dados fiscais, CEP de origem do frete, remetente padrão, prazos de arrependimento, garantia (padrão) e análise de devolução, frete reverso por conta da loja.

### Regras
- `[IMPLEMENTADO]` Login por **e-mail e senha** com JWT; o documento não é login (D1). (#4)
- `[IMPLEMENTADO]` Perfis são **acumuláveis**: a mesma conta pode ser cliente e administrativa. (PRD seção 3)
- `[IMPLEMENTADO]` Rotas do admin exigem perfil administrativo **e** a permissão do módulo da rota (`@RolesAllowed("ADMINISTRATIVO")` + `@ModuloAdmin`). (#5)
- `[IMPLEMENTADO]` Permissões são herdadas do papel (`/admin/papeis/{id}/permissoes-padrao`) e ajustáveis por usuário. (RF43)
- `[IMPLEMENTADO]` O admin cria usuário com nome, e-mail, papel, senha provisória e permissões; sem telefone, equipe, foto, status "Suspenso" ou "Exigir 2FA" (D35).
- `[IMPLEMENTADO]` Configuração da loja é registro único (`GET`/`PUT /admin/configuracao`); o prazo de garantia dela é só o padrão sugerido no cadastro de produto (D15). (#6)
- `[PENDENTE]` Escolha do perfil ativo no login e sessões revogáveis (#20); verificação de e-mail e recuperação de senha por token expirável (#33); login com Google por ID token, identificado pelo `sub` (D6, D37, #47); 2FA opcional pelo próprio usuário (#45); consentimento LGPD versionado e exclusão/anonimização (#46); auditoria com autor, campo, valor anterior/novo, IP e data (#41).
- `[PENDENTE]` Em `/conta/**`, o cliente só acessa os próprios dados: o id vem do token.

---

## 3. Catálogo

### Entidades
- **Marca**: `nome`, `slug`, `paisOrigem`, `siteOficial`, `descricao`, `logoUrl`, `status` (`RASCUNHO`, `ATIVA`, `INATIVA`), `destacarHome`; `quantidadeProdutos` derivado.
- **Categoria**: `nome`, `slug`, `ordemExibicao`, `descricao`, `status`, `exibirMenu`, `iconeUrl`, **`tipo`** (`DRONE`, `ACESSORIO`, `KIT`). Sem categoria pai.
- **Subcategoria**: pertence a uma `Categoria`; `nome`, `slug`, `ordemExibicao`, `descricao`, `status`.
- **Fornecedor**: `razaoSocial`, `cnpj` (único), `nomeContato`, `emailContato`, `telefoneContato` (D18), endereço estruturado (CEP, logradouro, número, complemento, bairro, `Municipio` IBGE), `status` (`ATIVO`, `INATIVO`).
- **Produto** (abstrato, herança `JOINED`, D27/D40): a **unidade de venda**, com `nome`, `sku` (único), `slug`, `descricaoCurta`, `descricao`, SEO (`metaTitle`, `metaDescription`, `tags`), `garantiaMeses`, `dataLancamento`, `status` (`RASCUNHO`, `ATIVO`, `INATIVO`), `destaque`, `preco`, `custoAquisicao`, `estoqueFisico`, `estoqueMinimo`, embalagem (`pesoEmbalagemKg`, altura/largura/comprimento em cm), marca, subcategoria, fornecedor, grupo (`rotuloNoGrupo`, `ordemNoGrupo`).
  - **Drone**: specs fixas (D14) como peso em gramas, dimensões dobrado/aberto, autonomia, alcance, velocidade, resolução de foto e vídeo, sensor, estabilização, bateria (tipo, mAh, tempo de carga), GPS, anticolisão, exigência e classe ANAC, registro SISANT.
  - **Acessorio**: `tipoAcessorio`; compatível com N produtos (D17).
  - **Kit**: sem estoque próprio; composto por **ComponenteKit** (`produto`, `quantidade`).
- **GrupoProduto**: `nome`, `slug`, `descricaoCurta`, `ativo`; junta versões numa página de venda (D40).
- **MidiaProduto**: `tipo` (`IMAGEM`, `VIDEO`, `MANUAL_PDF`), `url`, `principal`, `ordem`, `altText`.
- **Promocao**: `nome`, `escopo` (`PRODUTO`, `CARRINHO`, `FRETE`), vigência (`dataInicio`, `dataFim`), `percentualDesconto` ou `valorDesconto`, `valorMinimoCarrinho`, `ativa`, produtos.

### Regras de cadastro
- `[IMPLEMENTADO]` **Marca** publicada exige `nome`, `paisOrigem` e `logoUrl`. Excluir marca com produtos -> 409. (base)
- `[IMPLEMENTADO]` **Categoria** publicada exige `nome`, `ordemExibicao` e `idTipo`. Excluir categoria com subcategorias -> 409; categoria com produtos não muda de tipo (409). Reordenação por `PATCH /categorias/ordem` sem ids repetidos. (base, #8, #61)
- `[IMPLEMENTADO]` **Subcategoria** publicada exige `nome` e `idCategoria`; em rascunho pode ficar sem categoria (`categoria: null`). Excluir com produtos -> 409; com produtos não muda de tipo. (base, #8)
- `[IMPLEMENTADO]` **Fornecedor**: CNPJ válido e único, município existente; excluir com produtos -> 409. (base)
- `[IMPLEMENTADO]` **Tipo × categoria (D39)**: drone só entra em subcategoria de categoria de drone; acessório só em de acessório (400 em `idSubcategoria`); kit pode entrar em subcategoria de qualquer tipo. A subcategoria escolhida precisa ter categoria definida. Não existem categorias "Drones"/"Acessórios": isso é o tipo.
- `[IMPLEMENTADO]` **Escrita por tipo (D27)**: `POST`/`PUT` em `/drones`, `/acessorios`, `/kits`; `/produtos` só lista, consulta, exclui e filtra (POST/PUT -> 405). Id de outro tipo na rota do tipo -> 404.
- `[IMPLEMENTADO]` **Produto publicado** (`ATIVO`/`INATIVO`) exige `nome`, `sku`, marca, subcategoria, fornecedor, `preco` e **peso e as três dimensões da embalagem** (RN18), além dos obrigatórios do tipo (ex.: `pesoGramas` no drone, `tipoAcessorio` no acessório, `classeAnac` quando o drone exige registro ANAC). Rascunho só exige o status (D28).
- `[IMPLEMENTADO]` `sku` e `slug` únicos entre todos os produtos.
- `[IMPLEMENTADO]` **Margem** = (preço - custo) / preço, em %, calculada no backend (`margemPercentual`) (D26).
- `[IMPLEMENTADO]` **Estoque físico** só é informado na criação (gera movimento `ENTRADA` "Saldo inicial"); no `PUT` é ignorado. Depois, só muda por movimento de estoque.
- `[IMPLEMENTADO]` **Kit (D7, D40)**: não aceita `estoqueFisico`; componente precisa existir, não pode ser kit, não pode ser o próprio kit e não pode se repetir (some as quantidades). Publicar exige ao menos um componente. Disponibilidade = mínimo, entre os componentes, da divisão inteira entre disponível e quantidade exigida. Peso e dimensões da embalagem derivam dos componentes, salvo sobrescrita manual (D16; resposta indica `pesoSobrescrito`/`dimensoesSobrescritas`).
- `[IMPLEMENTADO]` **Grupo (D40)**: com grupo, `rotuloNoGrupo` é obrigatório (em qualquer status) e único dentro do grupo; sem grupo, rótulo e ordem são descartados. Grupo com produtos não se exclui (409). A resposta traz `versoes[]` e `precoAPartirDe`.
- `[IMPLEMENTADO]` **Exclusão de produto**: produto que é componente de kit -> 409; compatibilidade não conta como uso (os vínculos saem junto); excluir um kit remove seus componentes.
- `[PENDENTE]` Produto com pedido ou reserva -> 409 (D28): a regra está decidida, mas a exclusão de produto ainda não confere itens de pedido nem reservas.
- `[IMPLEMENTADO]` **Compatibilidade (D17)**: N:N entre `Acessorio` e `Produto`, recíproca (aparece nos dois lados como `compativeis[]` e `acessoriosCompativeis[]`). Acessório não é compatível consigo mesmo. (#11)
- `[IMPLEMENTADO]` **Mídia (D29)**: imagem (PNG/JPG) e manual (PDF) por upload `multipart` até 20 MB, com conteúdo conferido contra a extensão e nome gerado no servidor; vídeo só por URL HTTP/HTTPS. Imagem exige `altText`. Reordenação deve listar todas as mídias do produto exatamente uma vez; ao excluir, a ordem volta a 1..n. O arquivo em disco só é apagado após o commit. (#12)
- `[IMPLEMENTADO]` **Ações em lote** (`PATCH /produtos/lote`): ativar, inativar, reajustar preço por percentual, mover subcategoria. Exportação CSV de marcas, fornecedores, produtos, estoque e pedidos com os mesmos filtros da listagem. (#61)
- `[IMPLEMENTADO]` **Promoção**: vigência obrigatória; situação derivada da data (vigente, agendada, encerrada) usada como filtro; produtos referenciados devem existir; promoção já aplicada em pedido não se exclui (409). Preço promocional não é campo do produto. (#14)
- `[PENDENTE]` **Vitrine** (`/loja/**`, #21): só produtos `ATIVO` (RN17); grupo aparece como um card "a partir de" o menor preço ativo; a página da versão mostra as outras do grupo; não expõe custo, margem nem estoque mínimo. Filtros por marca, categoria, preço e specs; facetas com contagem.
- `[PENDENTE]` Termos buscados com destaque para zero resultados (#43); comparação de até 4 modelos (#44); avaliações por versão com média do grupo e moderação (#39); perguntas e respostas moderadas (#40).

---

## 4. Estoque

### Entidades
- Saldo físico em `Produto.estoqueFisico`; mínimo em `estoqueMinimo`.
- **MovimentoEstoque**: `produto`, `tipo` (`ENTRADA`, `SAIDA`, `AJUSTE`, `DEVOLUCAO`, `PERDA`), `origem`, `quantidade`, `saldoResultante`, `observacao`, `autor` (só quando manual).
- `[PENDENTE]` `ReservaEstoque` (#23), `UnidadeIndividual` (#34).

### Regras
- `[IMPLEMENTADO]` Toda alteração de estoque gera movimento com saldo resultante e origem (RF14, D11). O histórico é append-only.
- `[IMPLEMENTADO]` `ENTRADA`/`DEVOLUCAO` somam; `SAIDA`/`PERDA` subtraem; `AJUSTE` informa o **saldo novo** (maior ou igual a 0) e a quantidade registrada é a diferença. Demais tipos exigem quantidade maior ou igual a 1.
- `[IMPLEMENTADO]` Saldo nunca fica negativo -> 409 "Saldo insuficiente".
- `[IMPLEMENTADO]` Movimento em kit -> 400: lance em cada componente.
- `[IMPLEMENTADO]` A linha do produto é travada durante o movimento (sem leitura concorrente do mesmo saldo).
- `[IMPLEMENTADO]` Listagem por situação `ABAIXO_MINIMO`, `ZERADO` ou `TODOS`. (#13)
- `[PENDENTE]` **Disponível = físico - reservas ativas** (RN01). Reserva criada ao entrar no checkout, com prazo (`aeria.reserva.minutos`); pagamento aprovado converte em saída; expiração devolve o saldo (RN02), por rotina agendada e também `POST /admin/rotinas/expirar-reservas` (D33). (#23)
- `[PENDENTE]` Drone vendido recebe **unidade individual** com número de série, status, registro ANAC e garantia; item com quantidade N recebe N unidades distintas (RN12, RN14, D10). (#34)

---

## 5. Compra

### Entidades
- **Cupom**: `codigo` (único), `tipoDesconto` (`PERCENTUAL`, `VALOR_FIXO`), `valor`, vigência, `valorMinimo`, `limiteTotalUso`, `limitePorCliente`, `ativo`, restrição por produtos, categorias e marcas.
- **UsoCupom**: `cupom`, `cliente`, `pedido`, `dataUso`.
- `[PENDENTE]` `Carrinho`, `ItemCarrinho`, `ListaDesejos`.

### Regras
- `[IMPLEMENTADO]` **Cadastro de cupom** (#15): código único e sem espaços nas pontas; percentual > 0 e <= 100, valor fixo > 0; `dataFim` >= `dataInicio`; `limitePorCliente` <= `limiteTotalUso`; referências de restrição existentes e sem repetição; cupom já usado em pedido não se exclui (409). Usos exportáveis por cupom.
- `[PENDENTE]` **Carrinho** funciona sem login por token (`X-Carrinho-Token`) e é vinculado ao cliente no login; estados ativo, abandonado, convertido, expirado; rotina de abandono (`aeria.carrinho.abandono-horas`). (#24)
- `[PENDENTE]` **Aplicar cupom** (#25, RN03): exige vigência, valor mínimo atingido, limites não excedidos e restrições atendidas; fica guardado no carrinho até o fechamento.
- `[PENDENTE]` **Uso de cupom só se materializa com o pedido** (D9, RN13): `UsoCupom` aponta para o pedido; cancelamento ou não pagamento libera o limite.
- `[PENDENTE]` Item de carrinho e de pedido referenciam o **produto** escolhido (kit é uma linha só). Produto em rascunho não entra no carrinho (RN17).
- `[PENDENTE]` **Checkout** (#26): endereço -> modalidade de entrega -> pagamento -> revisão -> confirmação; cria o pedido a partir do carrinho reservado (#29).
- `[PENDENTE]` Lista de desejos independente do carrinho (#22).

---

## 6. Pedido, entrega e fiscal

### Entidades
- **Pedido**: `status`, `cliente` (`PerfilCliente`), `valorTotal`, `valorFrete`, `valorDesconto`, `enderecoEntrega` (embutido, congelado), itens, histórico, descontos.
- **ItemPedido**: `produto`, `quantidade`, `precoUnitarioCongelado`.
- **HistoricoStatusPedido**: `statusAnterior`, `statusNovo`, `origem`, `autor` (só manual), `observacao`.
- **DescontoPedido**: `origem` (`CUPOM`, `PROMOCAO`), `descricao`, `valor`, `promocao` opcional.
- `[PENDENTE]` `Entrega`, `EventoRastreio`, `RegraFrete`, `NotaFiscal`.

### Máquina de estados do pedido (`[IMPLEMENTADO]` #17)

```
PENDENTE --> PAGO --> SEPARACAO --> ENVIADO --> ENTREGUE --> EM_DEVOLUCAO --> DEVOLVIDO
   |           |          |                         ^              |
   +-----------+----------+--> CANCELADO            +--------------+
```

| De | Para |
|---|---|
| `PENDENTE` | `PAGO`, `CANCELADO` |
| `PAGO` | `SEPARACAO`, `CANCELADO` |
| `SEPARACAO` | `ENVIADO`, `CANCELADO` |
| `ENVIADO` | `ENTREGUE` |
| `ENTREGUE` | `EM_DEVOLUCAO` |
| `EM_DEVOLUCAO` | `DEVOLVIDO`, `ENTREGUE` |
| `DEVOLVIDO`, `CANCELADO` | (finais) |

Qualquer outra transição -> 409. Toda transição gera histórico com origem; autor obrigatório quando manual e nulo nos demais casos (RN08).

### Regras
- `[IMPLEMENTADO]` Listagem com filtro por status, período (`dataInicio` <= `dataFim`, formato AAAA-MM-DD) e busca; resumo de contagem por status. (#16)
- `[IMPLEMENTADO]` Endereço de entrega congelado como fotografia (cidade e UF como texto) (D12, RN15).
- `[IMPLEMENTADO]` Etiqueta PDF do pedido; sem endereço congelado -> 409. (#61)
- `[IMPLEMENTADO]` Dashboard considera faturados os pedidos em `PAGO`, `SEPARACAO`, `ENVIADO` e `ENTREGUE`; período padrão de 30 dias. (#18)
- `[PENDENTE]` Todo valor em `valorDesconto` tem procedência em ao menos um `DescontoPedido` de cupom ou promoção (RN16, RF53). (#29)
- `[PENDENTE]` **Frete híbrido** (D4, #30): expressa e padrão por agregador em tempo real; `RegraFrete` para retirada no showroom e como fallback quando a integração cai; cotação usa peso e dimensões da embalagem (no kit, derivados); custo e prazo congelados (RN07). Falha de integração não trava a compra (RNF05).
- `[PENDENTE]` Rastreio com eventos e código da transportadora (#31).
- `[PENDENTE]` **NF-e simulada** (D3, #35): número, série e chave fictícios gerados localmente; também para devolução.

---

## 7. Pagamento (`[PENDENTE]`)

- Métodos: PIX, cartão, boleto e faturamento PJ (análise de crédito). **Mercado Pago sandbox permanente** (D2). (#27)
- Um pedido tem um ou mais pagamentos (tentativas), com um vigente; confirmação por webhook (`/webhooks/mercadopago`, origem `INTEGRACAO`).
- Pagamento aprovado converte a reserva em saída de estoque e move o pedido para `PAGO`.
- Cartão salvo só como token do gateway + últimos dígitos; número, validade e CVV nunca passam pelo backend (RN11). (#28)
- Estorno total ou parcial registrado no pagamento (#36).

---

## 8. Devolução e troca (`[PENDENTE]` #37)

- Fluxo com protocolo: solicitação com motivo -> análise -> aprovação/recusa -> postagem reversa -> recebimento -> reembolso -> conclusão. Resolução: reembolso, troca ou reparo.
- Política (D8, parâmetros em `ConfiguracaoLoja`): 7 dias corridos de arrependimento com frete reverso pela loja (RN09); 90 dias para vício em durável; lacre rompido aceito se completo e sem dano; análise em até 5 dias úteis após recebimento; reembolso no método original.
- Histórico de devolução com origem e autor (D11); transição inválida -> 409 (D30). Pode gerar NF de devolução.

---

## 9. Atendimento e notificações (`[PENDENTE]`)

- **Chamado** (#38): aberto pelo cliente, com ou sem pedido; assunto, protocolo, mensagens e atendente responsável.
- **Avaliações** (#39): só quem comprou; "compra verificada" quando há item de pedido de origem (RN04); moderação pendente/aprovada/rejeitada antes de publicar (RN05).
- **Perguntas** (#40): loja ou clientes respondem; moderadas.
- **Proposta para empresas** (`nova-empresas`): `POST /leads-empresas`.
- **E-mail** (#32, D5): envio real via Resend; templates editáveis com variáveis e versionados; notificação guarda o texto renderizado; estados enviada/entregue/aberta/bounce/spam/falha via webhook.
- **Marketing** (#42, RN10): exige opt-in e link de descadastro; transacional sempre é enviado; preferências por tipo.

---

## 10. Segurança e privacidade (resumo, ver [`../SECURITY.md`](../SECURITY.md))

- Senha só com hash BCrypt; nunca em resposta ou log.
- Rotas públicas não expõem custo, margem, estoque mínimo nem dados de outros usuários.
- Consultas sempre parametrizadas; DTO de entrada com Bean Validation e tamanho máximo nos textos.
- Log não registra token, senha, documento completo nem dado de cartão.
- Dados pessoais seguem a LGPD: consentimento versionado, exportação e exclusão/anonimização (#46).

---

## Como manter este documento

Atualize no **mesmo PR** que:
- implementa uma regra marcada `[PENDENTE]` (troque para `[IMPLEMENTADO]` e ajuste o texto ao que foi codificado);
- cria ou altera entidade, enum, validação, transição de estado ou mensagem de conflito;
- registra ou altera uma decisão `Dn` ou regra `RNnn` no PRD.

Se a regra mudou no modelo de dados, atualize também [`modelo.plantuml`](modelo.plantuml), o `.puml` correspondente em [`diagramas/`](diagramas/) e o `backend/src/main/resources/import.sql`.
