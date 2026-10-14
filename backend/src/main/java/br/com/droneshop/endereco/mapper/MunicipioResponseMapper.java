package br.com.droneshop.endereco.mapper;

import br.com.droneshop.endereco.dto.MunicipioResponseDTO;
import br.com.droneshop.endereco.model.Municipio;

public final class MunicipioResponseMapper {

    private MunicipioResponseMapper() {
    }

    public static MunicipioResponseDTO toResponse(Municipio municipio) {
        return new MunicipioResponseDTO(
                municipio.getId(),
                municipio.getNome(),
                municipio.getCodigoIbge(),
                EstadoResponseMapper.toResponse(municipio.getEstado()));
    }
}
