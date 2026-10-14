# PRD: Aeria Drones

**Produto:** E-commerce de drones e acessórios (storefront + painel administrativo)
**Versão do documento:** 1.6
**Status:** rascunho para validação
**Base:** protótipo de 36 telas + 8 diagramas de classes por contexto delimitado, em `diagramas/`

---

## 1. Resumo

A Aeria é uma loja online de drones e acessórios. O produto tem duas faces: uma **loja** onde o cliente pesquisa, compara especificações técnicas, compra e acompanha o pedido; e um **painel administrativo** onde a operação cadastra catálogo, controla estoque, processa pedidos, trata devoluções e atende clientes.

O que diferencia este e-commerce de uma loja genérica é a natureza do produto: drone é bem de **alto valor unitário**, com **ficha técnica extensa** que pesa na decisão de compra, **garantia** relevante e, em parte dos modelos, **registro na ANAC**. Isso implica rastreabilidade por número de série, catálogo com muitos atributos comparáveis e um pós-venda mais exigente que o de varejo comum.

**Este é um projeto de testes/estudo, não uma operação comercial real.** Isso não muda o escopo funcional nem o modelo de dados (todos os fluxos de pagamento, nota fiscal e devolução existem de ponta a ponta), mas muda como duas peças externas são resolvidas: pagamento roda em sandbox (sem dinheiro real) e a nota fiscal é simulada (sem emissão fiscal válida). Ver decisões D2 e D3.

## 2. Objetivos

1. Permitir a compra completa e autônoma, do catálogo à confirmação do pedido, sem intervenção humana.
2. Dar à operação controle sobre catálogo, estoque e pedidos sem depender de desenvolvedor.
3. Rastrear cada unidade vendida (qual número de série foi para qual cliente) para sustentar garantia e obrigações regulatórias.
4. Registrar de forma auditável quem alterou preço, estoque e status de pedido.

### Não-objetivos desta versão

- Marketplace com vendedores terceiros.
- Aplicativo móvel nativo.
- Loja física / PDV integrado.
- Vendas internacionais (moeda, idioma e tributação estrangeira).
- Assinatura ou recorrência.

## 3. Público

| Perfil | Quem é | O que precisa |
|---|---|---|
| Piloto profissional | Fotógrafo, cinegrafista, topógrafo | Especificação técnica precisa, nota fiscal, garantia |
| Piloto recreativo | Hobbista | Comparação de modelos, preço, facilidade de uso |
| Comprador corporativo | Empresa, órgão público | Compra por CNPJ, faturamento a prazo, NF-e |
| Operador da loja | Equipe Aeria | Cadastro, estoque, pedidos, devoluções, atendimento |

Um detalhe estrutural: **a conta é única e os perfis são acumuláveis**. `Usuario` guarda o login; `PerfilCliente` e `PerfilAdministrativo` são opcionais e independentes. Um funcionário da Aeria pode também ser cliente, e escolhe com qual perfil entrar no momento do login. A escolha fica registrada na sessão.

## 4. Escopo funcional

### 4.1 Catálogo

Produto se divide em **Drone** e **Acessório**, com atributos próprios de cada tipo. As especificações técnicas são **colunas fixas e tipadas** na entidade, não atributo dinâmico (ver D14): em `Drone` são peso, dimensões dobrado/aberto, autonomia, alcance, velocidade máxima, resolução de foto, sensor, resolução de vídeo, estabilização, tipo e capacidade de bateria, tempo de carregamento, GPS, anticolisão, exigência e classe ANAC, registro SISANT. Spec nova é migration, e o preço disso é assumido conscientemente. Todo produto pertence a uma marca, uma subcategoria (que pertence a uma categoria) e um fornecedor. A compatibilidade entre acessório e produto é **relação N:N**, não texto descritivo (ver D17): é ela que alimenta a lista de acessórios compatíveis na página do drone e a sugestão do carrinho.

A **unidade vendável é a variante**, não o produto: "Padrão", "Fly More", "Enterprise" têm SKU, preço, custo e estoque próprios. Produto sem opções tem uma variante única. Isso existe porque uma configuração pode esgotar enquanto outra continua disponível.

