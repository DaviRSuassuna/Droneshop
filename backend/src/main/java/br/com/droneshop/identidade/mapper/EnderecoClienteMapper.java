package br.com.droneshop.identidade.mapper;

import br.com.droneshop.endereco.mapper.MunicipioResponseMapper;
import br.com.droneshop.endereco.model.Endereco;
import br.com.droneshop.identidade.dto.EnderecoClienteResponseDTO;
import br.com.droneshop.identidade.model.EnderecoCliente;

public final class EnderecoClienteMapper {

    private EnderecoClienteMapper() {
    }

    public static EnderecoClienteResponseDTO toResponse(EnderecoCliente e) {
        Endereco end = e.getEndereco();
        return new EnderecoClienteResponseDTO(e.getId(), e.getApelido(), end.getCep(), end.getLogradouro(),
                end.getNumero(), end.getComplemento(), end.getBairro(),
                MunicipioResponseMapper.toResponse(end.getMunicipio()), e.isPrincipal());
    }
}
