package br.com.droneshop.catalogo.resource;

import java.io.IOException;
import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.com.droneshop.catalogo.dto.MidiaProdutoAlteracaoRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoOrdemRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoRequestDTO;
import br.com.droneshop.catalogo.dto.MidiaProdutoResponseDTO;
import br.com.droneshop.catalogo.mapper.MidiaProdutoResponseMapper;
import br.com.droneshop.catalogo.model.MidiaProduto;
import br.com.droneshop.catalogo.service.MidiaProdutoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("midias")
@Produces(MediaType.APPLICATION_JSON)
public class MidiaProdutoResource {

    @Inject
    MidiaProdutoService service;

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void alterar(@PathParam("id") long id, MidiaProdutoAlteracaoRequestDTO dto) {
        service.alterar(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void excluir(@PathParam("id") long id) {
        service.excluir(id);
    }

    @GET
    @Path("/arquivos/{nome}")
    @Produces({ "image/png", "image/jpeg", "application/pdf" })
    public Response arquivo(@PathParam("nome") String nome) throws IOException {
        MidiaProdutoService.ArquivoMidia arquivo = service.buscarArquivo(nome);
        return Response.ok(arquivo.conteudo(), arquivo.contentType())
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }
}
