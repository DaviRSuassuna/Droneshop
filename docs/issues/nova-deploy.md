---
numero: null
titulo: "[infra] Deploy de produção"
labels: ["tipo:chore"]
fase: null
---

## Contexto
Saiu do escopo da #57. Publicar a API e o frontend num servidor, com banco próprio e HTTPS.

## Escopo
- [ ] `frontend/Dockerfile` (build do Angular servido por Nginx)
- [ ] `compose.prod.yaml` com Postgres, API (`Dockerfile.jvm`) e front atrás de proxy reverso (Caddy ou Nginx) com HTTPS
- [ ] `%prod` lendo tudo de variáveis de ambiente (`QUARKUS_DATASOURCE_*`, chaves JWT, Resend, Mercado Pago, agregador de frete); nenhuma senha no `application.properties`
- [ ] `.env.example` com as variáveis, sem valores reais
- [ ] `.github/workflows/deploy.yml`: na `main`, builda as imagens, envia ao servidor por SSH e sobe o compose
- [ ] Rulesets: check de deploy na `main`
- [ ] README com o passo a passo de configuração do servidor

## Davi faz
- Escolher e criar o servidor, apontar o domínio
- Gerar a chave SSH e cadastrar os secrets do repositório (`SSH_HOST`, `SSH_USER`, `SSH_KEY` e as variáveis do `.env`)

## Fora do escopo
- Escala, alta disponibilidade e monitoramento