A variante também guarda **peso e dimensões da embalagem**, que são a base da cotação de frete e mudam por configuração (a caixa do Fly More leva baterias e mochila a mais). Isso é distinto do `pesoGramas` do drone, que é spec de exibição e filtro. Ver D16.

O produto tem três estados de publicação (rascunho, ativo, inativo), não um booleano, para permitir o rascunho salvo automaticamente que a tela de cadastro já prevê.

Garantia é definida **por produto** em meses, com o prazo de `ConfiguracaoLoja` servindo apenas de valor padrão no cadastro (D15).

**Kit e combo são uma variante sem estoque próprio.** Uma `VarianteProduto` marcada como kit (`ehKit`) não guarda `estoqueFisico`: em vez disso, ela reúne outras variantes como componentes, cada uma com a quantidade exigida (ex.: kit "Drone X + Bateria Extra" = 1 unidade da variante do drone + 1 da bateria). Vender o kit dá baixa no estoque de cada componente, nunca num contador próprio do kit: do contrário, vender a bateria avulsa e dentro do kit no mesmo dia contaria dois estoques que na verdade são a mesma peça física. A disponibilidade do kit é sempre derivada: o mínimo entre os componentes disponíveis, dividido pela quantidade que cada um exige.

Preço promocional não é campo do produto: é uma **Promoção com vigência** (início e fim), que pode ter escopo de produto, de carrinho (desconto acima de um valor) ou de frete.

Requisitos:
- Busca e filtros por marca, categoria, faixa de preço e atributos técnicos.
- Comparação entre modelos.
- Galeria de imagens, vídeo e manual em PDF.
- URL amigável por slug, com meta title e description.
- Registro de termos buscados, com destaque para os que retornam zero resultados.

### 4.2 Estoque

Estoque físico fica na variante. O disponível para venda é o físico menos as **reservas ativas**.

- Ao entrar no checkout, cria-se reserva com prazo de expiração.
- Pagamento aprovado converte a reserva em saída; expiração devolve o saldo.
- Toda alteração gera **movimento** (entrada, saída, ajuste, devolução, perda) com saldo resultante e **origem** (`MANUAL`, `SISTEMA`, `INTEGRACAO`). O autor só é preenchido quando a origem é manual: baixa por pagamento aprovado vem de webhook e expiração de reserva vem de job, nenhuma das duas tem pessoa responsável (ver D11).
- Itens serializados (drones) têm **unidade individual** com número de série, status, registro ANAC e validade de garantia. A unidade aponta para o item do pedido na expedição, e **um item com quantidade N recebe N unidades** (ver D10).

### 4.3 Compra

- Carrinho **funciona sem login**, identificado por token de sessão, e é vinculado ao cliente quando ele se autentica.
- Carrinho tem estado (ativo, abandonado, convertido, expirado) para permitir recuperação por e-mail.
- Item de carrinho e item de pedido sempre referenciam a **variante** (nunca o produto diretamente): é ela que tem preço e estoque, kit incluso como uma linha só.
- Cupom com validade, valor mínimo, limite total e limite por cliente, restrição por produto/categoria/marca.
- O cupom aplicado fica guardado no **carrinho** enquanto o cliente não fecha a compra. O **uso só se materializa quando o pedido é criado**: `UsoCupom` aponta para o pedido, de modo que cancelamento ou recusa de pagamento liberam o limite de volta ao cliente (ver D9).
- Todo desconto aplicado no pedido fica discriminado por origem (cupom ou promoção, com valor de cada um), dando procedência ao total em `Pedido.valorDesconto`.
- Lista de desejos.
- Checkout: endereço, modalidade de entrega, pagamento, revisão e confirmação.

### 4.4 Pagamento

Métodos: PIX, cartão, boleto e faturamento PJ (sujeito a análise de crédito).

**Este é um projeto de testes: não processa dinheiro real.** Gateway: **Mercado Pago em modo sandbox**, permanentemente, não como etapa transitória. Cartões, PIX e boleto são os simulados pelo próprio sandbox; não há conta de produção nem credenciamento comercial.

