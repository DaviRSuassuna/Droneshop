package br.com.droneshop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

class EcommerceResourceTest {
    private final EcommerceResource resource = new EcommerceResource();

    @Test
    void deveCriarClienteEnderecoCarrinhoEPedido() {
        Response clienteResponse = resource.criarCliente(Map.of("nome", "Ana", "email", "ana@example.com"));
        Map<?, ?> cliente = (Map<?, ?>) clienteResponse.getEntity();
        String clienteId = cliente.get("id").toString();

        Response enderecoResponse = resource.criarEndereco(clienteId, Map.of("cidade", "Sao Paulo", "cep", "01000-000"));
        resource.adicionarCarrinho(clienteId, Map.of("produtoId", "drone-1", "quantidade", 2));
        Response pedidoResponse = resource.criarPedido(clienteId, Map.of("total", 3999.90));

        assertEquals(201, clienteResponse.getStatus());
        assertEquals(201, enderecoResponse.getStatus());
        assertEquals(1, resource.listarEnderecos(clienteId).size());
        assertEquals(1, resource.carrinho(clienteId).size());
        assertEquals(201, pedidoResponse.getStatus());
        assertEquals(1, resource.pedidosDoCliente(clienteId).size());
        assertNotNull(((Map<?, ?>) pedidoResponse.getEntity()).get("id"));
    }

    @Test
    void deveCriarValidarEPublicarCupomEAvaliacao() {
        Response cupomResponse = resource.criarCupom(Map.of("codigo", "BEMVINDO10", "valor", 10));
        Response cupomValido = resource.validarCupom("BEMVINDO10");
        Response cupomInvalido = resource.validarCupom("INVALIDO");
        Response avaliacaoResponse = resource.criarAvaliacao("drone-1", Map.of("nota", 5, "comentario", "Excelente"));
        String avaliacaoId = ((Map<?, ?>) avaliacaoResponse.getEntity()).get("id").toString();
        Response avaliacaoPublicada = resource.publicarAvaliacao(avaliacaoId);

        assertEquals(201, cupomResponse.getStatus());
        assertEquals(200, cupomValido.getStatus());
        assertEquals(404, cupomInvalido.getStatus());
        assertEquals(201, avaliacaoResponse.getStatus());
        assertEquals(200, avaliacaoPublicada.getStatus());
        assertTrue(resource.listarAvaliacoes("drone-1").stream()
                .anyMatch(avaliacao -> "PUBLICADO".equals(avaliacao.get("status"))));
    }
}