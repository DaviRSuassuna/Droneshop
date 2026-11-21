---
numero: null
titulo: "[identidade] Autenticação e autorização pelo Keycloak, intermediadas pelo Quarkus"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:identidade", "trilha:a"]
fase: 1
---

### Módulo

identidade (toca em `@RolesAllowed` de todos os módulos e nos guards do front: precisa de aval do supervisor nas duas trilhas)

### Requisitos, regras e decisões (PRD)

D56. Substitui o login próprio da #20, o 2FA da #45, a #47 (sai) e a #33.

### O que entregar

Atividade da disciplina: autenticação e autorização com Keycloak, e o backend Quarkus como único ponto de contato com ele.

| Requisito da atividade | Onde fica |
|---|---|
| Configuração do Keycloak | serviço `keycloak` no `compose.yaml` e realm exportado em `backend/keycloak/aeria-realm.json` (realm `aeria`, client confidencial `aeria-api`, papéis `CLIENTE`, `ADMINISTRATIVO`, `ADMIN`, OTP opcional ligado para os papéis administrativos), importado na subida. Usuários de exemplo da carga criados no realm, sem senha no banco |
| Login pelo Angular via backend | `POST /auth/login` {email, senha, codigoOtp?}: a API pede o token ao Keycloak e devolve {accessToken, expiraEm, papeis}. O refresh token vai em cookie `HttpOnly; Secure; SameSite=Strict`. Com OTP exigido e sem código: 401 com `codigo: "otp-necessario"` |
| Cadastro | `POST /auth/cadastro` cria o usuário pela Admin API com o papel `CLIENTE`, cria o `PerfilCliente` com o `sub` e dispara o e-mail de verificação (`execute-actions-email`) |
| Estado da autenticação | `GET /auth/eu` devolve id, nome, e-mail e papéis. `POST /auth/renovar` usa o cookie. `POST /auth/recuperar-senha` dispara `UPDATE_PASSWORD` pelo Keycloak |
| Papéis | `@RolesAllowed` nos resources, lidos do token (`quarkus-oidc`, resource server). `ADMIN` herda `ADMINISTRATIVO` |
| Proteção dos endpoints | sem token, 401; papel errado, 403; os dois em Problem (`exception/`) |
| Guards no Angular | `authGuard` (logado) e `papelGuard(papeis)` nas rotas de `/conta`, `/checkout` e `/admin`. Interceptor anexa `Authorization`, renova uma vez quando recebe 401 e, se falhar, leva para `/entrar?volta=...`. 403 mostra a tela de acesso negado |
| Logout | `POST /auth/sair`: a API revoga o refresh token e encerra a sessão no Keycloak (logout do backchannel), apaga o cookie e o front limpa o estado |

### Fora do escopo

Login com Google. Nenhuma chamada do Angular ao Keycloak. Nenhuma credencial administrativa do Keycloak fora do backend: o segredo do client vem por variável de ambiente, com padrão só de dev.

### Critérios de aceite

- [ ] Nenhuma coluna de senha ou hash no banco; as tabelas de sessão e token próprios são removidas
- [ ] Login, cadastro, eu, renovar, recuperar-senha e sair testados com o Keycloak de teste (Dev Services do `quarkus-oidc` ou `KeycloakFalso` em `%test`), sem depender da internet
- [ ] 401 sem token e 403 com papel errado, testados em um endpoint de cada módulo
- [ ] Admin com OTP: login sem código devolve `otp-necessario`; com código válido, entra
- [ ] Specs do interceptor (anexa token, renova uma vez, redireciona) e dos guards
- [ ] Conformidade de `/entrar` (`issue-4`) e da conta (`issue-19`) continua passando
- [ ] `aeria-identidade.puml` e `API.md` atualizados