Um pedido tem **um ou mais pagamentos**: recusa de cartão é rotina e o cliente tenta novamente, às vezes por outro método. Cada tentativa é um registro, com o vigente marcado. Estorno parcial de devolução também se registra aqui. O cliente pode salvar cartões, guardados como token do gateway e últimos dígitos, nunca o número completo (isso vale mesmo em sandbox, para manter o hábito de não tocar em dado sensível).

### 4.5 Pedido, entrega e fiscal

- Estados: pendente, pago, separação, enviado, entregue, em devolução, devolvido, cancelado.
- Cada transição fica em histórico, com **origem** (`MANUAL`, `SISTEMA`, `INTEGRACAO`), observação e, quando manual, o autor. Transições disparadas por webhook do gateway ou por evento de rastreio não têm pessoa responsável (ver D11).
- O **endereço de entrega é congelado no pedido**, como o preço e o frete: editar ou excluir o endereço do cliente depois não altera pedidos já feitos (ver D12).
- Frete calculado em modelo **híbrido**: para as modalidades expressa e padrão, cotação em tempo real via integração com agregador de transportadoras (fora do domínio modelado, chamada externa); `RegraFrete` (tabela própria) cobre a retirada no showroom (custo fixo, normalmente zero) e serve de fallback quando a integração cai. `Entrega` guarda `origemCalculo`, `transportadora` e `codigoServicoTransportadora` além do custo/prazo já congelados como fotografia do momento.
- Modalidades: expressa, padrão e retirada no showroom.
- Rastreio com eventos.
- **NF-e simulada**, projeto de testes, sem emissão fiscal real. `emitir()` gera número, série e chave de acesso fictícios localmente; não há certificado digital A1, credenciamento na SEFAZ nem emissor terceirizado (Focus NFe, NFe.io). `xmlUrl`/`danfeUrl` apontam para um arquivo gerado localmente, não um documento fiscal válido.

### 4.6 Devolução e troca

Fluxo próprio, com protocolo: solicitação com motivo, análise interna, aprovação ou recusa, postagem reversa, recebimento, reembolso, conclusão. Resolução pode ser reembolso, troca ou reparo. A devolução pode gerar nota fiscal própria.

Política (ver D8): 7 dias corridos de arrependimento com frete reverso por conta da Aeria; 90 dias para vício em durável; lacre rompido aceito; análise em até 5 dias úteis após o recebimento; reembolso no método de pagamento original.

### 4.7 Atendimento

Chamado aberto pelo cliente (com ou sem pedido vinculado), com assunto, protocolo, histórico de mensagens e atendente responsável.

### 4.8 Notificações

Canal desta versão: **e-mail**, com **envio real** (diferente de pagamento e nota fiscal, que são simulados neste projeto; aqui o e-mail de fato chega). Provedor: **Resend**, plano gratuito. Modelo preparado para outros canais sem mudança estrutural.

- Textos em **template editável pelo administrador**, com variáveis, versionado, não hardcoded.
- Notificação guarda o texto já renderizado, como histórico do que foi enviado.
- Estado acompanha o ciclo real: enviada ao provedor, entregue, aberta, bounce, spam, falha, casados por identificador do provedor.
- Separação entre transacional (o cliente sempre recebe) e marketing (exige opt-in e link de descadastro).
- Preferência por tipo de notificação.

### 4.9 Administração e auditoria

- Papéis: admin, operador, catálogo, atendimento, somente leitura.
- Permissão por módulo (ver, editar, excluir), herdada do papel e ajustável por usuário.
- 2FA opcional por perfil.
- Log de auditoria: entidade, ação, campo, valor anterior e novo, autor, IP, data.
- Configuração da loja: dados fiscais, CEP de origem do frete, remetente padrão, prazos de arrependimento e garantia.

### 4.10 Conta e privacidade

Verificação de e-mail, recuperação de senha por token (só se aplica a quem tem senha local), sessões revogáveis, consentimento LGPD datado e versionado, e exclusão/anonimização de conta.

**Login social com Google**, além do login local por e-mail e senha. Uma conta (`Usuario`) pode ter um ou mais provedores vinculados: quem entra só via Google pode nunca definir senha; quem já tem conta local pode vincular o Google depois. O identificador usado é o `sub` do Google (estável), não o e-mail, que pode mudar de lado a lado.

