package br.com.droneshop.notificacao.resource;

import java.util.List;

import br.com.droneshop.identidade.resource.UsuarioAtual;

import br.com.droneshop.notificacao.dto.PreferenciaNotificacaoDTO;
import br.com.droneshop.notificacao.service.PreferenciaNotificacaoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Preferências de notificação do cliente (#42, RF41). */
@Path("conta/preferencias-notificacao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("CLIENTE")
public class PreferenciaNotificacaoResource {

    @Inject
    PreferenciaNotificacaoService service;

    @Inject
    UsuarioAtual usuarioAtual;

    @GET
    public List<PreferenciaNotificacaoDTO> listar() {
        return service.doUsuario(usuarioAtual.id());
    }

    @PUT
    public void salvar(List<@Valid PreferenciaNotificacaoDTO> preferencias) {
        service.salvar(usuarioAtual.id(), preferencias);
    }
}
