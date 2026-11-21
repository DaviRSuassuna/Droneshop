package br.com.droneshop.administracao.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import br.com.droneshop.administracao.dto.DoisFatoresResponseDTO;
import br.com.droneshop.administracao.model.PerfilAdministrativo;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;

/** 2FA opcional do perfil administrativo (RF44): gera, confirma e desliga. */
@ApplicationScoped
public class DoisFatoresService {

    @Inject
    UsuarioRepository usuarios;

    private PerfilAdministrativo perfil(Long idUsuario) {
        Usuario u = usuarios.findById(idUsuario);
        if (u == null || u.getPerfilAdministrativo() == null)
            throw new ForbiddenException("Usuário sem perfil administrativo.");
        return u.getPerfilAdministrativo();
    }

    /** Gera o segredo (ainda desligado); com 2FA ligado, 409: o segredo nunca volta. */
    @Transactional
    public DoisFatoresResponseDTO gerar(Long idUsuario) {
        PerfilAdministrativo p = perfil(idUsuario);
        if (p.isTwoFAHabilitado())
            throw new ConflitoException("O 2FA já está ligado.");
        String segredo = Totp.novoSegredo();
        p.setTwoFASecret(segredo);
        String conta = URLEncoder.encode(p.getUsuario().getEmail(), StandardCharsets.UTF_8);
        return new DoisFatoresResponseDTO(segredo,
                "otpauth://totp/Aeria:" + conta + "?secret=" + segredo + "&issuer=Aeria&digits=6&period=30");
    }

    @Transactional
    public void confirmar(Long idUsuario, String codigo) {
        PerfilAdministrativo p = perfil(idUsuario);
        if (p.isTwoFAHabilitado())
            throw new ConflitoException("O 2FA já está ligado.");
        if (!Totp.valido(p.getTwoFASecret(), codigo))
            throw ValidationException.of("codigo", "Código inválido (back)");
        p.setTwoFAHabilitado(true);
    }

    @Transactional
    public void desligar(Long idUsuario, String codigo) {
        PerfilAdministrativo p = perfil(idUsuario);
        if (!p.isTwoFAHabilitado())
            throw new ConflitoException("O 2FA não está ligado.");
        if (!Totp.valido(p.getTwoFASecret(), codigo))
            throw ValidationException.of("codigo", "Código inválido (back)");
        p.setTwoFAHabilitado(false);
        p.setTwoFASecret(null);
    }

    /** Usado no login: o usuário tem 2FA ligado? */
    public static boolean exige(Usuario u) {
        return u.getPerfilAdministrativo() != null && u.getPerfilAdministrativo().isTwoFAHabilitado();
    }

    public static boolean codigoValido(Usuario u, String codigo) {
        return exige(u) && Totp.valido(u.getPerfilAdministrativo().getTwoFASecret(), codigo);
    }
}