## 5. Requisitos funcionais

| # | Área | Requisito |
|---|---|---|
| RF01 | Catálogo | Cadastrar produtos do tipo Drone e Acessório, cada um com atributos técnicos próprios. |
| RF02 | Catálogo | Todo produto pertence a uma marca, uma subcategoria e um fornecedor. Categoria e subcategoria são entidades separadas: cada subcategoria pertence a uma categoria, sem categoria pai. |
| RF03 | Catálogo | Produto tem uma ou mais variantes vendáveis, cada uma com SKU, preço adicional, custo, estoque e dimensões de embalagem próprios. |
| RF04 | Catálogo | Variante pode ser marcada como kit, composta por outras variantes com quantidade definida, sem estoque próprio. |
| RF05 | Catálogo | Cadastrar promoções com vigência (início/fim) e escopo de produto, carrinho ou frete. |
| RF06 | Catálogo | Buscar e filtrar produtos por marca, categoria, faixa de preço e atributos técnicos. |
| RF07 | Catálogo | Comparar especificações entre dois ou mais modelos. |
| RF08 | Catálogo | Produto suporta galeria de imagens, vídeo e manual em PDF. |
| RF09 | Catálogo | Produto tem URL amigável (slug) e campos de SEO (meta title/description). |
| RF10 | Catálogo | Registrar termos buscados, sinalizando os que não retornaram resultado. |
| RF11 | Estoque | Controlar estoque físico e mínimo por variante, não por produto. |
| RF12 | Estoque | Criar reserva de estoque com prazo de expiração ao iniciar o checkout. |
| RF13 | Estoque | Pagamento aprovado confirma a reserva como saída definitiva; expiração da reserva devolve o saldo. |
| RF14 | Estoque | Toda alteração de estoque gera um movimento (entrada, saída, ajuste, devolução, perda), com saldo resultante e origem (manual, sistema ou integração); autor só quando manual. |
| RF15 | Estoque | Drone vendido tem unidade individual rastreada por número de série, vinculada ao item do pedido na expedição; um item com quantidade N recebe N unidades distintas. |
| RF16 | Compra | Manter carrinho sem login, identificado por token de sessão. |
| RF17 | Compra | Vincular carrinho anônimo ao cliente automaticamente quando ele se autentica. |
| RF18 | Compra | Marcar e permitir notificar carrinhos abandonados. |
| RF19 | Compra | Manter lista de desejos independente do carrinho. |
| RF20 | Compra | Aplicar cupom de desconto no carrinho, respeitando validade, valor mínimo e limites de uso; o cupom aplicado fica guardado no carrinho até o fechamento. |
| RF21 | Compra | Checkout coleta endereço de entrega, modalidade de entrega e forma de pagamento antes da confirmação. |
| RF22 | Pagamento | Aceitar PIX, cartão, boleto e faturamento PJ como métodos de pagamento. |
| RF23 | Pagamento | Pedido pode ter mais de um registro de pagamento (novas tentativas após recusa), com um marcado como vigente. |
| RF24 | Pagamento | Cliente pode salvar cartões, armazenando apenas token do gateway e dígitos mascarados. |
| RF25 | Pagamento | Sistema permite estorno total ou parcial de um pagamento. |
| RF26 | Pedido/Entrega | Pedido percorre estados definidos, com histórico de transições registrando origem e, quando manual, autor. |
| RF27 | Pedido/Entrega | Frete calculado via integração com agregador de transportadoras para expressa/padrão, com tabela própria como fallback e para retirada no showroom. |
| RF28 | Pedido/Entrega | Entrega registra transportadora, código de rastreio e eventos de rastreio. |
| RF29 | Fiscal | Emitir nota fiscal (simulada) vinculada ao pedido. |
| RF30 | Devolução | Cliente solicita devolução ou troca de um pedido, informando motivo. |
| RF31 | Devolução | Solicitação percorre análise, aprovação/recusa, postagem reversa, recebimento e conclusão. |
| RF32 | Devolução | Aplicar a política de devolução configurada (prazo de arrependimento, aceite de lacre rompido, prazo de análise, método de reembolso). |
| RF33 | Atendimento | Cliente abre chamado de suporte, associado ou não a um pedido. |
| RF34 | Atendimento | Atendente responde e encerra um chamado, com histórico de mensagens preservado. |
| RF35 | Avaliações | Cliente avalia produtos que comprou; avaliação é marcada como compra verificada automaticamente. |
| RF36 | Avaliações | Avaliações e perguntas passam por moderação (pendente/aprovada/rejeitada) antes de publicação. |
| RF37 | Avaliações | Cliente pergunta sobre um produto; loja ou outros clientes podem responder. |
| RF38 | Notificações | Enviar e-mails transacionais reais (confirmação de pedido, pagamento aprovado, envio, rastreio, devolução, resposta de chamado). |
| RF39 | Notificações | Textos de e-mail são editáveis pelo administrador via template com variáveis, sem necessidade de novo deploy. |
| RF40 | Notificações | Registrar o resultado real de entrega do e-mail (entregue, aberto, bounce, spam) via webhook do provedor. |
| RF41 | Notificações | Cliente configura preferência de recebimento por tipo de notificação; e-mail de marketing inclui link de descadastro. |
| RF42 | Administração | Cadastrar usuários administrativos com papel (admin, operador, catálogo, atendimento, somente leitura). |
| RF43 | Administração | Permissões de módulo (ver, editar, excluir) são herdadas do papel e ajustáveis por usuário. |
| RF44 | Administração | Administrador pode habilitar 2FA na própria conta. |
| RF45 | Administração | Registrar log de auditoria para ações administrativas sensíveis, com autor, campo alterado e valores anterior/novo. |
| RF46 | Administração | Manter configuração única da loja (dados fiscais, CEP de origem, prazos de garantia e devolução). |
| RF47 | Conta | Usuário se autentica por e-mail/senha ou por login social com Google. |
| RF48 | Conta | Uma conta tem perfil de cliente e perfil administrativo simultaneamente, escolhendo qual usar no login. |
| RF49 | Conta | Oferecer verificação de e-mail e recuperação de senha por token, quando aplicável. |
| RF50 | Conta | Cliente pode revogar sessões ativas da própria conta. |
| RF51 | Conta | Registrar consentimento LGPD datado e versionado; permitir exclusão/anonimização da conta mediante solicitação. |
| RF52 | Compra | Registrar o uso de cupom vinculado ao pedido; cancelamento do pedido devolve o uso ao limite do cliente. |
| RF53 | Pedido/Entrega | Discriminar cada desconto do pedido por origem (cupom ou promoção) e valor. |
| RF54 | Pedido/Entrega | Congelar o endereço de entrega no pedido no momento da compra. |
| RF55 | Catálogo | Produto tem três estados de publicação: rascunho, ativo e inativo. |
| RF56 | Catálogo | Garantia é definida em meses por produto, herdando o padrão da configuração da loja. |
| RF57 | Pedido/Entrega | Cotação de frete usa peso e dimensões da embalagem da variante; no kit, derivados dos componentes salvo sobrescrita manual. |

