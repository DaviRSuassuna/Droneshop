package br.com.droneshop.pedido.service;

import java.util.List;

import br.com.droneshop.pedido.dto.PedidoFiltro;
import br.com.droneshop.pedido.dto.ResumoStatusPedidoResponseDTO;
import br.com.droneshop.pedido.dto.StatusPedidoRequestDTO;
import br.com.droneshop.pedido.model.HistoricoStatusPedido;
import br.com.droneshop.pedido.model.Pedido;

// O pedido é criado pelo checkout (#26); aqui só muda de status (#17)
public interface PedidoService {

    // valida os parâmetros de GET /pedidos (400 por campo)
    default PedidoFiltro filtro(String idStatus, String dataInicio, String dataFim, String busca) {
        return filtro(idStatus, dataInicio, dataFim, busca, null);
    }
    // #89: busca também por CPF/CNPJ e filtro pelo método do pagamento vigente
    PedidoFiltro filtro(String idStatus, String dataInicio, String dataFim, String busca, String idMetodoPagamento);
    br.com.droneshop.pedido.dto.IndicadoresPedidoResponseDTO indicadores();
    List<Pedido> findComFiltro(PedidoFiltro filtro, int page, int pageSize);
    long countComFiltro(PedidoFiltro filtro);
    // os 8 status, na ordem do enum, inclusive os sem pedido
    List<ResumoStatusPedidoResponseDTO> resumoPorStatus();
    Pedido findById(long id);
    // 409 se a transição não for permitida; grava histórico MANUAL com o autor
    void alterarStatus(long id, StatusPedidoRequestDTO dto, long idAutor);
    // ordem cronológica
    List<HistoricoStatusPedido> historico(long id);

    // mudança feita pelo sistema ou por integração (D11: sem autor); dispara StatusPedidoAlterado
    void mudarSemAutor(Pedido pedido, br.com.droneshop.pedido.model.StatusPedido novo, br.com.droneshop.model.OrigemRegistro origem, String observacao);
}
