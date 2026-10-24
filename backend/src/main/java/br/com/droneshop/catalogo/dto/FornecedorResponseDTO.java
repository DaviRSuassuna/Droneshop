package br.com.droneshop.catalogo.dto;

import br.com.droneshop.catalogo.model.StatusFornecedor;
import br.com.droneshop.endereco.dto.EnderecoResponseDTO;

// cnpj, telefone e cep saem so com digitos; a mascara e do front
public record FornecedorResponseDTO(
    Long id,
    String razaoSocial,
    String cnpj,
    String nomeContato,
    String emailContato,
    String telefoneContato,
    EnderecoResponseDTO endereco,
    StatusFornecedor status,
    long quantidadeProdutos
) {
}
