package br.com.droneshop.estoque.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.service.KitCalculo;
import br.com.droneshop.compra.model.ItemCarrinho;
import br.com.droneshop.estoque.model.Reserva;
import br.com.droneshop.estoque.model.StatusReserva;
import br.com.droneshop.estoque.model.TipoMovimento;
import br.com.droneshop.estoque.repository.ReservaRepository;
import br.com.droneshop.exception.ConflitoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ReservaServiceImpl implements ReservaService {

    @ConfigProperty(name = "aeria.reserva.minutos", defaultValue = "15")
    int minutos;

    @Inject
    ReservaRepository reservas;

    @Inject
    MovimentoEstoqueService movimentos;

    /** O que cada item prende do estoque: o próprio produto ou, no kit, cada componente (D7). */
    static Map<Produto, Integer> partes(ItemCarrinho item) {
        Map<Produto, Integer> partes = new LinkedHashMap<>();
        if (item.getProduto() instanceof Kit kit) {
            for (KitCalculo.Parte p : KitCalculo.partes(kit))
                partes.merge(p.produto(), p.quantidade() * item.getQuantidade(), Integer::sum);
        } else {
            partes.put(item.getProduto(), item.getQuantidade());
        }
        return partes;
    }

    @Override
    @Transactional
    public void reservar(List<ItemCarrinho> itens) {
        Map<Produto, Integer> precisa = new HashMap<>();
        Map<Long, Integer> jaMeu = new HashMap<>();
        for (ItemCarrinho item : itens) {
            partes(item).forEach((p, q) -> precisa.merge(p, q, Integer::sum));
            for (Reserva r : item.getReservas())
                if (r.getStatus() == StatusReserva.ATIVA)
                    jaMeu.merge(r.getProduto().getId(), r.getQuantidade(), Integer::sum);
        }
        for (Map.Entry<Produto, Integer> e : precisa.entrySet()) {
            Produto p = e.getKey();
            int livre = ReservaSaldo.disponivel(p) + jaMeu.getOrDefault(p.getId(), 0);
            if (e.getValue() > livre)
                throw new ConflitoException("Estoque insuficiente para " + p.getNome() + ": restam " + livre + ".");
        }

        LocalDateTime expiraEm = LocalDateTime.now().plusMinutes(minutos);
        for (ItemCarrinho item : itens) {
            Map<Produto, Integer> partes = partes(item);
            for (Reserva r : item.getReservas())
                if (r.getStatus() == StatusReserva.ATIVA && !partes.containsKey(r.getProduto()))
                    r.setStatus(StatusReserva.EXPIRADA);
            partes.forEach((p, q) -> {
                Reserva r = item.getReservas().stream()
                        .filter(x -> x.getStatus() == StatusReserva.ATIVA && x.getProduto().getId().equals(p.getId()))
                        .findFirst().orElse(null);
                if (r == null) {
                    r = new Reserva();
                    r.setProduto(p);
                    r.setItemCarrinho(item);
                    r.setStatus(StatusReserva.ATIVA);
                    item.getReservas().add(r);
                }
                r.setQuantidade(q);
                r.setExpiraEm(expiraEm);
                if (!reservas.isPersistent(r))
                    reservas.persist(r);
            });
        }
    }

    @Override
    @Transactional
    public void confirmar(List<ItemCarrinho> itens) {
        // a reserva vira CONFIRMADA e o físico baixa por SAIDA do sistema (RF14); reserva que
        // expirou antes do pagamento não impede a baixa
        for (ItemCarrinho item : itens)
            partes(item).forEach((p, q) -> {
                item.getReservas().stream()
                        .filter(r -> r.getStatus() == StatusReserva.ATIVA && r.getProduto().getId().equals(p.getId()))
                        .forEach(r -> r.setStatus(StatusReserva.CONFIRMADA));
                movimentos.doSistema(p, TipoMovimento.SAIDA, q, "Venda paga");
            });
    }

    @Override
    @Transactional
    public void liberar(List<ItemCarrinho> itens) {
        mudar(itens, StatusReserva.EXPIRADA);
    }

    private void mudar(List<ItemCarrinho> itens, StatusReserva novo) {
        for (ItemCarrinho item : itens)
            for (Reserva r : item.getReservas())
                if (r.getStatus() == StatusReserva.ATIVA)
                    r.setStatus(novo);
    }

    @Override
    @Transactional
    public int expirarVencidas(LocalDateTime referencia) {
        List<Reserva> vencidas = reservas.ativasVencidas(referencia);
        vencidas.forEach(r -> r.setStatus(StatusReserva.EXPIRADA));
        return vencidas.size();
    }
}
