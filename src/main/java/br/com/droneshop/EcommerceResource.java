package br.com.droneshop;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.inject.Inject;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EcommerceResource {
    @Inject
    DroneRepository droneRepository;
    private final Map<String, Map<String, Object>> clientes = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> enderecos = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> marcas = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> categorias = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> acessorios = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> cupons = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> pedidos = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> avaliacoes = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> carrinhos = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> desejos = new ConcurrentHashMap<>();

    private Response criado(Map<String, Object> valor) {
        return Response.status(Response.Status.CREATED).entity(valor).build();
    }

    private Map<String, Object> novo(Map<String, Object> dados) {
        Map<String, Object> resultado = new ConcurrentHashMap<>(dados == null ? Map.of() : dados);
        resultado.putIfAbsent("id", UUID.randomUUID().toString());
        resultado.putIfAbsent("criadoEm", OffsetDateTime.now().toString());
        return resultado;
    }

    private Response buscar(Map<String, Map<String, Object>> origem, String id) {
        Map<String, Object> valor = origem.get(id);
        return valor == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(valor).build();
    }

    @GET @Path("acessorios") public List<Map<String, Object>> listarAcessorios() { return new ArrayList<>(acessorios.values()); }
    @POST @Path("acessorios") public Response criarAcessorio(Map<String, Object> dados) { Map<String, Object> v = novo(dados); acessorios.put(v.get("id").toString(), v); return criado(v); }

    @GET @Path("marcas") public List<Map<String, Object>> listarMarcas() { return new ArrayList<>(marcas.values()); }
    @POST @Path("marcas") public Response criarMarca(Map<String, Object> dados) { Map<String, Object> v = novo(dados); marcas.put(v.get("id").toString(), v); return criado(v); }
    @GET @Path("marcas/{id}") public Response buscarMarca(@PathParam("id") String id) { return buscar(marcas, id); }
    @PUT @Path("marcas/{id}") public Response editarMarca(@PathParam("id") String id, Map<String, Object> dados) { if (!marcas.containsKey(id)) return Response.status(404).build(); dados = novo(dados); dados.put("id", id); marcas.put(id, dados); return Response.ok(dados).build(); }

    @GET @Path("categorias") public List<Map<String, Object>> listarCategorias() { return new ArrayList<>(categorias.values()); }
    @POST @Path("categorias") public Response criarCategoria(Map<String, Object> dados) { Map<String, Object> v = novo(dados); categorias.put(v.get("id").toString(), v); return criado(v); }
    @GET @Path("categorias/{id}/produtos") public List<Map<String, Object>> produtosDaCategoria(@PathParam("id") String id) { return List.of(); }
    @GET @Path("cupons") public List<Map<String, Object>> listarCupons() { return new ArrayList<>(cupons.values()); }
    @POST @Path("cupons") public Response criarCupom(Map<String, Object> dados) { Map<String, Object> v = novo(dados); cupons.put(v.get("id").toString(), v); return criado(v); }
    @POST @Path("cupons/{codigo}/validar") public Response validarCupom(@PathParam("codigo") String codigo) { return cupons.values().stream().anyMatch(c -> codigo.equals(c.get("codigo"))) ? Response.ok(Map.of("valido", true, "codigo", codigo)).build() : Response.status(404).entity(Map.of("valido", false)).build(); }

    @GET @Path("clientes/{id}") public Response buscarCliente(@PathParam("id") String id) { return buscar(clientes, id); }
    @POST @Path("clientes") public Response criarCliente(Map<String, Object> dados) { Map<String, Object> v = novo(dados); clientes.put(v.get("id").toString(), v); return criado(v); }
    @PUT @Path("clientes/{id}") public Response editarCliente(@PathParam("id") String id, Map<String, Object> dados) { if (!clientes.containsKey(id)) return Response.status(404).build(); dados = novo(dados); dados.put("id", id); clientes.put(id, dados); return Response.ok(dados).build(); }
    @GET @Path("clientes/{id}/pedidos") public List<Map<String, Object>> pedidosDoCliente(@PathParam("id") String id) { return pedidos.values().stream().filter(p -> id.equals(p.get("clienteId"))).toList(); }

    @GET @Path("clientes/{id}/carrinho") public List<Map<String, Object>> carrinho(@PathParam("id") String id) { return carrinhos.computeIfAbsent(id, k -> new ArrayList<>()); }
    @POST @Path("clientes/{id}/carrinho/itens") public Response adicionarCarrinho(@PathParam("id") String id, Map<String, Object> item) { Map<String, Object> v = novo(item); carrinhos.computeIfAbsent(id, k -> new ArrayList<>()).add(v); return criado(v); }
    @DELETE @Path("clientes/{clienteId}/carrinho/itens/{itemId}") public Response removerCarrinho(@PathParam("clienteId") String clienteId, @PathParam("itemId") String itemId) { List<Map<String, Object>> lista = carrinhos.getOrDefault(clienteId, new ArrayList<>()); boolean removeu = lista.removeIf(i -> itemId.equals(i.get("id"))); return removeu ? Response.noContent().build() : Response.status(404).build(); }
    @POST @Path("clientes/{id}/carrinho/cupom") public Response aplicarCupom(@PathParam("id") String id, Map<String, Object> dados) { carrinhos.computeIfAbsent(id, k -> new ArrayList<>()).add(Map.of("cupom", dados.get("codigo"))); return Response.ok(carrinho(id)).build(); }

    @GET @Path("clientes/{id}/lista-desejos") public List<Map<String, Object>> listarDesejos(@PathParam("id") String id) { return desejos.computeIfAbsent(id, k -> new ArrayList<>()); }
    @POST @Path("clientes/{clienteId}/lista-desejos/{produtoId}") public Response adicionarDesejo(@PathParam("clienteId") String clienteId, @PathParam("produtoId") String produtoId) { Map<String, Object> v = Map.of("produtoId", produtoId); desejos.computeIfAbsent(clienteId, k -> new ArrayList<>()).add(v); return criado(v); }
    @DELETE @Path("clientes/{clienteId}/lista-desejos/{produtoId}") public Response removerDesejo(@PathParam("clienteId") String c, @PathParam("produtoId") String p) { desejos.getOrDefault(c, new ArrayList<>()).removeIf(i -> p.equals(i.get("produtoId"))); return Response.noContent().build(); }
    @POST @Path("clientes/{clienteId}/lista-desejos/{produtoId}/mover-para-carrinho") public Response moverDesejo(@PathParam("clienteId") String c, @PathParam("produtoId") String p) { removerDesejo(c, p); return adicionarCarrinho(c, Map.of("produtoId", p, "quantidade", 1)); }

    @POST @Path("clientes/{clienteId}/pedidos") public Response criarPedido(@PathParam("clienteId") String clienteId, Map<String, Object> dados) { Map<String, Object> v = novo(dados); v.put("clienteId", clienteId); v.putIfAbsent("status", "PENDENTE"); v.putIfAbsent("total", BigDecimal.ZERO); pedidos.put(v.get("id").toString(), v); return criado(v); }
    @GET @Path("pedidos/{id}") public Response buscarPedido(@PathParam("id") String id) { return buscar(pedidos, id); }
    @POST @Path("pedidos/{id}/confirmar") public Response confirmarPedido(@PathParam("id") String id) { return alterarStatus(id, Map.of("status", "PAGO")); }
    @PATCH @Path("pedidos/{id}/status") public Response alterarStatus(@PathParam("id") String id, Map<String, Object> dados) { Map<String, Object> p = pedidos.get(id); if (p == null) return Response.status(404).build(); p.put("status", dados.get("status")); return Response.ok(p).build(); }
    @POST @Path("pedidos/{id}/pagamento") public Response pagamento(@PathParam("id") String id, Map<String, Object> dados) { Map<String, Object> v = novo(dados); v.put("pedidoId", id); v.put("status", "APROVADO"); return criado(v); }
    @POST @Path("pedidos/{id}/pagamento/estorno") public Response estorno(@PathParam("id") String id) { return Response.ok(Map.of("pedidoId", id, "status", "ESTORNADO")).build(); }

    @GET @Path("clientes/{id}/enderecos") public List<Map<String, Object>> listarEnderecos(@PathParam("id") String id) { return enderecos.values().stream().filter(e -> id.equals(e.get("clienteId"))).toList(); }
    @POST @Path("clientes/{id}/enderecos") public Response criarEndereco(@PathParam("id") String id, Map<String, Object> dados) { Map<String, Object> v = novo(dados); v.put("clienteId", id); enderecos.put(v.get("id").toString(), v); return criado(v); }
    @PUT @Path("enderecos/{id}") public Response editarEndereco(@PathParam("id") String id, Map<String, Object> dados) { if (!enderecos.containsKey(id)) return Response.status(404).build(); dados.put("id", id); enderecos.put(id, dados); return Response.ok(dados).build(); }
    @DELETE @Path("enderecos/{id}") public Response removerEndereco(@PathParam("id") String id) { return enderecos.remove(id) == null ? Response.status(404).build() : Response.noContent().build(); }
    @GET @Path("pedidos/{id}/entrega") public Response entrega(@PathParam("id") String id) { return Response.ok(Map.of("pedidoId", id, "status", "AGUARDANDO")).build(); }
    @GET @Path("entregas/{id}/rastreamento") public Response rastreamento(@PathParam("id") String id) { return Response.ok(List.of(Map.of("descricao", "Pedido recebido", "status", "AGUARDANDO"))).build(); }

    @GET @Path("produtos/{id}/avaliacoes") public List<Map<String, Object>> listarAvaliacoes(@PathParam("id") String id) { return avaliacoes.values().stream().filter(a -> id.equals(a.get("produtoId"))).toList(); }
    @POST @Path("produtos/{id}/avaliacoes") public Response criarAvaliacao(@PathParam("id") String id, Map<String, Object> dados) { Map<String, Object> v = novo(dados); v.put("produtoId", id); v.putIfAbsent("status", "RASCUNHO"); avaliacoes.put(v.get("id").toString(), v); return criado(v); }
    @POST @Path("avaliacoes/{id}/publicar") public Response publicarAvaliacao(@PathParam("id") String id) { Map<String, Object> v = avaliacoes.get(id); if (v == null) return Response.status(404).build(); v.put("status", "PUBLICADO"); return Response.ok(v).build(); }

    @GET @Path("admin/produtos") public List<Drone> adminProdutos() { return droneRepository.listAll(); }
    @PUT @Path("admin/produtos/{id}") public Response editarProduto(@PathParam("id") UUID id, Drone dados) { Drone drone = droneRepository.findById(id); if (drone == null) return Response.status(404).build(); drone.nome = dados.nome; drone.preco = dados.preco; drone.estoque = dados.estoque; drone.status = dados.status; return Response.ok(drone).build(); }
    @GET @Path("admin/pedidos") public List<Map<String, Object>> adminPedidos() { return new ArrayList<>(pedidos.values()); }
    @PATCH @Path("admin/pedidos/{id}/status") public Response adminStatus(@PathParam("id") String id, Map<String, Object> dados) { return alterarStatus(id, dados); }
    @POST @Path("admin/login") public Response adminLogin(Map<String, Object> dados) { return Response.ok(Map.of("autenticado", true, "email", dados.getOrDefault("email", ""))).build(); }
}