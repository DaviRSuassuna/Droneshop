package br.com.droneshop.pagamento.dto;

import br.com.droneshop.pagamento.model.CartaoSalvo;

/** Nunca devolve o token do gateway. */
public record CartaoSalvoResponseDTO(Long id, String ultimosDigitos, String bandeira, String validade, boolean padrao) {

    public static CartaoSalvoResponseDTO de(CartaoSalvo c) {
        return new CartaoSalvoResponseDTO(c.getId(), c.getUltimosDigitos(), c.getBandeira(), c.getValidade(),
                c.isPrincipal());
    }
}
