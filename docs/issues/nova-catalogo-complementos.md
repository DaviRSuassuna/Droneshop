---
numero: 84
titulo: "[catalogo] Logo da marca por upload, peso do acessório, sugestão nos compatíveis e kit em Ofertas"
labels: ["tipo:feature", "camada:backend", "camada:frontend", "modulo:catalogo", "trilha:b"]
fase: 1
---

### Módulo

catalogo

### Classes do diagrama

Marca, Acessorio, Kit

### Diagrama de referência

`docs/diagramas/aeria-catalogo.puml`

### Requisitos, regras e decisões (PRD)

RF01, RF04, RF08, D7, D21, D40

### O que entregar

Campos que as telas de cadastro do protótipo novo mostram e o modelo não tem:

1. **Logo da marca por arquivo** (`admin/marca-nova.html`: "Arraste um arquivo ou clique · SVG ou PNG"). Upload que guarda o arquivo como as mídias da #12 e grava o endereço em `logoUrl`. Informar `logoUrl` direto continua valendo.
2. **Peso do item** (`admin/acessorio-novo.html`): `Acessorio.pesoItemG`, em gramas, opcional. É o peso do acessório em si, diferente do peso da embalagem (RN18).
3. **Sugerir na página dos drones compatíveis** (`admin/acessorio-novo.html`): `Acessorio.sugerirNosCompativeis`, padrão `false`. Na página do drone (`GET /loja/produtos/{slug}/compativeis`, #21), entram só os acessórios ATIVO com a opção ligada.
4. **Exibir na página Ofertas** (`admin/kit-novo.html`): `Kit.exibirEmOfertas`, padrão `false`. Kit ATIVO com a opção ligada aparece em `GET /loja/ofertas` com `precoDe` = soma do preço dos componentes x quantidade e `precoPor` = preço do kit, sem promoção.

### Contrato de API

Convenções gerais (paginação, `idXxx`, status `{id, nome}`, erros Problem) em [`docs/API.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/API.md). Rotas do admin exigem token de administrador com permissão no módulo (#4, #5); rotas `/conta` exigem cliente logado.

```
POST /marcas/{id}/logo   multipart/form-data   arquivo=<png|svg>
-> 200 { "logoUrl": "/midias/arquivos/<nome>.png" }   (GET /marcas/{id} passa a trazer esse logoUrl)
-> 400 errors[].field = "arquivo"   não é PNG nem SVG, ou acima de 2 MB
-> 404 marca inexistente
   o arquivo antigo enviado por upload é apagado; logoUrl externa só é substituída

POST/PUT /acessorios  { ..., "pesoItemG": 85, "sugerirNosCompativeis": true }
-> resposta do acessório traz os dois campos
-> 400 errors[].field = "pesoItemG"   negativo
   RASCUNHO aceita vazio (D28); sem sugerirNosCompativeis, vale false

POST/PUT /kits  { ..., "exibirEmOfertas": true }
-> resposta do kit traz "exibirEmOfertas"

GET /loja/produtos/{slug}/compativeis   (#21) só acessórios ATIVO com sugerirNosCompativeis = true
GET /loja/ofertas                       (#21) ganha os kits ATIVO com exibirEmOfertas = true:
   { "produto": {...}, "precoDe": <soma dos componentes>, "precoPor": <preço do kit>, "promocao": null }
```

### Protótipo

[`admin/marca-nova.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/marca-nova.html), [`admin/acessorio-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/acessorio-novo.html), [`admin/kit-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/kit-novo.html)

### Backend (padrão SGA)

- [ ] Campos novos em `Acessorio` e `Kit`, com DTOs `record` e Bean Validation (mensagens com `(back)`)
- [ ] Upload do logo no mesmo armazenamento das mídias (#12)
- [ ] Service com `@Transactional` nas escritas
- [ ] Erros no formato Problem (400, 404)
- [ ] Carga de dados (`import.sql`) com peso e sugestão nos acessórios e o kit Fly More em Ofertas
- [ ] Diagrama `aeria-catalogo.puml` atualizado
- [ ] Testes `@Tag("issue-catalogo-complementos")` passando e `issue-catalogo-complementos` acrescentada em `backend/src/test/issues-entregues.txt`

### Frontend

- [ ] Upload do logo no cadastro de marca, peso e "Sugerir na página dos drones compatíveis" no acessório, "Exibir na página Ofertas" no kit
- [ ] Em `frontend/e2e/conformidade/telas.ts`, tirar do `ignorar` "Logo da marca" (`admin/marca-nova.html`), "Peso do item" e "Sugerir na página dos drones compatíveis" (`admin/acessorio-novo.html`) e "Exibir na página Ofertas" (`admin/kit-novo.html`)
- [ ] Conformidade das três telas passando (`npm run e2e -- --grep "@base|@issue-8|@issue-80"`), no desktop e no celular (D20)
- [ ] Specs de `marca.service`, `acessorio.service` e `kit.service`

### Critérios de aceite

- [ ] Logo aceita só PNG e SVG até 2 MB
- [ ] Peso do item não aceita valor negativo
- [ ] Página do drone sugere só acessórios com a opção ligada
- [ ] Kit em Ofertas mostra o preço dos componentes riscado e o preço do kit

### Testes

Backend: `catalogo/CatalogoComplementosResourceTest` em `backend/src/test/java/br/com/droneshop/`, escrito a partir deste contrato, antes do código. Os dois testes da loja dependem também da #21. Rodar: `./mvnw test -Dgroups=issue-catalogo-complementos`.

Frontend: conformidade das telas de marca, acessório e kit.

### Depende de

#80, #12, #11

### Trilha

**B**, posição 5 de 20. Anterior: armazenamento S3 (`nova-armazenamento-s3.md`). Próxima: #83. Guia: [`docs/agentes/README.md`](https://github.com/DaviRSuassuna/droneshop/blob/develop/docs/agentes/README.md).
