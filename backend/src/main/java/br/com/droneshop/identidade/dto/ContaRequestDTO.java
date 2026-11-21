package br.com.droneshop.identidade.dto;

import java.util.List;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Criação de conta do cliente (público). Documento e telefone chegam com ou sem máscara.
public record ContaRequestDTO(
    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(max = 120, message = "O campo deve ter no máximo 120 caracteres (back).")
    String nome,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Email(message = "E-mail inválido (back).")
    @Length(max = 254, message = "O campo deve ter no máximo 254 caracteres (back).")
    String email,

    @NotBlank(message = "O campo deve ser informado (back).")
    @Length(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres (back).")
    String senha,

    @NotNull(message = "Selecione o tipo de pessoa (back).")
    Long idTipoPessoa,

    @NotBlank(message = "O campo deve ser informado (back).")
    String documento,

    @Length(max = 20, message = "O campo deve ter no máximo 20 caracteres (back).")
    String telefone,

    // termos e política na versão vigente (GET /loja/termos), conferidos no service (#46)
    List<ConsentimentoRequestDTO> consentimentos
) {
}
