# Segurança

## Como reportar uma vulnerabilidade

Este é um projeto de estudo, sem operação comercial nem dados reais de clientes. Mesmo assim, se você encontrar uma vulnerabilidade, **não abra uma issue pública**: use o [reporte privado de vulnerabilidades do GitHub](../../security/advisories/new) deste repositório. Descreva o problema, como reproduzir e o impacto esperado.

## Diretrizes de revisão de segurança

Checklist usado na revisão de pull requests (por pessoas e por agentes, ver `AGENTS.md`). Qualquer item violado é **bloqueante**.

### Segredos e configuração

- Nenhum segredo no repositório: chave JWT, senha de banco de produção, token de API (Mercado Pago, Resend, Google, agregador de frete) ou `GH_TOKEN`. Em produção, tudo vem de variável de ambiente (`JWT_PUBLIC_KEY`, `JWT_PRIVATE_KEY`, `QUARKUS_DATASOURCE_*`).
- Valores fixos em `application.properties` só nos perfis `%dev` e `%test`.
- Chaves de desenvolvimento ficam em `target/` e não são versionadas.
- CORS: as origens permitidas não são abertas para `*`.

### Autenticação e autorização (backend)

- Toda rota do admin tem `@RolesAllowed("ADMINISTRATIVO")` e `@ModuloAdmin(...)` com o módulo certo, para o filtro de permissão por módulo (#5) valer. Resource novo sem essas anotações é bloqueante.
- Rotas `/conta/**` exigem cliente logado, e o cliente só acessa os próprios dados: o id vem do token, nunca de parâmetro ou corpo da requisição.
- Rotas públicas (`/loja/**`, login, cadastro) não expõem campo interno: custo de aquisição, margem, estoque mínimo, dados de outros clientes ou de usuários do admin.
- Autor de movimento, histórico ou auditoria vem do usuário do token (D11), nunca do corpo.
- Senha só com hash (BCrypt); nunca volta em resposta nem em log.

### Entrada de dados

- Todo DTO de entrada tem Bean Validation com tamanho máximo nos textos.
- Consultas com parâmetros nomeados ou posicionais (`:nome`, `?1`). Concatenar texto vindo do usuário em JPQL ou SQL é bloqueante.
- Upload de mídia (D29): só os tipos aceitos (PNG, JPG e PDF), com limite de tamanho e nome de arquivo gerado pelo servidor. O nome enviado nunca vira caminho no disco (path traversal).
- Ids de outros registros no corpo (`idMarca`, `idProduto`...) são conferidos no banco antes do uso.

### Erros e logs

- Respostas de erro no formato Problem, sem stack trace, SQL ou nome de classe interna (`UncaughtExceptionMapper`).
- Log não registra token, senha, documento (CPF/CNPJ) completo nem dado de cartão.

### Pagamento e dados pessoais

- Cartão só como token do Mercado Pago: número, validade e CVV nunca passam pelo backend nem são gravados.
- Dados pessoais seguem a LGPD (#46): exportação e exclusão a pedido do titular, consentimento registrado com versão.
- Massa de teste, carga de dados e protótipo usam só dados fictícios.

### Frontend

- Nada de `bypassSecurityTrust*` nem `[innerHTML]` com conteúdo vindo da API sem sanitização.
- O token JWT só é enviado pelo `auth.interceptor` para a URL da própria API.
- Nenhuma chave, segredo ou URL de serviço interno no código do Angular nem em `environment*.ts`.

### Dependências e CI

- Dependência nova precisa de motivo no PR; prefira as que o projeto já usa.
- Workflows do GitHub Actions não imprimem segredos e não executam código vindo de PR de fork com permissão de escrita.
