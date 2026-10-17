package br.com.droneshop.catalogo.mapper;

import java.time.LocalDateTime;

import br.com.droneshop.catalogo.dto.PromocaoResponseDTO;
import br.com.droneshop.catalogo.dto.VariantePromocaoResponseDTO;
import br.com.droneshop.catalogo.model.Promocao;

public final class PromocaoResponseMapper {

    private PromocaoResponseMapper() {
    }

    public static PromocaoResponseDTO toResponse(Promocao promocao) {
        return new PromocaoResponseDTO(
                promocao.getId(),
                promocao.getNome(),
                promocao.getEscopo(),
                promocao.getDataInicio(),
                promocao.getDataFim(),
                promocao.getPercentualDesconto(),
                promocao.getValorDesconto(),
                promocao.getValorMinimoCarrinho(),
                promocao.isAtiva(),
                situacao(promocao, LocalDateTime.now()),
                promocao.getVariantes().stream()
                        .map(v -> new VariantePromocaoResponseDTO(v.getId(), v.getSku(), v.getNome()))
                        .toList());
    }

    public static String situacao(Promocao promocao, LocalDateTime agora) {
        if (!promocao.isAtiva())
            return "INATIVA";
        if (agora.isBefore(promocao.getDataInicio()))
            return "AGENDADA";
        if (agora.isAfter(promocao.getDataFim()))
            return "ENCERRADA";
        return "VIGENTE";
    }
}
