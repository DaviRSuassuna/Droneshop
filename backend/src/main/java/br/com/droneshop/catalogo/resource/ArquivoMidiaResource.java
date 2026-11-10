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
    @Produces({ "image/png", "image/jpeg", "image/svg+xml", "application/pdf" })
    public Response arquivo(@PathParam("nome") String nome) throws IOException {
        MidiaProdutoService.ArquivoMidia arquivo = service.buscarArquivo(nome);
        Response.ResponseBuilder resposta = Response.ok(arquivo.conteudo(), arquivo.contentType())
                .header("X-Content-Type-Options", "nosniff");
        // O SVG do logo da marca (#84) pode conter script: a CSP impede que ele rode
        // quando o arquivo e aberto direto no navegador (no <img> script nunca roda)
        if (arquivo.contentType().equals("image/svg+xml"))
            resposta.header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox");
        return resposta.build();
    }
}
