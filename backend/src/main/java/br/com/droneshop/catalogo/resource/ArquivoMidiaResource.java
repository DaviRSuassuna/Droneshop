package br.com.droneshop.catalogo.resource;

import java.io.IOException;

import br.com.droneshop.catalogo.service.MidiaProdutoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

// Publico: a vitrine e o <img> do admin carregam o arquivo sem token.
@Path("midias/arquivos")
public class ArquivoMidiaResource {

    @Inject
    MidiaProdutoService service;

    @GET
    @Path("/{nome}")
    @Produces({ "image/png", "image/jpeg", "application/pdf" })
    public Response arquivo(@PathParam("nome") String nome) throws IOException {
        MidiaProdutoService.ArquivoMidia arquivo = service.buscarArquivo(nome);
        return Response.ok(arquivo.conteudo(), arquivo.contentType())
                .header("X-Content-Type-Options", "nosniff")
                .build();
    }
}
