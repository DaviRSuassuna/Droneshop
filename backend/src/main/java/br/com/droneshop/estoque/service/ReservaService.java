package br.com.droneshop.estoque.service;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.compra.model.ItemCarrinho;

public interface ReservaService {

    /** Reserva (ou renova) todos os itens; 409 se algum passar do disponível, sem reservar nada. */
    void reservar(List<ItemCarrinho> itens);

    /** Pagamento aprovado: as reservas ATIVA dos itens viram CONFIRMADA (#27). */
    void confirmar(List<ItemCarrinho> itens);

    /** Libera as reservas ATIVA dos itens (pedido cancelado, carrinho expirado). */
    void liberar(List<ItemCarrinho> itens);

    int expirarVencidas(LocalDateTime referencia);
}
