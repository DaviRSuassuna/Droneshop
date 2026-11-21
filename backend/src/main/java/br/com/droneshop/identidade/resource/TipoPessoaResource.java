package br.com.droneshop.identidade.resource;

import br.com.droneshop.identidade.model.TipoPessoa;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Público: a tela de criar conta precisa da lista antes do login (D31).
@Path("tipos-pessoa")
@Produces(MediaType.APPLICATION_JSON)
public class TipoPessoaResource {

    @GET
    public TipoPessoa[] buscarTodos() {
        return TipoPessoa.values();
    }
}
