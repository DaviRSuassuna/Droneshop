package br.com.droneshop.administracao.dto;

import java.util.List;

import br.com.droneshop.administracao.model.PapelAdministrativo;

public record UsuarioAdministrativoResponseDTO(
    Long id,
    String nome,
    String email,
    boolean ativo,
    boolean twoFAHabilitado,
    PapelAdministrativo papel,
    List<PermissaoItemResponseDTO> permissoes
) {}
