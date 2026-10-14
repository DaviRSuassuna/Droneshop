package br.com.droneshop.endereco.dto;

public record MunicipioResponseDTO(
    Long id,
    String nome,
    Integer codigoIbge,
    EstadoResponseDTO estado
) {
}
