package br.com.droneshop.identidade.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LoginResponseDTO(
        String token,
        LocalDateTime expiraEm,
        UsuarioResponseDTO usuario,
        List<String> perfis) {
}
