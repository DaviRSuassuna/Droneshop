package br.com.droneshop.identidade.dto;

import br.com.droneshop.endereco.dto.MunicipioResponseDTO;

public record EnderecoClienteResponseDTO(
    Long id,
    String apelido,
    String cep,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    MunicipioResponseDTO municipio,
    boolean principal
) {
}
