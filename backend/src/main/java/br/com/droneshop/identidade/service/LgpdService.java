package br.com.droneshop.identidade.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.dto.ConsentimentoRequestDTO;
import br.com.droneshop.identidade.dto.TermoResponseDTO;
import br.com.droneshop.identidade.model.ConsentimentoLGPD;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.ConsentimentoRepository;
import br.com.droneshop.identidade.repository.EnderecoClienteRepository;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import br.com.droneshop.identidade.client.KeycloakClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** Consentimentos LGPD e exclusão com anonimização (RF51, RNF03, #46). */
@ApplicationScoped
public class LgpdService {

    public static final String TERMOS_USO = "TERMOS_USO";
    public static final String POLITICA_PRIVACIDADE = "POLITICA_PRIVACIDADE";
    public static final String MARKETING = "MARKETING";

    /** Versões vigentes; mudar a versão aqui exige novo aceite na criação de conta. */
    static final Map<String, String> VIGENTES = Map.of(TERMOS_USO, "2026-10", POLITICA_PRIVACIDADE, "2026-10",
            MARKETING, "2026-10");
    static final List<String> OBRIGATORIOS = List.of(TERMOS_USO, POLITICA_PRIVACIDADE);

    @Inject
    ConsentimentoRepository consentimentos;

    @Inject
    UsuarioRepository usuarios;

    @Inject
    EnderecoClienteRepository enderecos;

    @Inject
    KeycloakClient keycloak;

    public List<TermoResponseDTO> termos() {
        return List.of(new TermoResponseDTO(TERMOS_USO, VIGENTES.get(TERMOS_USO), true),
                new TermoResponseDTO(POLITICA_PRIVACIDADE, VIGENTES.get(POLITICA_PRIVACIDADE), true),
                new TermoResponseDTO(MARKETING, VIGENTES.get(MARKETING), false));
    }

    private static boolean vigente(ConsentimentoRequestDTO c) {
        return c != null && c.tipo() != null && c.versao() != null && c.versao().equals(VIGENTES.get(c.tipo()));
    }

    /** Criar conta exige termos e política na versão vigente. */
    public void conferirObrigatorios(List<ConsentimentoRequestDTO> lista) {
        List<ConsentimentoRequestDTO> l = lista == null ? List.of() : lista;
        for (String tipo : OBRIGATORIOS)
            if (l.stream().noneMatch(c -> tipo.equals(c.tipo()) && vigente(c)))
                throw ValidationException.of("consentimentos",
                        "Aceite os termos de uso e a política de privacidade vigentes (back)");
    }

    @Transactional
    public void registrar(Usuario usuario, List<ConsentimentoRequestDTO> lista) {
        for (ConsentimentoRequestDTO c : lista == null ? List.<ConsentimentoRequestDTO>of() : lista)
            registrar(usuario, c);
    }

    @Transactional
    public void registrar(Usuario usuario, ConsentimentoRequestDTO c) {
        if (!vigente(c))
            throw ValidationException.of("versao", "Tipo ou versão de termo desconhecidos (back)");
        ConsentimentoLGPD n = new ConsentimentoLGPD();
        n.setUsuario(usuario);
        n.setTipo(c.tipo());
        n.setVersao(c.versao());
        n.setDataConsentimento(LocalDateTime.now());
        consentimentos.persist(n);
    }

    @Transactional
    public void consentir(Long idUsuario, ConsentimentoRequestDTO c) {
        registrar(usuarios.findById(idUsuario), c);
    }

    public List<ConsentimentoLGPD> doUsuario(Long idUsuario) {
        return consentimentos.doUsuario(idUsuario);
    }

    /** Anonimiza os dados pessoais e revoga as sessões; pedidos ficam íntegros. */
    @Transactional
    public void excluir(Long idUsuario, String senha) {
        Usuario u = usuarios.findById(idUsuario);
        if (u == null || !keycloak.confirmarSenha(u.getEmail(), senha))
            throw ValidationException.of("senha", "Senha incorreta (back)");
        u.setNome("Cliente removido");
        u.setEmail("removido-" + u.getId() + "@aeria.invalid");
        u.setAtivo(false);
        PerfilCliente p = u.getPerfilCliente();
        if (p != null) {
            p.setNome("Cliente removido");
            p.setDocumento(null);
            p.setTelefone(null);
            enderecos.delete("perfilCliente", p);
        }
        usuarios.flush();
        keycloak.remover(u.getKeycloakSub());
    }
}
