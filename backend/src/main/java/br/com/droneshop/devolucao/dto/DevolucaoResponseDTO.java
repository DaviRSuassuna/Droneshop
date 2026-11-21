package br.com.droneshop.devolucao.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.devolucao.model.Devolucao;
import br.com.droneshop.devolucao.model.ResolucaoDevolucao;
import br.com.droneshop.devolucao.model.StatusDevolucao;
import br.com.droneshop.model.OrigemRegistro;
import br.com.droneshop.pedido.mapper.PedidoResponseMapper;

public record DevolucaoResponseDTO(Long id, PedidoRef pedido, StatusDevolucao status, ResolucaoDevolucao resolucao,
        String motivo, LocalDateTime dataSolicitacao, LocalDateTime dataConclusao, List<Item> itens,
        List<Historico> historico) {

    public record PedidoRef(Long id, String numero) {
    }

    public record Item(Long id, Long idItemPedido, String produto, int quantidade) {
    }

    public record Autor(Long id, String nome) {
    }

    public record Historico(StatusDevolucao statusAnterior, StatusDevolucao statusNovo, OrigemRegistro origem,
            Autor autor, String observacao, LocalDateTime dataCadastro) {
    }

    public static DevolucaoResponseDTO de(Devolucao d) {
        return new DevolucaoResponseDTO(d.getId(),
                new PedidoRef(d.getPedido().getId(), PedidoResponseMapper.numero(d.getPedido().getId())),
                d.getStatus(), d.getResolucao(), d.getMotivo(), d.getDataSolicitacao(), d.getDataConclusao(),
                d.getItens().stream().map(i -> new Item(i.getId(), i.getItemPedido().getId(),
                        i.getItemPedido().getProduto().getNome(), i.getQuantidade())).toList(),
                d.getHistorico().stream().map(h -> new Historico(h.getStatusAnterior(), h.getStatusNovo(),
                        h.getOrigem(), h.getAutor() == null ? null : new Autor(h.getAutor().getId(), h.getAutor().getNome()),
                        h.getObservacao(), h.getDataCadastro())).toList());
    }
}
