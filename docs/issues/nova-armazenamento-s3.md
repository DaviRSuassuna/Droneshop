---
numero: null
titulo: "[catalogo] Mídias do produto no armazenamento S3 (RustFS): upload, exibição, download e exclusão"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:catalogo", "trilha:b"]
fase: 1
---

### Módulo

catalogo

### Classes do diagrama

MidiaProduto, TipoMidia

### Diagrama de referência

`docs/diagramas/aeria-catalogo.puml` (o modelo não muda: só muda onde o arquivo fica)

### Requisitos, regras e decisões (PRD)

RF08, RNF08, D28, D29, D42

### O que entregar

Atividade da disciplina: upload, armazenamento, recuperação e exclusão de arquivos em Object Storage compatível com S3. Hoje a #12 grava em disco (`aeria.midias.diretorio`); esta issue troca o armazenamento pelo RustFS, mantendo as rotas e o modelo.

| Requisito da atividade | Onde fica |
|---|---|
| 1. Upload pela interface Angular, integrado ao Quarkus | bloco Imagens do cadastro de drone, acessório e kit no admin |
| 2. Armazenamento S3 | RustFS no `compose.yaml`, extensão `quarkus-amazon-s3` |
| 3. Visualização e download | `GET /midias/arquivos/{nome}` exibe; `?download=true` baixa; galeria do admin e catálogo da loja |
| 4. Imagens dos produtos no catálogo | cadastro no admin e foto principal nos cards e na página do produto |
| 5. Exclusão com regras de negócio | regras da D42 (abaixo) |
| 6. Validação de tipo e tamanho | front e back (abaixo) |

Backend:

- Interface `ArmazenamentoArquivos` (`gravar`, `ler`, `remover`) com implementação S3; `MidiaProdutoServiceImpl` deixa de usar `java.nio.file`.
- Configuração por variável de ambiente: `AERIA_S3_ENDPOINT`, `AERIA_S3_BUCKET` (padrão `aeria-midias`; em paralelo, cada trilha usa o próprio bucket, ver `docs/agentes/README.md`), `AERIA_S3_ACCESS_KEY`, `AERIA_S3_SECRET_KEY`, `AERIA_S3_REGION`. Nenhuma chave real no repositório.
- Bucket criado na subida, se não existir.
- `compose.yaml`: serviço `rustfs` (portas 9000 API e 9001 console, volume persistente).
- Testes sem armazenamento externo: implementação em memória no perfil `%test`, ou Dev Services do S3.

Frontend:

- Bloco Imagens como no protótipo: arrastar ou escolher arquivo, prévia, texto alternativo, marcar principal, reordenar, baixar e excluir.
- Validação antes do envio (tipo e tamanho) com a mensagem no próprio bloco; erro 400 do back mostrado no mesmo lugar.
- Catálogo da loja usa a foto principal; produto sem imagem mostra o desenho de linha.

### Regras (D42)

| Situação | Resposta |
|---|---|
| Imagem PNG, JPEG ou WebP até 5 MB | 200 |
| Manual PDF até 20 MB | 200 |
| Extensão, content-type ou assinatura não batem com o tipo | 400 `arquivo` |
| Acima do limite | 400 `arquivo` |
| Excluir a mídia principal | 204; a próxima na ordem vira principal |
| Excluir a última imagem de produto ativo | 409 (inative o produto antes) |
| Excluir o produto | remove todos os objetos dele |
| Produto que não pode ser excluído (D28) | mídias mantidas |
| Armazenamento fora do ar | 503 |

### Contrato de API

Convenções gerais em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas iguais às da #12; muda o seguinte:

```
POST /produtos/{id}/midias   multipart/form-data
   idTipo=1, arquivo=<png|jpg|webp, até 5 MB>, altText="...", principal=true
   idTipo=3, arquivo=<pdf, até 20 MB>
-> 200 { "id": 5, "tipo": {"id":1,"nome":"IMAGEM"}, "url": "/midias/arquivos/<uuid>.webp", ... }
-> 400 errors[].field = "arquivo"   tipo ou tamanho inválido
-> 503                             armazenamento indisponível

GET /midias/arquivos/{nome}                 (público) -> 200 inline, com content-type | 404
GET /midias/arquivos/{nome}?download=true   (público) -> 200 com Content-Disposition: attachment | 404

DELETE /midias/{id} -> 204 | 404
                    -> 409   última imagem de produto ativo
```

### Protótipo

[`admin/produto-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/produto-novo.html), [`admin/acessorio-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/acessorio-novo.html), [`admin/kit-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/kit-novo.html) (bloco Imagens); [`produto.html`](https://davirsuassuna.github.io/droneshop/prototipo/produto.html) e [`drones.html`](https://davirsuassuna.github.io/droneshop/prototipo/drones.html) (exibição)

### Backend (padrão SGA)

- [ ] `ArmazenamentoArquivos` + implementação S3, sem `java.nio.file` no service
- [ ] Configuração por variável de ambiente e bucket criado na subida
- [ ] RustFS no `compose.yaml` e passo a passo no `backend/README.md`
- [ ] Validação de tipo, tamanho e assinatura, com os limites da D42 (WebP já vem da #87)
- [ ] Regras de exclusão da D42
- [ ] Erros no formato Problem (400, 404, 409, 503)
- [ ] Fotos da carga de dados (#87) enviadas ao bucket na subida, no lugar do disco
- [ ] Testes `@Tag` desta issue passando

### Frontend

- [ ] Bloco Imagens do cadastro integrado à API
- [ ] Validação de tipo e tamanho antes do envio
- [ ] Download e exclusão na galeria do admin
- [ ] Foto principal no catálogo da loja
- [ ] Segue o protótipo e o `DESIGN.md`, desktop e celular (360 a 480px, D20)
- [ ] Specs (`*.spec.ts`) dos services alterados

### Critérios de aceite

- [ ] Arquivo enviado pelo admin aparece no bucket do RustFS (console em `localhost:9001`)
- [ ] Imagem aparece no cadastro e no catálogo; download baixa o arquivo original
- [ ] Tipo ou tamanho inválido barrado no front e no back
- [ ] Exclusão remove o objeto do bucket e segue as regras da D42
- [ ] Nada de chave ou senha real versionada

### Testes

Backend: `catalogo/ArmazenamentoS3Test` e casos novos em `catalogo/MidiaProdutoResourceTest` (WebP, limites, download, regras de exclusão, 503).

### Depende de

#12, #87 (a #87 já aceita WebP e põe as fotos na carga de dados; esta issue só troca onde o arquivo fica, inclusive as fotos da carga)

### Trilha

**B**, posição 4 de 20. Anterior: #87. Próxima: #84. Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
