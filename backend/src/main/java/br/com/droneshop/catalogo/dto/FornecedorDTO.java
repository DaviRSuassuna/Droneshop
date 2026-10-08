package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

import br.com.droneshop.endereco.dto.EnderecoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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

    // opcional; com ou sem mascara: DDD + 8 ou 9 digitos. O service guarda so os digitos
    @Pattern(regexp = "^$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
             message = "Telefone inválido. Use (00) 0000-0000 ou (00) 00000-0000 (back).")
    private final String telefoneContato;

    // @Valid faz o Bean Validation descer para os campos do endereco
    @NotNull(message = "Informe o endereço (back).")
    @Valid
    private final EnderecoDTO endereco;

    // valor esperado: "ATIVO" ou "INATIVO"
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String status;

    public FornecedorDTO(String razaoSocial, String cnpj, String nomeContato, String emailContato,
                         String telefoneContato, EnderecoDTO endereco, String status) {
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.nomeContato = nomeContato;
        this.emailContato = emailContato;
        this.telefoneContato = telefoneContato;
        this.endereco = endereco;
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

    public EnderecoDTO getEndereco() {
        return endereco;
    }

    public String getStatus() {
        return status;
    }
}
