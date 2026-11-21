package br.com.droneshop.pagamento.repository;

import br.com.droneshop.pagamento.model.Pagamento;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PagamentoRepository implements PanacheRepository<Pagamento> {

    public Pagamento porGateway(String gatewayTransacaoId) {
        return find("gatewayTransacaoId", gatewayTransacaoId).firstResult();
    }
}
