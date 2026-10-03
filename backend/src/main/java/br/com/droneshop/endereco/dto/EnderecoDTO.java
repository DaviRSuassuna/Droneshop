package br.com.droneshop.endereco.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Entrada do endereco, embutida no DTO de quem tem endereco (hoje, FornecedorDTO).
// Cidade e UF nao vem como texto: o front manda o id do Municipio escolhido.
public final class EnderecoDTO {

    // aceita com ou sem hifen; o service guarda so os 8 digitos
    @NotBlank(message = "O campo deve ser informado (back).")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido. Use o formato 00000-000 (back).")
    private final String cep;

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 150, message = "O campo deve ter no máximo 150 caracteres (back).")
    private final String logradouro;

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 10, message = "O campo deve ter no máximo 10 caracteres (back).")
    private final String numero;

    @Length(max = 60, message = "O campo deve ter no máximo 60 caracteres (back).")
    private final String complemento;

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 80, message = "O campo deve ter no máximo 80 caracteres (back).")
    private final String bairro;

    @NotNull(message = "Selecione o município (back).")
    private final Long idMunicipio;

    public EnderecoDTO(String cep, String logradouro, String numero, String complemento, String bairro,
                       Long idMunicipio) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.idMunicipio = idMunicipio;
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public Long getIdMunicipio() {
        return idMunicipio;
    }
}
