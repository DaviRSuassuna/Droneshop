package br.com.droneshop.identidade.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String emissor;

    @Inject
    UsuarioRepository usuarios;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarios.porEmail(dto.email());
        if (usuario == null || !usuario.isAtivo() || usuario.getSenhaHash() == null
                || !BcryptUtil.matches(dto.senha(), usuario.getSenhaHash())) {
            throw new NotAuthorizedException(ERRO_LOGIN, "Bearer");
        }

        LocalDateTime expiraEm = LocalDateTime.now().plus(DURACAO_TOKEN);
        List<String> perfis = perfisDo(usuario);
        String token = Jwt.issuer(emissor)
                .subject(usuario.getId().toString())
                .upn(usuario.getEmail())
                .groups(Set.copyOf(perfis))
                .expiresIn(DURACAO_TOKEN)
                .sign();

        return new LoginResponseDTO(token, expiraEm,
                new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), perfis), perfis);
    }

    @Transactional
    public UsuarioResponseDTO usuarioAtivo(Long id) {
        Usuario usuario = usuarios.findById(id);
        if (usuario == null || !usuario.isAtivo()) {
            throw new NotAuthorizedException(ERRO_LOGIN, "Bearer");
        }
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), perfisDo(usuario));
    }

    private List<String> perfisDo(Usuario usuario) {
        List<String> perfis = new ArrayList<>();
        if (usuario.getPerfilCliente() != null)
            perfis.add("CLIENTE");
        if (usuario.getPerfilAdministrativo() != null)
            perfis.add("ADMINISTRATIVO");
        return List.copyOf(perfis);
    }
}
