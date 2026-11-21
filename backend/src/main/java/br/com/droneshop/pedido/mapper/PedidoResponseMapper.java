package br.com.droneshop.pedido.mapper;

import java.math.BigDecimal;
import java.util.List;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.model.OrigemRegistro;
import br.com.droneshop.pedido.dto.ClientePedidoResponseDTO;
import br.com.droneshop.pedido.dto.EnderecoPedidoResponseDTO;
import br.com.droneshop.pedido.dto.HistoricoStatusPedidoResponseDTO;
import br.com.droneshop.pedido.dto.ItemPedidoResponseDTO;
import br.com.droneshop.pedido.dto.PedidoListagemResponseDTO;
import br.com.droneshop.pedido.dto.PedidoResponseDTO;
import br.com.droneshop.pedido.model.EnderecoPedido;
import br.com.droneshop.pedido.model.HistoricoStatusPedido;
import br.com.droneshop.pedido.model.ItemPedido;
import br.com.droneshop.pedido.model.Pedido;

public final class PedidoResponseMapper {

    private PedidoResponseMapper() {
    }

    public static String numero(Long id) {
        return String.format("AER-%06d", id);
    }

    public static PedidoListagemResponseDTO toListagem(Pedido p) {
        return new PedidoListagemResponseDTO(
                p.getId(),
                numero(p.getId()),
                p.getStatus(),
                cliente(p.getCliente()),
                p.getValorTotal(),
                p.getItens().size(),
                p.getDataCadastro());
    }

    public static PedidoResponseDTO toResponse(Pedido p) {
        return new PedidoResponseDTO(
                p.getId(),
                numero(p.getId()),
                p.getStatus(),
                cliente(p.getCliente()),
                p.getValorTotal(),
                p.getValorFrete(),
                p.getValorDesconto(),
                p.getItens().stream().map(PedidoResponseMapper::item).toList(),
                endereco(p.getEnderecoEntrega()),
                List.of(),
                null,
                p.getDataCadastro());
    }

    public static HistoricoStatusPedidoResponseDTO toHistorico(HistoricoStatusPedido h) {
        Usuario autor = h.getOrigem() == OrigemRegistro.MANUAL ? h.getAutor() : null;
        return new HistoricoStatusPedidoResponseDTO(
                h.getStatusAnterior(),
                h.getStatusNovo(),
                h.getOrigem(),
                autor == null ? null : new HistoricoStatusPedidoResponseDTO.AutorResumo(autor.getId(), autor.getNome()),
                h.getObservacao(),
                h.getDataCadastro());
    }

    static ItemPedidoResponseDTO item(ItemPedido i) {
        Produto p = i.getProduto();
        return new ItemPedidoResponseDTO(
                i.getId(),
                new ItemPedidoResponseDTO.ProdutoResumo(p.getId(), p.getSku(), p.getNome(), p.getImagemPrincipal()),
                i.getQuantidade(),
                i.getPrecoUnitarioCongelado(),
                i.getPrecoUnitarioCongelado().multiply(BigDecimal.valueOf(i.getQuantidade())));
    }

    static ClientePedidoResponseDTO cliente(PerfilCliente c) {
        return new ClientePedidoResponseDTO(c.getId(), c.getNome());
    }

    static EnderecoPedidoResponseDTO endereco(EnderecoPedido e) {
        if (e == null)
            return null;
        return new EnderecoPedidoResponseDTO(e.getDestinatario(), e.getLogradouro(), e.getNumero(),
                e.getComplemento(), e.getBairro(), e.getCidade(), e.getUf(), e.getCep());
    }
}
