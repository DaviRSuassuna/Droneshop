package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.Fornecedor;

public record FornecedorResponseDTO(
    Long id,
    String razaoSocial,
    String cnpj,
    String nomeContato,
    String emailContato,
    String telefoneContato,
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
            fornecedor.getStatus().name());
    }

}
