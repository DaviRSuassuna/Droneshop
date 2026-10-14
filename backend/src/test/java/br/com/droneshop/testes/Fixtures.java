package br.com.droneshop.testes;

import static br.com.droneshop.testes.Api.given;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

/**
 * Massa de dados dos testes das issues, montada só pela API (nunca pelo banco).
 *
 * Os corpos seguem os contratos das issues (docs/issues/NNN.md). Cada método
 * cria registros novos com sufixo aleatório, então os testes não dependem uns
 * dos outros nem do total das tabelas. Ids de enum seguem D31 (ordem do diagrama).
 */
public final class Fixtures {

    // ids de enum (D31)
    public static final long PRODUTO_RASCUNHO = 1, PRODUTO_ATIVO = 2, PRODUTO_INATIVO = 3;
    public static final long TIPO_DRONE = 1, TIPO_ACESSORIO = 2;
    public static final long PAPEL_ADMIN = 1, PAPEL_OPERADOR = 2, PAPEL_CATALOGO = 3, PAPEL_ATENDIMENTO = 4,
            PAPEL_SOMENTE_LEITURA = 5;
    public static final long MOV_ENTRADA = 1, MOV_SAIDA = 2, MOV_AJUSTE = 3, MOV_DEVOLUCAO = 4, MOV_PERDA = 5;
    public static final long PF = 1, PJ = 2;
    public static final long PEDIDO_PENDENTE = 1, PEDIDO_PAGO = 2, PEDIDO_SEPARACAO = 3, PEDIDO_ENVIADO = 4,
            PEDIDO_ENTREGUE = 5, PEDIDO_EM_DEVOLUCAO = 6, PEDIDO_DEVOLVIDO = 7, PEDIDO_CANCELADO = 8;
    public static final long ENTREGA_EXPRESSA = 1, ENTREGA_PADRAO = 2, ENTREGA_RETIRADA = 3;
    public static final long PIX = 1, CARTAO = 2, BOLETO = 3, FATURAMENTO_PJ = 4;
    public static final long MODERACAO_PENDENTE = 1, MODERACAO_APROVADA = 2, MODERACAO_REJEITADA = 3;

    /** CEP de destino que o FreteFalso cota normalmente. */
    public static final String CEP_DESTINO = "77001002";

    private static Long idPalmas;

    private Fixtures() {
    }

    // ---------------------------------------------------------------- catálogo

    public record Arvore(long idMarca, String slugMarca, long idCategoria, String slugCategoria,
            long idSubcategoria, String slugSubcategoria, long idFornecedor) {
    }

    public record ProdutoCriado(long id, String slug, long idVariante, String sku) {
    }

    public static long idPalmas() {
        if (idPalmas == null)
            idPalmas = Dados.idMunicipio("TO", Dados.IBGE_PALMAS);
        return idPalmas;
    }

    /** Marca, categoria > subcategoria e fornecedor ativos, criados pelas rotas já entregues. */
    public static Arvore arvore() {
        String s = Dados.sufixo();
        Map<String, Object> marca = new HashMap<>();
        marca.put("nome", "Marca " + s);
        marca.put("paisOrigem", "Brasil");
        marca.put("logoUrl", "https://cdn.exemplo.com.br/logo.png");
        marca.put("idStatus", Dados.ATIVA);
        marca.put("destacarHome", true);
        long idMarca = criar("/marcas", marca);

        Map<String, Object> categoria = new HashMap<>();
        categoria.put("nome", "Categoria " + s);
        categoria.put("ordemExibicao", 1);
        categoria.put("idStatus", Dados.ATIVA);
        categoria.put("exibirMenu", true);
        long idCategoria = criar("/categorias", categoria);

        Map<String, Object> sub = new HashMap<>();
        sub.put("nome", "Sub " + s);
        sub.put("idCategoria", idCategoria);
        sub.put("ordemExibicao", 1);
        sub.put("idStatus", Dados.ATIVA);
        long idSub = criar("/subcategorias", sub);

        Map<String, Object> endereco = new HashMap<>();
        endereco.put("cep", "77001002");
        endereco.put("logradouro", "Quadra 104 Norte");
        endereco.put("numero", "12");
        endereco.put("bairro", "Plano Diretor Norte");
        endereco.put("idMunicipio", idPalmas());
        Map<String, Object> forn = new HashMap<>();
        forn.put("razaoSocial", "Fornecedor " + s + " Ltda.");
        forn.put("cnpj", Dados.cnpjValido());
        forn.put("nomeContato", "Contato " + s);
        forn.put("emailContato", s + "@exemplo.com.br");
        forn.put("telefoneContato", "63999990000");
        forn.put("endereco", endereco);
        forn.put("idStatus", Dados.FORNECEDOR_ATIVO);
        long idForn = criar("/fornecedores", forn);

        return new Arvore(idMarca, "marca-" + s, idCategoria, "categoria-" + s, idSub, "sub-" + s, idForn);
    }

