---
numero: 87
titulo: "[catalogo] Imagem principal do produto nas listagens e fotos na carga de dados"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:catalogo", "trilha:b"]
fase: 1
---

### Módulo

catalogo (com reflexo em estoque, grupo, promoção, cupom, pedido e dashboard)

### Classes do diagrama

Nenhuma nova: a imagem vem de `MidiaProduto` (#12) com `principal = true`

### Diagrama de referência

`docs/diagramas/aeria-catalogo.puml`

### Requisitos, regras e decisões (PRD)

RF08, RNF08, D38, D40

### O que entregar

O protótipo novo mostra a foto do produto na miniatura (`.thumb`) de toda tela que lista produto. O Angular desenha sempre o quadrado vazio (`thumb ph`), porque a API não manda a foto e a carga de dados não tem nenhuma:

1. **API.** Todo produto devolvido nas listagens e nos resumos ganha `imagemPrincipal`: o `url` da mídia IMAGEM marcada como principal (#12), ou `null` sem imagem. Vale para `GET /produtos` e `GET /produtos/{id}`, os itens de `GET /estoque`, as versões de `GET /grupos-produto/{id}`, os produtos de promoção e cupom, os itens de `GET /pedidos/{id}` e o `estoqueBaixo` de `GET /admin/dashboard`.
2. **Carga de dados.** As fotos do protótipo (`docs/design/prototipo/assets/img/`) entram como mídia principal dos produtos de exemplo: `falcon.webp` no Falcon X2 Pro, Standard e Pro Fly More; `nimbus.webp` no Nimbus Mini 3 e no Combo; `heron.webp` no Heron Survey Pro. Os arquivos ficam em `backend/src/main/resources/midias-carga/` e são copiados para `aeria.midias.diretorio` na subida da aplicação, só quando ainda não estão lá. Produto sem foto continua com o quadrado vazio, como no protótipo.
3. **Telas.** As 14 miniaturas do admin mostram a foto quando há `imagemPrincipal`, com `alt=""` (o nome do produto já está ao lado), e o quadrado vazio quando não há: listagem de produtos, cadastro de produto (componentes do kit, compatíveis e prévia), compatíveis do acessório, grupos (listagem e cadastro), estoque, promoção, cupom, dashboard e detalhe do pedido.

O formato WebP passa a ser aceito no upload de imagem da #12, junto com PNG e JPG.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas do admin exigem token de administrador com permissão no módulo (#4, #5); rotas `/conta` exigem cliente logado.

```
GET /produtos, GET /produtos/{id}
-> cada produto: { ..., "imagemPrincipal": "/midias/arquivos/<nome>.webp" | null }

GET /estoque                    -> items[].produto.imagemPrincipal
GET /grupos-produto/{id}        -> versoes[].imagemPrincipal
GET /promocoes/{id}, /cupons/{id} -> produtos[].imagemPrincipal
GET /pedidos/{id}               -> itens[].produto.imagemPrincipal   (imagem atual, não congelada)
GET /admin/dashboard            -> estoqueBaixo[].produto.imagemPrincipal

   imagemPrincipal = url da MidiaProduto IMAGEM com principal = true; sem ela -> null
   trocar a principal (PATCH /midias/{id}) ou apagar a mídia (DELETE /midias/{id}) muda o campo na hora

POST /produtos/{id}/midias  idTipo=1 aceita também arquivo image/webp (#12)

GET /midias/arquivos/{nome} (público) serve as fotos da carga de dados
```

### Protótipo

[`admin/produtos.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/produtos.html), admin/estoque.html, admin/grupos.html, admin/grupo-novo.html, admin/kit-novo.html, admin/promocao-nova.html, admin/cupom-novo.html, admin/pedido.html, admin/index.html

### Backend (padrão SGA)

- [ ] `imagemPrincipal` nos DTOs `record` de produto e nos resumos usados por estoque, grupo, promoção, cupom, pedido e dashboard
- [ ] Imagem principal buscada junto da listagem (uma consulta por página, sem N+1)
- [ ] Cópia das fotos da carga na subida, sem sobrescrever arquivo que já existe
- [ ] Carga de dados (`import.sql`) com as mídias principais dos produtos de exemplo
- [ ] WebP aceito no upload de imagem
- [ ] Testes `@Tag("issue-imagem-produto")` passando e `issue-imagem-produto` acrescentada em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] `imagemPrincipal` nos models de `core/models` que trazem produto
- [ ] As 14 miniaturas com a foto e o quadrado vazio como reserva
- [ ] Imagem com tamanho fixo na miniatura, sem mexer no layout (D20, sem rolagem horizontal)
- [ ] Specs dos services afetados com o campo novo
- [ ] Conformidade das telas entregues continua passando

### Critérios de aceite

- [ ] Produto com imagem principal mostra a foto na listagem de produtos; sem imagem, o quadrado vazio
- [ ] Trocar ou apagar a principal reflete na listagem
- [ ] Banco novo sobe com as fotos dos produtos de exemplo, sem upload manual
- [ ] Nenhuma tela faz uma requisição de mídia por linha da tabela

### Testes

Backend: `catalogo/ImagemProdutoResourceTest` em `backend/src/test/java/br/com/droneshop/`, escrito a partir deste contrato, antes do código. Rodar: `./mvnw test -Dgroups=issue-imagem-produto`.

Frontend: specs dos services e conformidade das telas citadas.

### Depende de

#12, #80

### Trilha

**B**, posição 3 de 20. Anterior: admin no protótipo 02 (`nova-admin-prototipo-02.md`). Próxima: armazenamento S3 (`nova-armazenamento-s3.md`). Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
