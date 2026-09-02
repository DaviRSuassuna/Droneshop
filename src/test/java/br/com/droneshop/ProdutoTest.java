package br.com.droneshop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ProdutoTest {
    @Test
    void deveAtualizarEstoqueEPublicarProduto() {
        Drone drone = new Drone();
        drone.estoque = 10;
        drone.status = StatusProduto.RASCUNHO;

        drone.atualizarEstoque(-3);
        drone.publicar();

        assertEquals(7, drone.estoque);
        assertEquals(StatusProduto.PUBLICADO, drone.status);
    }

    @Test
    void naoDevePermitirEstoqueNegativo() {
        Drone drone = new Drone();
        drone.estoque = 2;

        assertThrows(IllegalArgumentException.class, () -> drone.atualizarEstoque(-3));
        assertEquals(2, drone.estoque);
    }

    @Test
    void deveAceitarDadosDoCatalogo() {
        Drone drone = new Drone();
        drone.nome = "Aeria Compact";
        drone.preco = new BigDecimal("1999.90");
        drone.tipo = TipoDrone.COMPACTO;

        assertEquals("Aeria Compact", drone.nome);
        assertEquals(new BigDecimal("1999.90"), drone.preco);
        assertEquals(TipoDrone.COMPACTO, drone.tipo);
    }
}