    /** Variante completa (publicável), com embalagem. */
    public static Map<String, Object> variante(String sku, double preco, int estoque) {
        Map<String, Object> v = new HashMap<>();
        v.put("nome", "Padrão");
        v.put("sku", sku);
        v.put("preco", preco);
        v.put("custoAquisicao", BigDecimal.valueOf(preco).multiply(BigDecimal.valueOf(0.6)).doubleValue());
        v.put("estoqueFisico", estoque);
        v.put("estoqueMinimo", 1);
        v.put("pesoEmbalagemKg", 1.5);
        v.put("alturaEmbalagemCm", 20);
        v.put("larguraEmbalagemCm", 30);
        v.put("comprimentoEmbalagemCm", 25);
        v.put("ehKit", false);
        v.put("ativa", true);
        return v;
    }

    /** Campos comuns de produto, ATIVO. */
    static Map<String, Object> produtoBase(String nome, Arvore a) {
        Map<String, Object> p = new HashMap<>();
        p.put("nome", nome);
        p.put("modelo", nome.toUpperCase().replace(' ', '-'));
        p.put("descricaoCurta", "Produto criado pelo teste.");
        p.put("descricao", "Descrição longa do produto criado pelo teste.");
        p.put("idMarca", a.idMarca());
        p.put("idSubcategoria", a.idSubcategoria());
        p.put("idFornecedor", a.idFornecedor());
        p.put("idStatus", PRODUTO_ATIVO);
        p.put("destaque", false);
        p.put("garantiaMeses", 12);
        p.put("dataLancamento", "2026-03-01");
        return p;
    }

    /** Corpo de drone ATIVO com todas as specs e as variantes informadas. */
    public static Map<String, Object> drone(String nome, Arvore a, List<Map<String, Object>> variantes) {
        Map<String, Object> d = produtoBase(nome, a);
        d.put("pesoGramas", 895);
        d.put("dimensoesDobrado", "22x10x9");
        d.put("dimensoesAberto", "35x29x11");
        d.put("autonomiaMinutos", 46);
        d.put("alcanceMetros", 15000);
        d.put("velocidadeMaximaKmh", 75);
        d.put("resolucaoFotoMp", 48);
        d.put("tipoSensor", "CMOS 1\"");
        d.put("resolucaoVideo", "5.1K");
        d.put("estabilizacao", "Gimbal 3 eixos");
        d.put("tipoBateria", "LiPo");
        d.put("capacidadeBateriaMah", 5000);
        d.put("tempoCarregamentoMinutos", 90);
        d.put("gpsIntegrado", true);
        d.put("sensoresAnticolisao", true);
        d.put("exigeRegistroAnac", true);
        d.put("classeAnac", "Classe 3");
        d.put("registroSisant", "");
        d.put("variantes", variantes);
        return d;
    }

    /** Corpo de acessório ATIVO. */
    public static Map<String, Object> acessorio(String nome, Arvore a, List<Map<String, Object>> variantes) {
        Map<String, Object> d = produtoBase(nome, a);
        d.put("tipoAcessorio", "Bateria");
        d.put("variantes", variantes);
        return d;
    }

    public static ProdutoCriado criarDrone(double preco, int estoque) {
        return criarDrone(arvore(), preco, estoque);
    }

