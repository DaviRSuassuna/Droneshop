package br.com.droneshop.catalogo.dto;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CNPJ;

import br.com.droneshop.endereco.dto.EnderecoRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Fornecedor nao tem RASCUNHO: os obrigatorios ficam aqui mesmo.
// CNPJ e telefone aceitam mascara; o service guarda so os digitos.
public record FornecedorRequestDTO(
    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 2, max = 150, message = "O campo deve conter entre 2 e 150 caracteres (back).")
    String razaoSocial,

    @NotBlank(message = "O campo deve ser informado (back).")
    @CNPJ(message = "CNPJ inválido (back).")
    String cnpj,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 2, max = 100, message = "O campo deve conter entre 2 e 100 caracteres (back).")
    String nomeContato,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Email(message = "E-mail inválido (back).")
    @Length(max = 150, message = "O campo deve ter no máximo 150 caracteres (back).")
    String emailContato,

    @Pattern(regexp = "^$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
             message = "Telefone inválido. Use (00) 0000-0000 ou (00) 00000-0000 (back).")
    String telefoneContato,

    @NotNull(message = "Informe o endereço (back).")
    @Valid
    EnderecoRequestDTO endereco,

    @NotNull(message = "O status deve ser informado (back).")
    Long idStatus
) {
}