## 6. Regras de negócio

| # | Regra |
|---|---|
| RN01 | Não se vende acima do estoque disponível (físico menos reservas ativas). |
| RN02 | Reserva não confirmada expira e devolve o saldo. |
| RN03 | Cupom exige validade vigente, valor mínimo atingido e limites de uso não excedidos. |
| RN04 | Avaliação só é "compra verificada" se houver item de pedido de origem. |
| RN05 | Avaliação e pergunta passam por moderação antes de publicar. |
| RN06 | Preço do item é congelado no pedido; alteração posterior no catálogo não afeta pedidos existentes. |
| RN07 | Custo e prazo de frete são congelados no pedido. |
| RN08 | Toda transição de status de pedido gera histórico com origem registrada (manual, sistema ou integração); o autor é obrigatório quando a origem é manual e nulo nos demais casos. |
| RN09 | Devolução só é aceita dentro do prazo de arrependimento configurado. |
| RN10 | Marketing exige opt-in e link de descadastro; transacional não. |
| RN11 | Número de cartão nunca é armazenado, apenas token do gateway e dígitos mascarados. |
| RN12 | Drone entregue tem número de série vinculado ao item do pedido. |
| RN13 | Uso de cupom só se materializa com a criação do pedido; pedido cancelado ou não pago libera o uso de volta ao limite do cliente. |
| RN14 | Item de pedido com quantidade N de produto serializado recebe N unidades individuais distintas. |
| RN15 | Endereço de entrega é congelado no pedido; alteração posterior no cadastro do cliente não afeta pedidos existentes. |
| RN16 | Todo valor em `Pedido.valorDesconto` tem procedência registrada em pelo menos um desconto de origem cupom ou promoção. |
| RN17 | Produto em rascunho não aparece no storefront nem pode ser adicionado ao carrinho. |
| RN18 | Variante sem peso e dimensões de embalagem não pode ser publicada, porque inviabiliza a cotação de frete. |

