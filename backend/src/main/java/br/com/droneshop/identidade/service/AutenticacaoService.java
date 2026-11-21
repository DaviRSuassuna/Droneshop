package br.com.droneshop.identidade.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.com.droneshop.administracao.service.DoisFatoresService;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.dto.LoginRequestDTO;
import br.com.droneshop.identidade.dto.LoginResponseDTO;
import br.com.droneshop.identidade.dto.SessaoResponseDTO;
import br.com.droneshop.identidade.dto.UsuarioResponseDTO;
import br.com.droneshop.identidade.model.PerfilAtivo;
import br.com.droneshop.identidade.model.SessaoUsuario;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.SessaoUsuarioRepository;
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
    private static final String ERRO_2FA = "Código de verificação inválido.";
    private static final Duration DURACAO_2FA = Duration.ofMinutes(5);

    /** Login que espera o código do 2FA: o token temporário só vale aqui, por 5 minutos (#45). */
    private record Pendente2fa(Long idUsuario, LocalDateTime expiraEm) {
    }

    private final Map<String, Pendente2fa> pendentes2fa = new ConcurrentHashMap<>();

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String emissor;

    @Inject
    UsuarioRepository usuarios;

    @Inject
    SessaoUsuarioRepository sessoes;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarios.porEmail(dto.email());
        if (usuario == null || !usuario.isAtivo() || usuario.getSenhaHash() == null
                || !BcryptUtil.matches(dto.senha(), usuario.getSenhaHash())) {
            throw new NotAuthorizedException(ERRO_LOGIN, "Bearer");
        }
        if (DoisFatoresService.exige(usuario)) {
            String temporario = UUID.randomUUID().toString();
            pendentes2fa.put(temporario, new Pendente2fa(usuario.getId(), LocalDateTime.now().plus(DURACAO_2FA)));
            return new LoginResponseDTO(null, null, null, perfisDo(usuario), null, false, true, temporario);
        }
        return abrirSessao(usuario);
    }

    /** Segunda etapa do login com 2FA: token temporário + código TOTP (#45). */
    @Transactional
    public LoginResponseDTO login2fa(String tokenTemporario, String codigo) {
        Pendente2fa p = tokenTemporario == null ? null : pendentes2fa.get(tokenTemporario);
        if (p == null || p.expiraEm().isBefore(LocalDateTime.now())) {
            if (tokenTemporario != null)
                pendentes2fa.remove(tokenTemporario);
            throw new NotAuthorizedException(ERRO_2FA, "Bearer");
        }
        Usuario usuario = usuarios.findById(p.idUsuario());
        if (usuario == null || !usuario.isAtivo() || !DoisFatoresService.codigoValido(usuario, codigo))
            throw new NotAuthorizedException(ERRO_2FA, "Bearer");
        pendentes2fa.remove(tokenTemporario);
        return abrirSessao(usuario);
    }

    /** Abre a sessão do usuário já autenticado (senha, Google...). */
    @Transactional
    public LoginResponseDTO abrirSessao(Usuario usuario) {
        List<String> perfis = perfisDo(usuario);
        PerfilAtivo ativo = perfis.size() == 1 ? PerfilAtivo.valueOf(perfis.get(0)) : null;

        SessaoUsuario sessao = new SessaoUsuario();
        sessao.setUsuario(usuario);
        sessao.setPerfilAtivo(ativo);
        sessao.setExpiraEm(LocalDateTime.now().plus(DURACAO_TOKEN));
        sessoes.persist(sessao);
        return resposta(usuario, sessao);
    }

    @Transactional
    public LoginResponseDTO escolherPerfil(Long idUsuario, Long idSessao, Long idPerfilAtivo) {
        SessaoUsuario sessao = sessaoDo(idUsuario, idSessao);
        PerfilAtivo perfil = PerfilAtivo.valueOf(idPerfilAtivo);
        if (perfil == null || !perfisDo(sessao.getUsuario()).contains(perfil.getNome()))
            throw ValidationException.of("idPerfilAtivo", "Você não tem esse perfil (back)");
        sessao.setPerfilAtivo(perfil);
        return resposta(sessao.getUsuario(), sessao);
    }

    @Transactional
    public UsuarioResponseDTO usuarioAtivo(Long id) {
        Usuario usuario = usuarios.findById(id);
        if (usuario == null || !usuario.isAtivo()) {
            throw new NotAuthorizedException(ERRO_LOGIN, "Bearer");
        }
        return new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), perfisDo(usuario));
    }

    /** Sessão existe, é do usuário, não foi revogada e não expirou. */
    @Transactional
    public boolean sessaoValida(Long idSessao) {
        SessaoUsuario s = sessoes.findById(idSessao);
        return s != null && s.valida() && s.getUsuario().isAtivo();
    }

    @Transactional
    public List<SessaoResponseDTO> sessoesDo(Long idUsuario, Long idSessaoAtual) {
        return sessoes.ativasDo(idUsuario).stream()
                .map(s -> new SessaoResponseDTO(s.getId(), s.getPerfilAtivo(), s.getDataCadastro(),
                        s.getExpiraEm(), s.getId().equals(idSessaoAtual)))
                .toList();
    }

    @Transactional
    public void revogar(Long idUsuario, Long idSessao) {
        SessaoUsuario s = sessaoDo(idUsuario, idSessao);
        if (s.getRevogadaEm() == null)
            s.setRevogadaEm(LocalDateTime.now());
    }

    /** Revoga todas as sessões do usuário (troca de senha, exclusão de conta). */
    @Transactional
    public void revogarTodas(Long idUsuario) {
        sessoes.update("revogadaEm = ?1 where usuario.id = ?2 and revogadaEm is null", LocalDateTime.now(), idUsuario);
    }

    private SessaoUsuario sessaoDo(Long idUsuario, Long idSessao) {
        SessaoUsuario s = idSessao == null ? null : sessoes.findById(idSessao);
        if (s == null || !s.getUsuario().getId().equals(idUsuario))
            throw new ResourceNotFoundException("Sessão não encontrada.");
        return s;
    }

    private LoginResponseDTO resposta(Usuario usuario, SessaoUsuario sessao) {
        List<String> perfis = perfisDo(usuario);
        PerfilAtivo ativo = sessao.getPerfilAtivo();
        Duration resta = Duration.between(LocalDateTime.now(), sessao.getExpiraEm());
        String token = Jwt.issuer(emissor)
                .subject(usuario.getId().toString())
                .upn(usuario.getEmail())
                .groups(ativo == null ? Set.of() : Set.of(ativo.getNome()))
                .claim("sid", sessao.getId())
                .expiresIn(resta.isNegative() ? Duration.ZERO : resta)
                .sign();
        return new LoginResponseDTO(token, sessao.getExpiraEm(),
                new UsuarioResponseDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(), perfis), perfis,
                ativo == null ? null : ativo.getNome(), ativo == null, false, null);
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
