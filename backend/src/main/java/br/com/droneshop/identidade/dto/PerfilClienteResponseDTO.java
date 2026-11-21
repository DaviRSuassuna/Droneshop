package br.com.droneshop.identidade.dto;

import br.com.droneshop.identidade.model.TipoPessoa;

public record PerfilClienteResponseDTO(
    String nome,
    String email,
    TipoPessoa tipoPessoa,
    String documento,
    String telefone
) {
}
