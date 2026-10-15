package br.com.droneshop.endereco.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Espelha o JSON de https://viacep.com.br/ws/{cep}/json/.
// Quando o CEP tem formato valido mas nao existe, o ViaCEP responde 200 com {"erro": "true"}.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
    String cep,
    String logradouro,
    String complemento,
    String bairro,
    String localidade,
    String uf,
    String ibge,
    Boolean erro
) {
}