## 7. Requisitos não-funcionais

| # | Categoria | Requisito |
|---|---|---|
| RNF01 | Arquitetura | API Quarkus/Java seguindo o padrão do projeto de referência: `DefaultEntity` como `@MappedSuperclass` com id `Long` e auditoria automática; enums de domínio com id e nome, conversor JPA e endpoint de leitura para popular selects; DTO de entrada com Bean Validation e DTO de saída como `record`; `PanacheRepository`; service com transação e validação própria; paginação por `page`/`pageSize`. |
| RNF02 | Segurança | Senha com hash forte quando existir (contas só-Google não têm); OAuth 2.0/OIDC para login com Google; tokens de sessão e de recuperação expiráveis; 2FA opcional no admin; dado de cartão nunca armazenado, só token do gateway e dígitos mascarados. |
| RNF03 | Privacidade (LGPD) | Consentimento registrado e versionado por tipo; dados pessoais fora de log; exclusão/anonimização de conta sob solicitação. |
| RNF04 | Desempenho | Listagem de catálogo paginada; busca com índice; sem meta numérica de latência definida neste estágio (projeto de testes, sem carga real a validar). |
| RNF05 | Disponibilidade | Sem SLA formal (projeto de testes, não operação real); indisponibilidade de uma integração externa (frete, e-mail) não deve travar o restante do fluxo de compra. |
| RNF06 | Escalabilidade | Fora de escopo deste projeto (ver Não-objetivos); o modelo (estoque por variante, DTO/Repository isolados por módulo) não impede evolução futura, mas não há meta de volume a suportar. |
| RNF07 | Usabilidade | Interface responsiva: toda tela (storefront e admin) funciona no navegador do desktop e no navegador de celular (largura de 360 a 480px), ver D20. Navegação e rótulos em português. |
| RNF08 | Acessibilidade | Boas práticas básicas (contraste, navegação por teclado, texto alternativo em imagem); sem certificação formal WCAG neste projeto. |
| RNF09 | Compatibilidade | Navegadores modernos (Chrome, Firefox, Edge, Safari) nas duas últimas versões; sem suporte a navegadores legados. |
| RNF10 | Observabilidade | Log estruturado de erros e das chamadas a integrações externas (gateway de pagamento, agregador de frete, provedor de e-mail); falha de integração externa é distinguível de erro interno no log. |
| RNF11 | Auditoria | Ações administrativas sensíveis (criação, atualização, exclusão, login, mudança de status) geram log com autor, campo alterado, valor anterior e novo, IP e data. |
| RNF12 | Manutenibilidade | Estrutura de código replica o padrão do projeto de referência (SGA) em todos os módulos, para reduzir divergência entre partes escritas em momentos diferentes; diagramas de classes (`.puml`) versionados junto do código. |
| RNF13 | Internacionalização | Só português (pt-BR) e Real (BRL) nesta versão; sem suporte a multi-idioma ou multi-moeda. |
| RNF14 | Ambiente | Projeto de testes/estudo: pagamento em sandbox e nota fiscal simulada, sem dinheiro real nem emissão fiscal válida; e-mail é a única integração que sai para o mundo real (ver D2, D3, D5). |
| RNF15 | Conformidade legal | Política de devolução segue o piso do CDC (arrependimento e vício em durável, ver D8); consentimento e exclusão seguem a LGPD; não há garantia de conformidade fiscal real, dado que a NF-e é simulada. |

