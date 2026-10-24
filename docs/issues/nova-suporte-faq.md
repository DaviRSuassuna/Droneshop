---
numero: null
titulo: "[atendimento] Suporte: temas e perguntas frequentes com texto fixo"
labels: ["tipo:feature", "camada:frontend", "modulo:atendimento"]
fase: 4
---

### Módulo

atendimento (só frontend)

### Classes do diagrama

Nenhuma

### Diagrama de referência

Nenhum: não muda o modelo

### Requisitos, regras e decisões (PRD)

D8, D38

### O que entregar

A página de suporte do protótipo novo (`suporte.html`) tem, além de "Abrir chamado" (#38), os blocos "Encontre pelo assunto" (temas: Pedidos e entrega, Trocas e devoluções, Garantia e reparo, Regras ANAC e SISANT, Baterias e transporte, Firmware e apps) e "Perguntas frequentes — Respostas rápidas".

O conteúdo é texto fixo no Angular, sem API e sem cadastro no admin (decisão do mantenedor). Os prazos que já existem na configuração da loja vêm de `GET /loja/configuracao` (#21), para o texto não contradizer a regra: arrependimento (`prazoArrependimentoDias`) e frete reverso (`freteReversoPorContaLoja`). O resto do texto segue o protótipo.

"Ver artigos" de cada tema abre as perguntas daquele tema na própria página, sem página de artigo separada.

### Contrato de API

Nenhuma rota nova. Usa `GET /loja/configuracao` (#21).

### Protótipo

[`suporte.html`](https://davirsuassuna.github.io/droneshop/prototipo/suporte.html)

### Frontend

- [ ] Temas e perguntas frequentes em `/suporte`, com o texto num arquivo de conteúdo do Angular (não espalhado no HTML)
- [ ] Prazo de arrependimento e frete reverso lidos de `/loja/configuracao`
- [ ] Perguntas abrem e fecham pelo teclado (RNF08)
- [ ] Conformidade de `suporte.html` passando (`npm run e2e -- --grep @issue-38`), no desktop e no celular (D20)

### Critérios de aceite

- [ ] Os seis temas do protótipo, cada um com suas perguntas
- [ ] Prazo de arrependimento na resposta igual ao da configuração da loja

### Testes

Sem teste de backend: não há rota nova. A estrutura da página (temas, "Respostas rápidas") é conferida pela conformidade de `suporte.html`, que já tem a etiqueta `issue-38` (uma etiqueta por tela). Rodar: `npm run e2e -- --grep @issue-38`.

### Depende de

#38, #21
