# Como contribuir com a Aeria

## Fluxo

```
issue  ->  branch a partir da develop  ->  PR para develop  ->  (Davi aprova)  ->  merge
                                                         develop  ->  PR para main  ->  (Davi aprova)  ->  merge
```

1. **Pegue uma issue** da fase atual (aba Issues > Milestones) e se atribua nela.
2. **Crie a branch a partir da `develop`** com o número da issue:
   ```bash
   git switch develop && git pull
   git switch -c feat/12-variante-produto
   ```
   Tipos aceitos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.
   Atalho: na issue, "Development > Create a branch" (ajuste o nome para o padrão acima, branch de origem `develop`).
3. **Commits** em Conventional Commits, em português, com escopo:
   `feat(catalogo): cria VarianteProduto com dados de embalagem`
4. **Abra o PR para a `develop`** com `Closes #12` na descrição. A issue fecha sozinha no merge.
5. **Revisão:** só o Davi aprova (CODEOWNERS). Sem a aprovação dele o botão de merge fica bloqueado.
6. **Release:** quando a fase estiver estável, PR de `develop` para `main`. É o único caminho aceito para a `main`.

Nunca faça commit direto em `main` nem em `develop`: o push é recusado.

## O que é verificado automaticamente

| Verificação | Onde |
|---|---|
| PR para `main` só sai da `develop` | workflow `origem-da-branch` |
| Branch do PR para `develop` segue `tipo/<numero>-<resumo>` | workflow `origem-da-branch` |
| PR para `develop` tem `Closes #<numero>` | workflow `origem-da-branch` |
| Aprovação do Davi, push direto bloqueado, sem force push, sem apagar a branch | rulesets da `main` e da `develop` |

## Configuração do repositório

Já aplicado: rulesets da `main` e da `develop` (push direto, force push e exclusão bloqueados; PR com 1 aprovação de Code Owner), labels, milestones por fase e issues #3 a #48.

Falta, depois que estes arquivos chegarem na `develop`:

1. Adicionar o check `origem-da-branch` como obrigatório nos dois rulesets (Settings > Rules > Rulesets > editar > Require status checks to pass).
2. Settings > General > Pull Requests: marcar "Automatically delete head branches".

`.github/rulesets/*.json` guarda a configuração completa, para reimportar se precisar.

### Como o Davi faz merge do próprio PR

O GitHub não deixa o autor aprovar o próprio PR. Os rulesets colocam o papel **Admin** do repositório na lista de bypass em modo *pull request*: o Davi continua sem conseguir dar push direto, mas no PR dele aparece a opção "Merge without waiting for requirements to be met (bypass rules)". Colaboradores não têm esse botão.

### Plano do GitHub

O repositório é privado e os rulesets estão sendo aplicados (conta com GitHub Pro). Se a conta voltar para o plano Free, eles deixam de bloquear.
