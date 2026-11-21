package br.com.droneshop.estoque.service;

import java.util.List;

import br.com.droneshop.catalogo.model.Kit;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.service.KitCalculo;
import br.com.droneshop.estoque.repository.ReservaRepository;
import io.quarkus.arc.Arc;

/**
 * Disponível = físico - reservas ATIVA (#23). No kit, o menor (disponível do componente /
 * quantidade exigida); componente que não está ATIVO conta como zero (D7).
 */
public final class ReservaSaldo {

    private ReservaSaldo() {
    }

    private static ReservaRepository reservas() {
        return Arc.container().instance(ReservaRepository.class).get();
    }

    public static int reservado(Produto p) {
        return p instanceof Kit ? 0 : reservas().reservadoAtivo(p.getId());
    }

    public static int disponivel(Produto p) {
        if (!(p instanceof Kit kit)) {
            int fisico = p.getEstoqueFisico() == null ? 0 : p.getEstoqueFisico();
            return Math.max(fisico - reservado(p), 0);
        }
        List<KitCalculo.Parte> partes = KitCalculo.partes(kit);
        if (partes.isEmpty())
            return 0;
        int menor = Integer.MAX_VALUE;
        for (KitCalculo.Parte parte : partes) {
            int d = parte.produto().getStatus() == StatusProduto.ATIVO ? disponivel(parte.produto()) : 0;
            menor = Math.min(menor, d / parte.quantidade());
        }
        return menor;
    }
}
