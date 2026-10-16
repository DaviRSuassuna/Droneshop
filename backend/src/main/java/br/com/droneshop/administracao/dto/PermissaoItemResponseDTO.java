package br.com.droneshop.administracao.dto;

import br.com.droneshop.administracao.model.ModuloSistema;

public record PermissaoItemResponseDTO(
    ModuloSistema modulo,
    boolean podeVer,
    boolean podeEditar,
    boolean podeExcluir
) {}
