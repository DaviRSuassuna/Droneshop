package br.com.droneshop.catalogo.resource;

import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.com.droneshop.administracao.ModuloAdmin;
import br.com.droneshop.administracao.model.ModuloSistema;
import br.com.droneshop.catalogo.dto.MidiaProdutoOrdemRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoResponseDTO;
import br.com.droneshop.catalogo.mapper.MidiaProdutoResponseMapper;
import br.com.droneshop.catalogo.model.MidiaProduto;
import br.com.droneshop.catalogo.service.MidiaProdutoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("produtos/{id}/midias")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMINISTRATIVO")
@ModuloAdmin(ModuloSistema.CATALOGO)
public class ProdutoMidiaResource {

    @Inject
    MidiaProdutoService service;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public MidiaProdutoResponseDTO incluir(
            @PathParam("id") long idProduto,
            @RestForm("idTipo") Long idTipo,
            @RestForm("arquivo") FileUpload arquivo,
            @RestForm("url") String url,
            @RestForm("principal") Boolean principal,
            @RestForm("ordem") Integer ordem,
            @RestForm("altText") String altText) {
        return MidiaProdutoResponseMapper.toResponse(
                service.criar(idProduto, new MidiaProdutoRequestDTO(idTipo, arquivo, url, principal, ordem, altText)));
    }

    @GET
    public List<MidiaProdutoResponseDTO> listar(@PathParam("id") long idProduto) {
        List<MidiaProduto> midias = service.listar(idProduto);
        return MidiaProdutoResponseMapper.toResponse(midias);
    }

    @PATCH
    @Path("/ordem")
    @Consumes(MediaType.APPLICATION_JSON)
    public void reordenar(@PathParam("id") long idProduto, MidiaProdutoOrdemRequestDTO dto) {
        service.reordenar(idProduto, dto);
    }
}