## 8. Métricas

| Métrica | Por que importa |
|---|---|
| Taxa de conversão | Saúde geral da loja |
| Abandono de carrinho e recuperação | Retorno direto do fluxo de e-mail |
| Buscas sem resultado | Lacuna de catálogo |
| Recusa de pagamento por método | Problema de gateway ou de UX no checkout |
| Prazo médio de entrega vs. prometido | Confiabilidade da regra de frete |
| Taxa de devolução por motivo | Qualidade de produto ou de descrição |
| Entrega e abertura de e-mail | Saúde do canal de notificação |

## 9. Faseamento

O painel administrativo vem primeiro (D19): a loja só tem o que vender e o que mostrar depois que catálogo, estoque, promoções e cupons podem ser cadastrados pela operação. Marca, categoria, subcategoria e fornecedor já estão entregues. O backlog detalhado fica nas issues do GitHub, uma por tarefa, agrupadas em milestones por fase.

| Fase | Entrega |
|---|---|
| 1 | **Painel administrativo**: login do admin, usuários com papéis e permissões, configuração da loja, catálogo completo (produto, drone, acessório, variante, kit, compatibilidade, mídia), movimento de estoque, promoções, cadastro de cupons, pedidos (listagem, detalhe e mudança de status) e dashboard |
| 2 | **Conta do cliente e vitrine**: perfil e endereços do cliente, sessão com perfil ativo, listagem, filtros e página de produto, lista de desejos |
| 3 | **Compra, pagamento e entrega**: reserva de estoque, carrinho anônimo, aplicação de cupom, checkout, pagamento, cartões salvos, frete, rastreio, notificações transacionais, verificação de e-mail |
| 4 | **Pós-venda**: devolução, estorno, nota fiscal, número de série, atendimento, avaliações e perguntas |
| 5 | **Auditoria, marketing e busca**: log de auditoria, preferências de notificação e marketing, termos buscados, comparação, 2FA, LGPD, login com Google |

## 10. Decisões registradas

