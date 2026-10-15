package br.com.droneshop.identidade.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.identidade.dto.LoginRequestDTO;
import br.com.droneshop.identidade.dto.LoginResponseDTO;
import br.com.droneshop.identidade.dto.UsuarioResponseDTO;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;

@ApplicationScoped
public class AutenticacaoService {

    private static final Duration DURACAO_TOKEN = Duration.ofHours(1);
    private static final String ERRO_LOGIN = "E-mail ou senha inválidos.";
    private static final String EMISSOR = "droneshop-api";

    @Inject
    UsuarioRepository usuarios;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarios.porEmail(dto.email());
        if (usuario == null || !usuario.isAtivo() || usuario.getSenhaHash() == null
                || !BcryptUtil.matches(dto.senha(), usuario.getSenhaHash())) {
            throw new NotAuthorizedException(ERRO_LOGIN);
        }

        LocalDateTime expiraEm = LocalDateTime.now().plus(DURACAO_TOKEN);
        String token = Jwt.issuer(EMISSOR)
                .subject(usuario.getId().toString())
                .upn(usuario.getEmail())
                .groups(usuario.getPerfis())
                .expiresIn(DURACAO_TOKEN)
                .sign();

        List<String> perfis = usuario.getPerfis().stream().sorted().toList();
        return new LoginResponseDTO(token, expiraEm,
                new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), perfis), perfis);
    }

    @Transactional
    public UsuarioResponseDTO usuarioAtivo(Long id) {
        Usuario usuario = usuarios.findById(id);
        if (usuario == null || !usuario.isAtivo()) {
            throw new NotAuthorizedException(ERRO_LOGIN);
        }
        List<String> perfis = usuario.getPerfis().stream().sorted().toList();
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), perfis);
    }
}
