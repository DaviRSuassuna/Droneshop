package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.endereco.dto.EnderecoResponseDTO;

public record FornecedorResponseDTO(
    Long id,
    String razaoSocial,
    String cnpj,
    String nomeContato,
    String emailContato,
    String telefoneContato,
    EnderecoResponseDTO endereco,
    String status
) {
    public static FornecedorResponseDTO valueOf(Fornecedor fornecedor) {
        return new FornecedorResponseDTO(
            fornecedor.getId(),
            fornecedor.getRazaoSocial(),
            fornecedor.getCnpj(),
            fornecedor.getNomeContato(),
            fornecedor.getEmailContato(),
            fornecedor.getTelefoneContato(),
            EnderecoResponseDTO.valueOf(fornecedor.getEndereco()),
            fornecedor.getStatus().name());
    }

}
