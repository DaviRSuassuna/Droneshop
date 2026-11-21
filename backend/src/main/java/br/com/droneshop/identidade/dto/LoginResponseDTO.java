package br.com.droneshop.identidade.dto;

import java.time.LocalDateTime;
import java.util.List;

/** perfilAtivo null e escolherPerfil true: quem tem os dois perfis escolhe em /auth/perfil-ativo (#20). */
public record LoginResponseDTO(
        String token,
        LocalDateTime expiraEm,
        UsuarioResponseDTO usuario,
        List<String> perfis,
        String perfilAtivo,
        boolean escolherPerfil,
        // 2FA ligado (#45): token nulo; o código vai em /auth/login/2fa com o tokenTemporario
        boolean precisa2fa,
        String tokenTemporario) {
}
