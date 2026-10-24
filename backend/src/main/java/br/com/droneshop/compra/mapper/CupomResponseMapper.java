package br.com.droneshop.compra.mapper;

import java.time.LocalDateTime;

import br.com.droneshop.compra.dto.CupomResponseDTO;
import br.com.droneshop.compra.dto.UsoCupomResponseDTO;
import br.com.droneshop.compra.model.Cupom;
import br.com.droneshop.compra.model.UsoCupom;

public final class CupomResponseMapper {

    private CupomResponseMapper() {}

    public static CupomResponseDTO toResponse(Cupom cupom, long quantidadeUsos) {
        return new CupomResponseDTO(
                cupom.getId(),
                cupom.getCodigo(),
                cupom.getTipoDesconto(),
                cupom.getValor(),
                cupom.getDataInicio(),
                cupom.getDataFim(),
                cupom.getValorMinimo(),
                cupom.getLimiteTotalUso(),
                cupom.getLimitePorCliente(),
                cupom.isAtivo(),
                situacao(cupom, quantidadeUsos, LocalDateTime.now()),
                quantidadeUsos,
                cupom.getCategorias().stream()
                        .map(c -> new CupomResponseDTO.ReferenciaCupomDTO(c.getId(), c.getNome())).toList(),
                cupom.getMarcas().stream()
                        .map(m -> new CupomResponseDTO.ReferenciaCupomDTO(m.getId(), m.getNome())).toList(),
                cupom.getProdutos().stream()
                        .map(p -> new CupomResponseDTO.ReferenciaCupomDTO(p.getId(), p.getNome(), p.getSku())).toList());
    }

    public static UsoCupomResponseDTO toUsoResponse(UsoCupom uso) {
        return new UsoCupomResponseDTO(
                uso.getId(),
                new UsoCupomResponseDTO.PedidoResumo(uso.getPedido().getId()),
                new UsoCupomResponseDTO.ClienteResumo(uso.getCliente().getId(), uso.getCliente().getNome()),
                uso.getDataUso());
    }

    public static String situacao(Cupom cupom, long usos, LocalDateTime agora) {
        if (!cupom.isAtivo()) return "INATIVO";
        if (cupom.getLimiteTotalUso() != null && usos >= cupom.getLimiteTotalUso()) return "ESGOTADO";
        if (agora.isBefore(cupom.getDataInicio())) return "AGENDADO";
        if (cupom.getDataFim() != null && agora.isAfter(cupom.getDataFim())) return "ENCERRADO";
        return "VIGENTE";
    }
}
