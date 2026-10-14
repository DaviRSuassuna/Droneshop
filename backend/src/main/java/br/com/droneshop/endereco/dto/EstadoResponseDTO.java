package br.com.droneshop.endereco.dto;

public record EstadoResponseDTO(
    Long id,
    String nome,
    String sigla,
    Integer codigoIbge
) {
}
