package br.com.droneshop.catalogo.mapper;

import java.util.List;

import br.com.droneshop.catalogo.dto.MidiaProdutoResponseDTO;
import br.com.droneshop.catalogo.model.MidiaProduto;

public final class MidiaProdutoResponseMapper {

    private MidiaProdutoResponseMapper() {
    }

    public static MidiaProdutoResponseDTO toResponse(MidiaProduto midia) {
        return new MidiaProdutoResponseDTO(
                midia.getId(),
                midia.getTipo(),
                midia.getUrl(),
                midia.isPrincipal(),
                midia.getOrdem(),
                midia.getAltText());
    }

    public static List<MidiaProdutoResponseDTO> toResponse(List<MidiaProduto> midias) {
        return midias.stream().map(MidiaProdutoResponseMapper::toResponse).toList();
    }
}
