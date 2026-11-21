package br.com.droneshop.pagamento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ServicoExternoIndisponivelException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.pagamento.client.MercadoPagoClient;
import br.com.droneshop.pagamento.model.Estorno;
import br.com.droneshop.pagamento.model.Pagamento;
import br.com.droneshop.pagamento.model.StatusPagamento;
import br.com.droneshop.pagamento.repository.EstornoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class EstornoServiceImpl implements EstornoService {

    private static final Logger LOG = Logger.getLogger(EstornoServiceImpl.class);

    @Inject
    @RestClient
    MercadoPagoClient gateway;

    @Inject
    PagamentoService pagamentos;

    @Inject
    EstornoRepository estornos;

    @Override
    @Transactional
    public Estorno estornar(Long idPagamento, BigDecimal valor, String motivo) {
        Pagamento p = pagamentos.buscar(idPagamento);
        if (p.getStatus() != StatusPagamento.APROVADO && p.getStatus() != StatusPagamento.ESTORNADO_PARCIAL)
            throw new ConflitoException("Só pagamento aprovado pode ser estornado.");
        BigDecimal v = valor.setScale(2, RoundingMode.HALF_UP);
        BigDecimal jaEstornado = p.getEstornos().stream().map(Estorno::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal resta = p.getValor().subtract(jaEstornado);
        if (v.signum() <= 0 || v.compareTo(resta) > 0)
            throw ValidationException.of("valor", "Informe um valor entre 0,01 e " + resta + " (back)");

        MercadoPagoClient.EstornoGateway resposta;
        try {
            resposta = gateway.estornar(p.getGatewayTransacaoId(), new MercadoPagoClient.NovoEstorno(v));
        } catch (ProcessingException | WebApplicationException e) {
            LOG.errorf("Falha de integração no estorno do pagamento %d: %s", p.getId(), e.getMessage());
            throw new ServicoExternoIndisponivelException("O meio de pagamento está fora do ar. Tente de novo.", e);
        }
        Estorno e = new Estorno();
        e.setPagamento(p);
        e.setValor(v);
        e.setMotivo(motivo.trim());
        e.setGatewayEstornoId(resposta == null ? null : resposta.id());
        estornos.persist(e);
        p.getEstornos().add(e);
        p.setStatus(v.compareTo(resta) == 0 ? StatusPagamento.ESTORNADO : StatusPagamento.ESTORNADO_PARCIAL);
        return e;
    }

    @Override
    @Transactional
    public List<Estorno> doPagamento(Long idPagamento) {
        return pagamentos.buscar(idPagamento).getEstornos();
    }
}
