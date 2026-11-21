package br.com.droneshop.compra.dto;

import java.util.List;

public record MoverParaCarrinhoResponseDTO(CarrinhoResponseDTO carrinho, List<NaoAdicionado> naoAdicionados) {

    public record NaoAdicionado(Long idProduto, String motivo) {
    }
}
