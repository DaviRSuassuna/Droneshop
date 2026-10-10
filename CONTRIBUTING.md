# Como contribuir com a Aeria

## Fluxo

```
issue  ->  branch a partir da develop  ->  PR para develop  ->  (Davi aprova)  ->  merge
                                                         develop  ->  PR para main  ->  (Davi aprova)  ->  merge
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
5. **Revisão:** PR de outra pessoa só entra com a aprovação do Davi (check `aprovacao`). PR aberto pelo Davi passa direto, sem bypass.
6. **Release:** quando a fase estiver estável, PR de `develop` para `main`. É o único caminho aceito para a `main`.

Nunca faça commit direto em `main` nem em `develop`: o push é recusado.

## O que é verificado automaticamente

| Verificação | Onde |
|---|---|
| PR para `main` só sai da `develop` | workflow `origem-da-branch` |
| Branch do PR para `develop` segue `tipo/<resumo>` ou `tipo/<numero>-<resumo>` | workflow `origem-da-branch` |
| PR para `develop` sem `Closes #<numero>` gera só um aviso, não bloqueia | workflow `origem-da-branch` |
| PR de outra pessoa aprovado pelo Davi no último commit | workflow `aprovacao` |
| Merge só por PR (push direto bloqueado), sem force push, sem apagar a branch | rulesets da `main` e da `develop` |

## Configuração do repositório

Já aplicado: rulesets da `main` e da `develop` (push direto, force push e exclusão bloqueados; merge só por PR, sem aprovação obrigatória), labels, milestones por fase e issues #3 a #48.

Falta, depois que estes arquivos chegarem na `develop`:

1. Adicionar o check `origem-da-branch` como obrigatório nos dois rulesets (Settings > Rules > Rulesets > editar > Require status checks to pass).
2. Settings > General > Pull Requests: marcar "Automatically delete head branches".

`.github/rulesets/*.json` guarda a configuração completa, para reimportar se precisar.

### Por que a aprovação é um check e não a regra de review do GitHub

O GitHub não deixa o autor aprovar o próprio PR. Com a regra nativa de aprovação, todo PR do Davi dependeria do bypass de admin, que também ignora os checks. Por isso os rulesets têm 0 aprovações obrigatórias e exigem o check `aprovacao` (`.github/workflows/aprovacao.yml`): passa direto em PR do Davi e, nos demais, só depois que ele aprova o último commit. Commit novo depois da aprovação exige aprovar de novo.

### Plano do GitHub

O repositório é privado e os rulesets estão sendo aplicados (conta com GitHub Pro). Se a conta voltar para o plano Free, eles deixam de bloquear.
