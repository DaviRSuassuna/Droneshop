package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class FornecedorDTO {

    // Diferente de Marca/Categoria, Fornecedor nao tem RASCUNHO: os campos
    // obrigatorios sao sempre exigidos, entao o @NotBlank fica aqui mesmo.

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 2, max = 150, message = "O campo deve conter entre 2 e 150 caracteres (back).")
    private final String razaoSocial;

    // @CNPJ aceita com ou sem mascara e valida os digitos verificadores
    @NotBlank(message = "O campo deve ser informado (back).")
    @CNPJ(message = "CNPJ inválido (back).")
    private final String cnpj;

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 2, max = 100, message = "O campo deve conter entre 2 e 100 caracteres (back).")
    private final String nomeContato;

    @NotBlank(message = "O campo deve ser informado (back).")
    @Email(message = "E-mail inválido (back).")
    @Length(max = 150, message = "O campo deve ter no máximo 150 caracteres (back).")
    private final String emailContato;

    @Length(max = 20, message = "O campo deve ter no máximo 20 caracteres (back).")
    private final String telefoneContato;

    // valor esperado: "ATIVO" ou "INATIVO"
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String status;

    public FornecedorDTO(String razaoSocial, String cnpj, String nomeContato, String emailContato,
                         String telefoneContato, String status) {
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.nomeContato = nomeContato;
        this.emailContato = emailContato;
        this.telefoneContato = telefoneContato;
        this.status = status;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getNomeContato() {
        return nomeContato;
    }

    public String getEmailContato() {
        return emailContato;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public String getStatus() {
        return status;
    }
}
