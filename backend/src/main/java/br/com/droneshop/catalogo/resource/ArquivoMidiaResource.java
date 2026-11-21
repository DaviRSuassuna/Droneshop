package br.com.droneshop.catalogo.resource;

import br.com.droneshop.catalogo.service.MidiaProdutoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

// Publico: a vitrine e o <img> do admin carregam o arquivo sem token. O bucket e privado (D42):
// a API le o objeto e devolve; ?download=true baixa com Content-Disposition: attachment.
@Path("midias/arquivos")
public class ArquivoMidiaResource {

    @Inject
    MidiaProdutoService service;

    @GET
    @Path("/{nome}")
    @Produces({ "image/png", "image/jpeg", "image/webp", "application/pdf" })
    public Response arquivo(@PathParam("nome") String nome, @QueryParam("download") boolean download) {
        MidiaProdutoService.ArquivoMidia arquivo = service.buscarArquivo(nome);
        Response.ResponseBuilder resposta = Response.ok(arquivo.conteudo(), arquivo.contentType())
                .header("X-Content-Type-Options", "nosniff");
        // o nome ja passou pela expressao do service (uuid.ext), sem aspas nem barras
        resposta.header("Content-Disposition", (download ? "attachment" : "inline") + "; filename=\"" + nome + "\"");
        return resposta.build();
    }
}
