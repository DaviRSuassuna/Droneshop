package br.com.droneshop.identidade.dto;

import java.time.LocalDateTime;

import br.com.droneshop.identidade.model.PerfilAtivo;

public record SessaoResponseDTO(Long id, PerfilAtivo perfilAtivo, LocalDateTime dataCadastro,
        LocalDateTime expiraEm, boolean atual) {
}
