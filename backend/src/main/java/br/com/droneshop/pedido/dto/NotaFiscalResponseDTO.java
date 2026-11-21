package br.com.droneshop.pedido.dto;

import java.time.LocalDateTime;

import br.com.droneshop.pedido.model.NotaFiscal;
import br.com.droneshop.pedido.model.TipoOrigemNota;

public record NotaFiscalResponseDTO(Long id, TipoOrigemNota tipoOrigem, String numero, String serie,
        String chaveAcesso, String xmlUrl, String danfeUrl, LocalDateTime dataEmissao) {

    public static NotaFiscalResponseDTO de(NotaFiscal n) {
        return new NotaFiscalResponseDTO(n.getId(), n.getTipoOrigem(), n.getNumero(), n.getSerie(),
                n.getChaveAcesso(), n.getXmlUrl(), n.getDanfeUrl(), n.getDataEmissao());
    }
}
