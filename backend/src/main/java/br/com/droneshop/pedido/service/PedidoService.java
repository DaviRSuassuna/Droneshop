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
    PedidoFiltro filtro(String idStatus, String dataInicio, String dataFim, String busca);
    List<Pedido> findComFiltro(PedidoFiltro filtro, int page, int pageSize);
    long countComFiltro(PedidoFiltro filtro);
    // os 8 status, na ordem do enum, inclusive os sem pedido
    List<ResumoStatusPedidoResponseDTO> resumoPorStatus();
    Pedido findById(long id);
    // 409 se a transição não for permitida; grava histórico MANUAL com o autor
    void alterarStatus(long id, StatusPedidoRequestDTO dto, long idAutor);
    // ordem cronológica
    List<HistoricoStatusPedido> historico(long id);

}