| # | Decisão |
|---|---|
| D1 | Login por e-mail e senha, não por CPF/CNPJ. Documento fica no `PerfilCliente`, pedido no cadastro/checkout. Aplicado: o formulário de entrada de `login.html` usa e-mail e senha, e o documento é pedido apenas na aba de criação de conta. |
| D2 | Pagamento via **Mercado Pago em modo sandbox**, permanentemente: projeto não processa dinheiro real. |
| D3 | NF-e **simulada**, sem certificado digital, sem SEFAZ, sem emissor terceirizado. `emitir()` gera identificadores fictícios localmente. |
| D4 | Frete em modelo **híbrido**: cotação em tempo real via agregador de transportadoras para expressa/padrão; tabela própria (`RegraFrete`) só para retirada no showroom e como fallback se a integração cair. |
| D5 | E-mail com **envio real** via **Resend** (plano gratuito), diferente de pagamento e NF-e, que ficam simulados. |
| D6 | **Login social com Google**, além de e-mail/senha local. Conta única (`Usuario`) pode ter várias `ContaSocial` vinculadas. |
| D7 | **Kits e combos** modelados como `VarianteProduto` sem estoque próprio, composta por `ComponenteKit` (outras variantes + quantidade). Estoque e baixa acontecem sempre no componente. |
| D8 | **Política de devolução**: 7 dias corridos de arrependimento (piso do CDC), frete de volta por conta da Aeria; 90 dias para vício em produto durável, com escolha entre troca/reparo/reembolso; lacre rompido não invalida o arrependimento (produto se testa ligando), desde que completo e sem dano; 5 dias úteis de prazo de análise após o recebimento; reembolso no mesmo método de pagamento usado na compra. Parâmetros em `ConfiguracaoLoja`. |
| D9 | **Cupom vinculado ao pedido**: `Carrinho` guarda o cupom aplicado; `UsoCupom` aponta para `Pedido`. Uso só conta quando vira pedido, e cancelamento libera o limite. Desconto do pedido discriminado por origem em `DescontoPedido`. |
| D10 | **Unidade serializada por item**: relação invertida para `UnidadeIndividual → ItemPedido` com cardinalidade `0..*`, permitindo N séries numa linha com quantidade N. |
| D11 | **Origem de registro**: `MovimentoEstoque`, `HistoricoStatusPedido` e `HistoricoDevolucao` ganham `origem: OrigemRegistro` (`MANUAL`, `SISTEMA`, `INTEGRACAO`) e `autor` passa a `0..1`, preenchido só quando manual. Evita usuário-fantasma na base e distingue ação humana de job e de webhook. |
| D12 | **Endereço congelado**: `EnderecoPedido` como fotografia no pedido, com referência opcional ao `Endereco` de origem apenas para rastreio de procedência. |
| D13 | **Variantes na própria tela de cadastro**: tabela editável embutida no formulário do produto, com checkbox "produto sem opções" que colapsa para uma linha. Preço, SKU, custo, estoque e embalagem deixam de ser campos do produto. |
| D14 | **Specs técnicas como colunas fixas** e tipadas em `Drone`, não atributo dinâmico. Ganha filtro por faixa, validação e comparação diretos; perde a capacidade de o admin criar spec nova sem deploy. Aceito por ser projeto de estudo. |
| D15 | **Garantia por produto** em meses; `ConfiguracaoLoja.prazoGarantiaDias` vira apenas o valor padrão sugerido no cadastro. |
| D16 | **Peso e dimensão desdobrados**: `Drone.pesoGramas` e `dimensoesDobrado`/`dimensoesAberto` são spec de exibição e filtro; `VarianteProduto.pesoEmbalagemKg` e as três dimensões da caixa são o que cota frete. No kit, derivados da soma dos componentes com sobrescrita opcional. |
| D17 | **Compatibilidade como relação N:N** entre `Acessorio` e `Produto`, substituindo o campo texto `Acessorio.compatibilidade`. Como `Acessorio` herda de `Produto`, o mesmo vínculo cobre acessório-drone (bateria e modelo) e acessório-acessório (carregador e bateria). O vínculo é recíproco: aparece nos dois cadastros e habilita filtro real, em vez de depender de nome digitado sem erro. Fecha a pendência P1. |
| D18 | **Contato do fornecedor em três colunas**: `Fornecedor.contato` vira `nomeContato`, `emailContato` e `telefoneContato`. Permite busca por e-mail ou telefone e validação de formato, que a string única não permitia. |
| D19 | **Painel administrativo primeiro.** A fase 1 entrega todas as telas do admin integradas à API antes do storefront. Pedidos entram no admin já na fase 1, alimentados por carga de dados, e passam a ser criados pelo checkout na fase 3. |
| D20 | **Web responsiva, sem app nativo.** Toda tela é entregue para o navegador do desktop e para o navegador do celular (360 a 480px). O desenho continua desktop-first, mas a tela só é dada como pronta depois de conferida nas duas larguras; não há aplicativo nativo. |
| D21 | **Rascunho aceita campos vazios.** Marca e categoria em `RASCUNHO` salvam sem nenhum campo preenchido; os obrigatórios só valem para `ATIVA`/`INATIVA`. A API trata texto em branco como não informado. |
| D22 | **Registro inexistente responde 404** (formato Problem) em consulta, alteração e exclusão, como no projeto de referência SGA. |
| D23 | **Busca por nome não diferencia maiúsculas** em marca, categoria e fornecedor. Ignorar acentos fica para a busca do catálogo (#43); o filtro de produto já ignora. |
| D24 | **O que está em uso não se exclui, se inativa.** Excluir marca, subcategoria ou fornecedor com produtos, ou categoria com subcategorias, responde 409 com a contagem e a orientação de inativar. Não há exclusão em cascata. |

## 11. Pendências em aberto

| # | Pendência |
|---|---|
| P2 | Margem (%) aparece na tela como campo calculado a partir de custo e preço. Não é coluna, é derivação, mas ainda não está especificado se é margem bruta sobre custo ou sobre preço. |
| P3 | O painel administrativo foi revisado e completado (20 telas, todo CRUD com listagem e formulário). As 16 telas do storefront ainda não foram revisadas contra o modelo v1.3. |
