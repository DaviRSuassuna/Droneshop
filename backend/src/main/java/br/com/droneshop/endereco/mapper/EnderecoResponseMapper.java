package br.com.droneshop.endereco.mapper;

import br.com.droneshop.endereco.dto.EnderecoResponseDTO;
import br.com.droneshop.endereco.model.Endereco;

public final class EnderecoResponseMapper {

    private EnderecoResponseMapper() {
    }

    public static EnderecoResponseDTO toResponse(Endereco endereco) {
        if (endereco == null)
            return null;
        return new EnderecoResponseDTO(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                MunicipioResponseMapper.toResponse(endereco.getMunicipio()));
    }
}
