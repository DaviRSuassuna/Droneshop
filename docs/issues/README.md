# Issues

Corpo de cada issue do backlog, versionado. É aqui que o **contrato de API** de cada issue vive: os testes do backend (`backend/src/test`) são escritos a partir dele.

| Arquivo | Issue |
|---|---|
| `NNN.md` | Issue existente de número NNN |
| `nova-<nome>.md` | Issue criada a partir do arquivo. Antes da primeira sincronização tem `numero: null` e é criada pelo título; depois, o número entra no cabeçalho e **o nome do arquivo fica**, porque os testes dela usam a etiqueta `issue-<nome>` |

Cada arquivo começa com um cabeçalho entre `---`, um valor JSON por linha:

```
---
numero: 9                      # null nas issues novas
titulo: "[catalogo] Produto: ..."
labels: ["tipo:feature", "camada:backend", "modulo:catalogo"]
fase: 1                        # milestone cujo título contém "Fase 1"
---
```

## Trilha

Toda issue aberta tem a etiqueta `trilha:a` ou `trilha:b` e termina com a seção `### Trilha`: posição na ordem, issue anterior e próxima, e o **portão** quando depende de uma issue da outra trilha. Quem define e muda a ordem é o supervisor (`docs/agentes/README.md`); ao mudar, refaça a seção das issues afetadas.

## Como mudar uma issue

1. Edite o arquivo (ou crie um `nova-<nome>.md`) num PR para a `develop`.
2. Depois do merge, em **Actions > sincronizar-issues > Run workflow** na `develop`: primeiro com **simular** marcado, para ver a lista; depois desmarcado, para aplicar.
3. Os workflows `vincular-testes` e `vincular-prototipo` completam a issue com a lista de testes e os links do protótipo (rodam sozinhos no push da `develop`; depois de criar issues novas, rode os dois à mão).

Os blocos escritos por esses dois workflows ficam entre marcadores HTML (`<!-- testes:inicio -->`, `<!-- prototipo:inicio -->`) e não são apagados pela sincronização.

Localmente, com o [GitHub CLI](https://cli.github.com/) logado: `python scripts/sincronizar_issues.py --dry-run`.
