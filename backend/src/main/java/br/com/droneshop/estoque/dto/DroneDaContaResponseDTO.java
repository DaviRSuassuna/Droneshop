package br.com.droneshop.estoque.dto;

import java.time.LocalDate;

import br.com.droneshop.estoque.model.UnidadeIndividual;
import br.com.droneshop.pedido.mapper.PedidoResponseMapper;

/** Drone comprado pelo cliente, com série, garantia e registro ANAC (#88). */
public record DroneDaContaResponseDTO(Long idUnidade, Produto produto, String numeroSerie, String registroAnac,
        LocalDate dataCompra, LocalDate validadeGarantia, Pedido pedido) {

    public record Produto(Long id, String nome, String slug) {
    }

    public record Pedido(Long id, String numero) {
    }

    public static DroneDaContaResponseDTO de(UnidadeIndividual u) {
        var p = u.getProduto();
        var pedido = u.getItemPedido().getPedido();
        return new DroneDaContaResponseDTO(u.getId(), new Produto(p.getId(), p.getNome(), p.getSlug()),
                u.getNumeroSerie(), u.getRegistroAnac(), pedido.getDataCadastro().toLocalDate(),
                u.getValidadeGarantia(), new Pedido(pedido.getId(), PedidoResponseMapper.numero(pedido.getId())));
    }
}
