package br.com.droneshop.endereco.dto;

import br.com.droneshop.endereco.model.Municipio;

// o estado vai aninhado, para o front exibir "Palmas/TO" e preencher o select de Estado
public record MunicipioResponseDTO(
    Long id,
    String nome,
    Integer codigoIbge,
    EstadoResponseDTO estado
) {
    public static MunicipioResponseDTO valueOf(Municipio municipio) {
        if (municipio == null)
            return null;
        return new MunicipioResponseDTO(
            municipio.getId(),
            municipio.getNome(),
            municipio.getCodigoIbge(),
            EstadoResponseDTO.valueOf(municipio.getEstado()));
    }

}
