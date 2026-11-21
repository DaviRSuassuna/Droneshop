package br.com.droneshop.catalogo.resource;

import java.util.List;

import br.com.droneshop.catalogo.dto.OfertaLojaResponseDTO;
import br.com.droneshop.catalogo.dto.ProdutoLojaResumoDTO;
import br.com.droneshop.catalogo.service.LojaComplementoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("loja")
@Produces(MediaType.APPLICATION_JSON)
public class LojaComplementoResource {

    @Inject
    LojaComplementoService service;

    @GET
    @Path("/produtos/{slug}/compativeis")
    public List<ProdutoLojaResumoDTO> buscarCompativeis(@PathParam("slug") String slug) {
        return service.buscarCompativeis(slug);
    }

    @GET
    @Path("/ofertas")
    public List<OfertaLojaResponseDTO> buscarOfertas() {
        return service.buscarOfertas();
    }
}
