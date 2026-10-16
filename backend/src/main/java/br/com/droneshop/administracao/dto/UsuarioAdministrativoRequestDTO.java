package br.com.droneshop.administracao.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioAdministrativoRequestDTO(
    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    String nome,
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres.")
    String email,
    String senha,
    Long idPapel,
    Boolean ativo,
    List<@Valid PermissaoItemRequestDTO> permissoes
) {}
