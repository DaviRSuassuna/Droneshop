package br.com.droneshop.catalogo.mapper;

import br.com.droneshop.catalogo.dto.MarcaResponseDTO;
import br.com.droneshop.catalogo.model.Marca;

public final class MarcaResponseMapper {

    private MarcaResponseMapper() {
    }

    public static MarcaResponseDTO toResponse(Marca marca) {
        return new MarcaResponseDTO(
                marca.getId(),
                marca.getNome(),
                marca.getSlug(),
                marca.getPaisOrigem(),
                marca.getSiteOficial(),
                marca.getDescricao(),
                marca.getLogoUrl(),
                marca.getStatus(),
                marca.isDestacarHome());
    }
}
