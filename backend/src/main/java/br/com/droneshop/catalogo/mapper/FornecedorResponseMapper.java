package br.com.droneshop.catalogo.mapper;

import br.com.droneshop.catalogo.dto.FornecedorResponseDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.endereco.mapper.EnderecoResponseMapper;

public final class FornecedorResponseMapper {

    private FornecedorResponseMapper() {
    }

    public static FornecedorResponseDTO toResponse(Fornecedor fornecedor) {
        return new FornecedorResponseDTO(
                fornecedor.getId(),
                fornecedor.getRazaoSocial(),
                fornecedor.getCnpj(),
                fornecedor.getNomeContato(),
                fornecedor.getEmailContato(),
                fornecedor.getTelefoneContato(),
                EnderecoResponseMapper.toResponse(fornecedor.getEndereco()),
                fornecedor.getStatus());
    }
}
