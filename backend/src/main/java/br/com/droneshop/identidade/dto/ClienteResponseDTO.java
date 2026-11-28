package br.com.droneshop.identidade.dto;

import br.com.droneshop.identidade.model.TipoPessoa;

// id = id do usuário (o mesmo de /auth/eu)
public record ClienteResponseDTO(
    Long id,
    String nome,
    String email,
    TipoPessoa tipoPessoa,
    String documento,
    String telefone
) {
}
