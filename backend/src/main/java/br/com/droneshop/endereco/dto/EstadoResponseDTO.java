package br.com.droneshop.endereco.dto;

import br.com.droneshop.endereco.model.Estado;

public record EstadoResponseDTO(
    Long id,
    String nome,
    String sigla,
    Integer codigoIbge
) {
    public static EstadoResponseDTO valueOf(Estado estado) {
        return new EstadoResponseDTO(estado.getId(), estado.getNome(), estado.getSigla(), estado.getCodigoIbge());
    }

}