    public static ProdutoCriado criarDrone(Arvore a, double preco, int estoque) {
        String s = Dados.sufixo();
        String sku = "DR-" + s.toUpperCase();
        return extrairProduto(given().contentType(ContentType.JSON)
                .body(drone("Drone " + s, a, lista(variante(sku, preco, estoque))))
                .when().post("/drones").then().statusCode(200).extract().jsonPath(), sku);
    }

    public static ProdutoCriado criarAcessorio(double preco, int estoque) {
        return criarAcessorio(arvore(), preco, estoque);
    }

    public static ProdutoCriado criarAcessorio(Arvore a, double preco, int estoque) {
        String s = Dados.sufixo();
        String sku = "AC-" + s.toUpperCase();
        return extrairProduto(given().contentType(ContentType.JSON)
                .body(acessorio("Acessorio " + s, a, lista(variante(sku, preco, estoque))))
                .when().post("/acessorios").then().statusCode(200).extract().jsonPath(), sku);
    }

    private static ProdutoCriado extrairProduto(JsonPath j, String sku) {
        return new ProdutoCriado(j.getLong("id"), j.getString("slug"), j.getLong("variantes[0].id"), sku);
    }

    // ---------------------------------------------------------------- pessoas

    public record Cliente(long id, String email, String senha, String token) {
    }

    /** CPF válido, só dígitos, diferente a cada chamada. */
    public static String cpfValido() {
        int[] n = new int[11];
        for (int i = 0; i < 9; i++)
            n[i] = ThreadLocalRandom.current().nextInt(10);
        n[9] = dvCpf(n, 9);
        n[10] = dvCpf(n, 10);
        StringBuilder sb = new StringBuilder();
        for (int d : n)
            sb.append(d);
        return sb.toString();
    }

