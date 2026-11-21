package br.com.droneshop.identidade.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.com.droneshop.identidade.client.GoogleClient;
import br.com.droneshop.identidade.dto.LoginResponseDTO;
import br.com.droneshop.identidade.model.ContaSocial;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.ProvedorSocial;
import br.com.droneshop.identidade.model.TipoPessoa;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.ContaSocialRepository;
import br.com.droneshop.identidade.repository.PerfilClienteRepository;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

/**
 * Login com Google (RF47, D6, D37): o ID token é validado no Google; o identificador é o sub.
 * Conta nova só-Google não tem senha; conta local com o mesmo e-mail é vinculada.
 */
@ApplicationScoped
public class GoogleService {

    private static final Logger LOG = Logger.getLogger(GoogleService.class);
    private static final String INVALIDO = "Não foi possível entrar com o Google.";

    @Inject
    @RestClient
    GoogleClient google;

    @ConfigProperty(name = "aeria.google.client-id")
    Optional<String> clientId;

    @Inject
    UsuarioRepository usuarios;

    @Inject
    PerfilClienteRepository perfis;

    @Inject
    ContaSocialRepository contas;

    @Inject
    AutenticacaoService autenticacao;

    @Transactional
    public LoginResponseDTO entrar(String idToken) {
        GoogleClient.TokenInfo info;
        try {
            info = google.tokeninfo(idToken);
        } catch (WebApplicationException | ProcessingException e) {
            LOG.warnf("ID token do Google recusado: %s", e.getMessage());
            throw new NotAuthorizedException(INVALIDO, "Bearer");
        }
        if (info == null || info.sub() == null || info.email() == null || !"true".equals(info.emailVerified()))
            throw new NotAuthorizedException(INVALIDO, "Bearer");
        if (clientId.isPresent() && !clientId.get().isBlank() && !clientId.get().equals(info.aud()))
            throw new NotAuthorizedException(INVALIDO, "Bearer");

        ContaSocial conta = contas.porSub(ProvedorSocial.GOOGLE, info.sub());
        Usuario usuario;
        if (conta != null) {
            usuario = conta.getUsuario();
        } else {
            usuario = usuarios.porEmail(info.email());
            if (usuario == null)
                usuario = novoCliente(info);
            ContaSocial nova = new ContaSocial();
            nova.setUsuario(usuario);
            nova.setProvedor(ProvedorSocial.GOOGLE);
            nova.setIdentificadorExterno(info.sub());
            contas.persist(nova);
        }
        if (!usuario.isAtivo())
            throw new NotAuthorizedException(INVALIDO, "Bearer");
        return autenticacao.abrirSessao(usuario);
    }

    // Conta só-Google: sem senha nem documento; o cliente completa o cadastro depois
    private Usuario novoCliente(GoogleClient.TokenInfo info) {
        String nome = info.name() == null || info.name().isBlank() ? info.email().substring(0, info.email().indexOf('@'))
                : info.name().trim();
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(info.email());
        u.setEmailVerificado(true);
        u.setAtivo(true);
        usuarios.persist(u);
        PerfilCliente p = new PerfilCliente();
        p.setUsuario(u);
        p.setNome(nome);
        p.setTipoPessoa(TipoPessoa.PF);
        perfis.persist(p);
        u.setPerfilCliente(p);
        return u;
    }

    public List<ContaSocial> doUsuario(Long idUsuario) {
        return contas.doUsuario(idUsuario);
    }
}
