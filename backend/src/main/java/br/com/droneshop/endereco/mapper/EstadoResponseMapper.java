package br.com.droneshop.endereco.mapper;

import br.com.droneshop.endereco.dto.EstadoResponseDTO;
import br.com.droneshop.endereco.model.Estado;

public final class EstadoResponseMapper {

    private EstadoResponseMapper() {
    }

    public static EstadoResponseDTO toResponse(Estado estado) {
        return new EstadoResponseDTO(estado.getId(), estado.getNome(), estado.getSigla(), estado.getCodigoIbge());
    }
}
