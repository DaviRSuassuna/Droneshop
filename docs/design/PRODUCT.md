# Produto

Resumo de produto para quem desenha ou implementa tela. O detalhe está no [PRD](../PRD-Aeria.md).

## Plataforma

Aplicação web: storefront (loja) e painel administrativo, no navegador do desktop e do celular (D20). Sem aplicativo nativo.

## Usuários

| Perfil | O que faz |
|---|---|
| Cliente | pesquisa, compara ficha técnica, compra, acompanha pedido, pede devolução e suporte |
| Operação (admin) | cadastra catálogo, controla estoque, processa pedidos, trata devoluções e atendimento, configura a loja |

Uma mesma conta pode ter perfil de cliente e administrativo; escolhe qual usar no login.

## Propósito

Loja de drones e acessórios. Drone é bem de alto valor, com ficha técnica extensa, garantia relevante e, em parte dos modelos, registro na ANAC: a loja precisa ajudar a comparar e dar segurança na compra.

## Contexto

Projeto de estudo, não operação comercial real. Pagamento em sandbox (Mercado Pago) e NF-e simulada; e-mail é envio real. Por isso o protótipo e o site publicado usam só dados fictícios.

## Princípios

1. **Painel administrativo primeiro** (D19): a loja só tem o que vender depois que a operação consegue cadastrar.
2. **Ficha técnica é conteúdo principal**, não rodapé: specs tipadas, filtráveis e comparáveis.
3. **Nada some sem querer**: o que está em uso se inativa, não se exclui (D24); rascunho salva incompleto (D21).
4. **Desenho desktop-first, entrega nas duas larguras** (D20).

## Marca

Wordmark em texto, "AERIA", em Sora com espaçamento largo. Não há logotipo em imagem.

## Acessibilidade

Contraste adequado no tema escuro, navegação por teclado, texto alternativo em imagem e status nunca indicado só por cor (RNF08).
