# Como contribuir com a Aeria

## Fluxo

```
issue  ->  branch a partir da develop  ->  PR para develop  ->  check verde  ->  merge
                                                         develop  ->  PR para main  ->  check verde  ->  merge
```

1. **Pegue uma issue** da fase atual (aba Issues > Milestones) e se atribua nela. Ajustes pequenos (CI, docs, configuração) podem ir sem issue.
2. **Crie a branch a partir da `develop`**, com o número da issue quando houver:
   ```bash
   git switch develop && git pull
   git switch -c feat/12-variante-produto
   ```
   Sem issue: `chore/ajusta-gitignore`.
   Tipos aceitos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`.
   Atalho: na issue, "Development > Create a branch" (ajuste o nome para o padrão acima, branch de origem `develop`).
3. **Commits** em Conventional Commits, em português, com escopo:
   `feat(catalogo): cria VarianteProduto com dados de embalagem`
4. **Abra o PR para a `develop`** com `Closes #12` na descrição, se houver issue. Ela fecha sozinha no merge.
5. **Revisão:** não há aprovação obrigatória. O CODEOWNERS só pede a revisão do Davi automaticamente; o merge fica liberado quando os checks passam.
6. **Release:** quando a fase estiver estável, PR de `develop` para `main`. É o único caminho aceito para a `main`.

Nunca faça commit direto em `main` nem em `develop`: o push é recusado.

## O que é verificado automaticamente

| Verificação | Onde |
|---|---|
| PR para `main` só sai da `develop` | workflow `origem-da-branch` |
| Branch do PR para `develop` segue `tipo/<resumo>` ou `tipo/<numero>-<resumo>` | workflow `origem-da-branch` |
| PR para `develop` sem `Closes #<numero>` gera só um aviso, não bloqueia | workflow `origem-da-branch` |
| Merge só por PR (push direto bloqueado), sem force push, sem apagar a branch | rulesets da `main` e da `develop` |

## Configuração do repositório

Já aplicado: rulesets da `main` e da `develop` (push direto, force push e exclusão bloqueados; merge só por PR, sem aprovação obrigatória), labels, milestones por fase e issues #3 a #48.

Falta, depois que estes arquivos chegarem na `develop`:

1. Adicionar o check `origem-da-branch` como obrigatório nos dois rulesets (Settings > Rules > Rulesets > editar > Require status checks to pass).
2. Settings > General > Pull Requests: marcar "Automatically delete head branches".

`.github/rulesets/*.json` guarda a configuração completa, para reimportar se precisar.

### Por que não há aprovação obrigatória

O GitHub não deixa o autor aprovar o próprio PR, e quase todos os PRs saem da conta do Davi. Com aprovação obrigatória, todo merge dependeria do bypass de admin, que também ignora os checks. Sem ela, o PR entra quando os checks passam. Se o time crescer, basta voltar `required_approving_review_count` para 1 e `require_code_owner_review` para `true` nos dois rulesets.

### Plano do GitHub

O repositório é privado e os rulesets estão sendo aplicados (conta com GitHub Pro). Se a conta voltar para o plano Free, eles deixam de bloquear.
