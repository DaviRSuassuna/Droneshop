package br.com.droneshop.pedido.resource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.compra.model.Carrinho;
import br.com.droneshop.compra.model.ItemCarrinho;
import br.com.droneshop.compra.resource.CarrinhoResource;
import br.com.droneshop.compra.service.CarrinhoService;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.pedido.dto.CotacaoFreteRequestDTO;
import br.com.droneshop.pedido.dto.CotacaoFreteResponseDTO;
import br.com.droneshop.pedido.service.FreteService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Cotação de frete pública: itens no corpo ou o carrinho do header (#30). */
@Path("frete/cotacao")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FreteResource {

    @Inject
    FreteService frete;

    @Inject
    ProdutoRepository produtos;

    @Inject
    CarrinhoService carrinhos;

    @POST
    @Transactional
    public List<CotacaoFreteResponseDTO> cotar(@Valid CotacaoFreteRequestDTO dto,
            @HeaderParam(CarrinhoResource.HEADER) String tokenCarrinho) {
        Map<Produto, Integer> itens = new LinkedHashMap<>();
        if (dto.itens() != null && !dto.itens().isEmpty()) {
            for (CotacaoFreteRequestDTO.Item i : dto.itens()) {
                Produto p = produtos.findById(i.idProduto());
                if (p == null)
                    throw ValidationException.of("itens", "Produto não encontrado (back)");
                itens.merge(p, i.quantidade(), Integer::sum);
            }
        } else if (tokenCarrinho != null && !tokenCarrinho.isBlank()) {
            Carrinho c = carrinhos.ativo(tokenCarrinho);
            for (ItemCarrinho i : c.getItens())
                itens.merge(i.getProduto(), i.getQuantidade(), Integer::sum);
        }
        return frete.cotar(dto.cep(), itens);
    }
}
