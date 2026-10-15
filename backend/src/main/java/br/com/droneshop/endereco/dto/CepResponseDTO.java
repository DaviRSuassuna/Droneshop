package br.com.droneshop.endereco.dto;

// Resposta do GET /ceps/{cep}: o que o ViaCEP devolveu, com o Municipio ja resolvido no banco.
// "localidade" e "uf" sao os textos do ViaCEP; servem de pista quando municipio vier null.
public record CepResponseDTO(
    String cep,
    String logradouro,
    String complemento,
    String bairro,
    String localidade,
    String uf,
    MunicipioResponseDTO municipio
) {
}
