package br.com.droneshop.endereco.dto;

import br.com.droneshop.endereco.model.Endereco;

// endereco -> municipio -> estado, aninhados; o CEP sai so com digitos
public record EnderecoResponseDTO(
    String cep,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    MunicipioResponseDTO municipio
) {
    public static EnderecoResponseDTO valueOf(Endereco endereco) {
        if (endereco == null)
            return null;
        return new EnderecoResponseDTO(
            endereco.getCep(),
            endereco.getLogradouro(),
            endereco.getNumero(),
            endereco.getComplemento(),
            endereco.getBairro(),
            MunicipioResponseDTO.valueOf(endereco.getMunicipio()));
    }

}
