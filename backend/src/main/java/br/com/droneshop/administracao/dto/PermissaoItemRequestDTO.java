package br.com.droneshop.administracao.dto;

public record PermissaoItemRequestDTO(
    Long idModulo,
    Boolean podeVer,
    Boolean podeEditar,
    Boolean podeExcluir
) {}
