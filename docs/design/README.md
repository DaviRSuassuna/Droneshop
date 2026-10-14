# Design e protótipo

Referência visual da Aeria. O protótipo é a fonte da verdade das telas: quem implementa uma tela segue a versão publicada da `develop`.

| Arquivo | Conteúdo |
|---|---|
| [`prototipo/`](prototipo/) | Protótipo navegável em HTML puro: 16 telas do storefront e 20 do admin |
| [`DESIGN.md`](DESIGN.md) | Tokens, tipografia, layout e componentes. Os tokens são os mesmos do `frontend/src/styles.scss` |
| [`PRODUCT.md`](PRODUCT.md) | Usuários, propósito, contexto e princípios do produto |

## Versão publicada

**https://davirsuassuna.github.io/droneshop/prototipo/**

Atualiza sozinha a cada merge na `develop` que altere esta pasta (workflow `.github/workflows/prototipo-pages.yml`).

### Link de cada tela

Use estes links na seção "Protótipo" das issues. Eles sempre abrem a versão mais recente.

**Storefront**

| Tela | Link |
|---|---|
| Home | [`index.html`](https://davirsuassuna.github.io/droneshop/prototipo/index.html) |
| Drones | [`drones.html`](https://davirsuassuna.github.io/droneshop/prototipo/drones.html) |
| Acessórios | [`acessorios.html`](https://davirsuassuna.github.io/droneshop/prototipo/acessorios.html) |
| Ofertas | [`ofertas.html`](https://davirsuassuna.github.io/droneshop/prototipo/ofertas.html) |
| Produto | [`produto.html`](https://davirsuassuna.github.io/droneshop/prototipo/produto.html) |
| Empresas | [`empresas.html`](https://davirsuassuna.github.io/droneshop/prototipo/empresas.html) |
| Suporte | [`suporte.html`](https://davirsuassuna.github.io/droneshop/prototipo/suporte.html) |
| Login e cadastro | [`login.html`](https://davirsuassuna.github.io/droneshop/prototipo/login.html) |
| Carrinho | [`carrinho.html`](https://davirsuassuna.github.io/droneshop/prototipo/carrinho.html) |
| Checkout | [`checkout.html`](https://davirsuassuna.github.io/droneshop/prototipo/checkout.html) |
| Pedido confirmado | [`confirmacao.html`](https://davirsuassuna.github.io/droneshop/prototipo/confirmacao.html) |
| Conta: perfil | [`conta-perfil.html`](https://davirsuassuna.github.io/droneshop/prototipo/conta-perfil.html) |
| Conta: endereços | [`conta-enderecos.html`](https://davirsuassuna.github.io/droneshop/prototipo/conta-enderecos.html) |
| Conta: pagamentos | [`conta-pagamentos.html`](https://davirsuassuna.github.io/droneshop/prototipo/conta-pagamentos.html) |
| Conta: pedidos | [`conta-pedidos.html`](https://davirsuassuna.github.io/droneshop/prototipo/conta-pedidos.html) |
| Conta: lista de desejos | [`conta-favoritos.html`](https://davirsuassuna.github.io/droneshop/prototipo/conta-favoritos.html) |

**Admin**

| Tela | Link |
|---|---|
| Dashboard | [`admin/index.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/index.html) |
| Produtos | [`admin/produtos.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/produtos.html) |
| Cadastrar drone | [`admin/produto-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/produto-novo.html) |
| Cadastrar acessório | [`admin/acessorio-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/acessorio-novo.html) |
| Marcas | [`admin/marcas.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/marcas.html) |
| Cadastrar marca | [`admin/marca-nova.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/marca-nova.html) |
| Categorias | [`admin/categorias.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/categorias.html) |
| Cadastrar categoria | [`admin/categoria-nova.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/categoria-nova.html) |
| Fornecedores | [`admin/fornecedores.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/fornecedores.html) |
| Cadastrar fornecedor | [`admin/fornecedor-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/fornecedor-novo.html) |
| Estoque | [`admin/estoque.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/estoque.html) |
| Movimento de estoque | [`admin/estoque-movimento.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/estoque-movimento.html) |
| Promoções | [`admin/promocoes.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/promocoes.html) |
| Criar promoção | [`admin/promocao-nova.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/promocao-nova.html) |
| Cupons | [`admin/cupons.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/cupons.html) |
| Criar cupom | [`admin/cupom-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/cupom-novo.html) |
| Pedidos | [`admin/pedidos.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/pedidos.html) |
| Detalhe do pedido | [`admin/pedido.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/pedido.html) |
| Usuários | [`admin/usuarios.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/usuarios.html) |
| Novo usuário | [`admin/usuario-novo.html`](https://davirsuassuna.github.io/droneshop/prototipo/admin/usuario-novo.html) |

### O que ainda não está no protótipo

| Falta | Situação |
|---|---|
| Listagem e cadastro de subcategoria | já implementados no admin; a tela segue o padrão de Categorias |
| Versões de celular (360 a 480px) | exigidas pela D20; até existirem, a tela empilha as colunas com os mesmos tokens |

## Como alterar o protótipo

1. Crie a branch pela issue e edite os arquivos desta pasta.
2. Teste localmente (abaixo), nos dois temas.
3. Se mudar cor, fonte, espaçamento ou componente, atualize também o `DESIGN.md` e o `frontend/src/styles.scss`.
4. Abra o PR para a `develop`. Depois do merge, a versão publicada é atualizada.

Uma tela que já está em implementação segue a versão da `develop` do momento em que a issue foi pega. Mudança de design depois disso vira issue nova.

## Como testar localmente

Abra `prototipo/index.html` (storefront) ou `prototipo/admin/index.html` (admin) no navegador. Só a fonte Sora vem da internet; sem conexão, cai para Helvetica.

## Cuidados

- **O site publicado é público**, mesmo com o repositório privado. Use só dados fictícios: nada de nome, e-mail, telefone ou documento real.
- `docs/telas/*.zip` guarda as versões anteriores do wireframe só como histórico. A pasta `prototipo/` é a que vale.

## Configuração do GitHub Pages (uma vez, pelo admin do repositório)

1. **Settings → Pages → Build and deployment → Source:** GitHub Actions.
2. **Settings → Environments → github-pages → Deployment branches and tags:** adicionar a regra `develop` (por padrão o ambiente só aceita a branch padrão).
3. Em **Actions → prototipo → Run workflow**, rodar uma vez na `develop` para a primeira publicação.