    private static int dvCpf(int[] n, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++)
            soma += n[i] * (tamanho + 1 - i);
        int r = (soma * 10) % 11;
        return r == 10 ? 0 : r;
    }

    /** Corpo de criação de conta PF (com os consentimentos que a #46 passa a exigir). */
    public static Map<String, Object> novaConta(String email) {
        Map<String, Object> c = new HashMap<>();
        c.put("nome", "Cliente " + email.substring(0, email.indexOf('@')));
        c.put("email", email);
        c.put("senha", "segredo123");
        c.put("idTipoPessoa", PF);
        c.put("documento", cpfValido());
        c.put("telefone", "63999990000");
        c.put("consentimentos", List.of(
                Map.of("tipo", "TERMOS_USO", "versao", "2026-10"),
                Map.of("tipo", "POLITICA_PRIVACIDADE", "versao", "2026-10")));
        return c;
    }

    /** Cria a conta pela rota pública e faz login. */
    public static Cliente novoCliente() {
        String email = Dados.sufixo() + "@cliente.com.br";
        long id = RestAssured.given().contentType(ContentType.JSON).body(novaConta(email))
                .when().post("/clientes").then().statusCode(200).extract().jsonPath().getLong("id");
        return new Cliente(id, email, "segredo123", Api.login(email, "segredo123"));
    }

    /** Endereço principal do cliente em Palmas; devolve o id. */
    public static long enderecoCliente(Cliente c) {
        Map<String, Object> e = new HashMap<>();
        e.put("apelido", "Casa");
        e.put("cep", CEP_DESTINO);
        e.put("logradouro", "Quadra 104 Norte");
        e.put("numero", "10");
        e.put("complemento", "");
        e.put("bairro", "Plano Diretor Norte");
        e.put("idMunicipio", idPalmas());
        e.put("principal", true);
        return Api.como(c.token()).contentType(ContentType.JSON).body(e)
                .when().post("/conta/enderecos").then().statusCode(200).extract().jsonPath().getLong("id");
    }

    /** Usuário administrativo com o papel informado; devolve o token. */
    public static String adminComPapel(long idPapel) {
        String email = Dados.sufixo() + "@aeria.com.br";
        Map<String, Object> u = new HashMap<>();
        u.put("nome", "Usuario " + email);
        u.put("email", email);
        u.put("senha", "provisoria123");
        u.put("idPapel", idPapel);
        u.put("ativo", true);
        given().contentType(ContentType.JSON).body(u).when().post("/admin/usuarios").then().statusCode(200);
        return Api.login(email, "provisoria123");
    }

    // ---------------------------------------------------------------- compra

    public static String novoCarrinho() {
        return RestAssured.given().when().post("/carrinho").then().statusCode(200)
                .extract().jsonPath().getString("token");
    }

    public static void adicionar(String tokenCarrinho, long idVariante, int quantidade) {
        Api.carrinho(tokenCarrinho).contentType(ContentType.JSON)
                .body(Map.of("idVariante", idVariante, "quantidade", quantidade))
                .when().post("/carrinho/itens").then().statusCode(200);
    }

    /**
     * Pedido PENDENTE do cliente: carrinho anônimo, vínculo, reserva e checkout
     * com entrega PADRAO e o método informado. Devolve o id do pedido.
     */
    public static long pedidoPendente(Cliente c, long idVariante, int quantidade, long idMetodo) {
        String carrinho = novoCarrinho();
        adicionar(carrinho, idVariante, quantidade);
        Api.carrinho(c.token(), carrinho).when().post("/carrinho/vincular").then().statusCode(200);
        Api.carrinho(c.token(), carrinho).when().post("/carrinho/reservar").then().statusCode(200);
        long idEndereco = enderecoCliente(c);
        String servico = Api.carrinho(c.token(), carrinho).contentType(ContentType.JSON)
                .body(Map.of("cep", CEP_DESTINO)).when().post("/frete/cotacao")
                .then().statusCode(200).extract().jsonPath()
                .getString("find { it.modalidade.id == " + ENTREGA_PADRAO + " }.codigoServicoTransportadora");
        Map<String, Object> checkout = new HashMap<>();
        checkout.put("idEndereco", idEndereco);
        checkout.put("idModalidadeEntrega", ENTREGA_PADRAO);
        checkout.put("codigoServicoTransportadora", servico);
        checkout.put("idMetodoPagamento", idMetodo);
        return Api.carrinho(c.token(), carrinho).contentType(ContentType.JSON).body(checkout)
                .when().post("/checkout").then().statusCode(200).extract().jsonPath().getLong("id");
    }

    /** Pedido PAGO: pendente + cartão aprovado no Mercado Pago falso. */
    public static long pedidoPago(Cliente c, long idVariante, int quantidade) {
        long id = pedidoPendente(c, idVariante, quantidade, CARTAO);
        pagarComCartao(c, id, "tok_aprovado").then().statusCode(200);
        return id;
    }

    public static io.restassured.response.Response pagarComCartao(Cliente c, long idPedido, String tokenCartao) {
        Map<String, Object> p = new HashMap<>();
        p.put("idMetodo", CARTAO);
        p.put("tokenCartao", tokenCartao);
        p.put("salvarCartao", false);
        return Api.como(c.token()).contentType(ContentType.JSON).body(p)
                .when().post("/pedidos/{id}/pagamentos", idPedido);
    }

    /** Leva um pedido PAGO até ENTREGUE pelo admin (use acessório, que não exige série). */
    public static void avancarAteEntregue(long idPedido) {
        for (long status : new long[] { PEDIDO_SEPARACAO, PEDIDO_ENVIADO, PEDIDO_ENTREGUE })
            mudarStatus(idPedido, status).then().statusCode(204);
    }

    public static io.restassured.response.Response mudarStatus(long idPedido, long idStatus) {
        return given().contentType(ContentType.JSON).body(Map.of("idStatus", idStatus))
                .when().patch("/pedidos/{id}/status", idPedido);
    }

    /** Id de um pedido da carga de dados no status informado (a #16 garante três por status). */
    public static long pedidoDaCarga(long idStatus) {
        return given().queryParam("idStatus", idStatus).queryParam("pageSize", 1)
                .when().get("/pedidos").then().statusCode(200)
                .extract().jsonPath().getLong("items[0].id");
    }

    // ---------------------------------------------------------------- apoio

    @SafeVarargs
    public static List<Map<String, Object>> lista(Map<String, Object>... itens) {
        return new ArrayList<>(List.of(itens));
    }

    public static long criar(String rota, Map<String, Object> corpo) {
        return given().contentType(ContentType.JSON).body(corpo)
                .when().post(rota).then().statusCode(200).extract().jsonPath().getLong("id");
    }
}
