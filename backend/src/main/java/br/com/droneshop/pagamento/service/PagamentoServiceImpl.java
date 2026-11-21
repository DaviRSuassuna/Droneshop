package br.com.droneshop.pagamento.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.com.droneshop.estoque.service.ReservaService;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ServicoExternoIndisponivelException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.TipoPessoa;
import br.com.droneshop.model.OrigemRegistro;
import br.com.droneshop.pagamento.client.MercadoPagoClient;
import br.com.droneshop.pagamento.dto.CartaoSalvoRequestDTO;
import br.com.droneshop.pagamento.dto.PagamentoRequestDTO;
import br.com.droneshop.pagamento.model.CartaoSalvo;
import br.com.droneshop.pagamento.model.MetodoPagamento;
import br.com.droneshop.pagamento.model.Pagamento;
import br.com.droneshop.pagamento.model.StatusPagamento;
import br.com.droneshop.pagamento.repository.CartaoSalvoRepository;
import br.com.droneshop.pagamento.repository.PagamentoRepository;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import br.com.droneshop.pedido.service.PedidoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class PagamentoServiceImpl implements PagamentoService {

    private static final Logger LOG = Logger.getLogger(PagamentoServiceImpl.class);

    @Inject
    @RestClient
    MercadoPagoClient gateway;

    @Inject
    PagamentoRepository pagamentos;

    @Inject
    CartaoSalvoRepository cartoes;

    @Inject
    CartaoSalvoService cartoesService;

    @Inject
    PedidoRepository pedidos;

    @Inject
    PedidoService pedidoService;

    @Inject
    ReservaService reservas;

    static String metodoNoGateway(MetodoPagamento m) {
        return switch (m) {
            case PIX -> "pix";
            case BOLETO -> "bolbradesco";
            case CARTAO -> "credit_card";
            case FATURAMENTO_PJ -> "account_money";
        };
    }

    static StatusPagamento status(String statusGateway) {
        return switch (statusGateway == null ? "" : statusGateway) {
            case "approved", "authorized" -> StatusPagamento.APROVADO;
            case "rejected", "cancelled" -> StatusPagamento.RECUSADO;
            case "refunded" -> StatusPagamento.ESTORNADO;
            default -> StatusPagamento.PENDENTE;
        };
    }

    @Override
    @Transactional
    public Pagamento pagar(Long idUsuario, Long idPedido, PagamentoRequestDTO dto) {
        Pedido pedido = pedidos.doUsuario(idPedido, idUsuario);
        if (pedido == null)
            throw new ResourceNotFoundException("Pedido não encontrado.");
        MetodoPagamento metodo = MetodoPagamento.valueOf(dto.idMetodo());
        if (metodo == null)
            throw ValidationException.of("idMetodo", "Método de pagamento inválido (back)");
        if (metodo == MetodoPagamento.FATURAMENTO_PJ && pedido.getCliente().getTipoPessoa() != TipoPessoa.PJ)
            throw ValidationException.of("idMetodo", "Faturamento só para pessoa jurídica (back)");
        if (pedido.getStatus() != StatusPedido.PENDENTE
                || pedido.getPagamentos().stream().anyMatch(p -> p.getStatus() == StatusPagamento.APROVADO))
            throw new ConflitoException("Este pedido não está aguardando pagamento.");

        CartaoSalvo cartao = null;
        String token = null;
        if (metodo == MetodoPagamento.CARTAO) {
            if (dto.idCartaoSalvo() != null) {
                cartao = cartoes.doUsuario(dto.idCartaoSalvo(), idUsuario);
                if (cartao == null)
                    throw ValidationException.of("idCartaoSalvo", "Cartão não encontrado (back)");
                token = cartao.getTokenGateway();
            } else {
                token = dto.tokenCartao();
            }
            if (token == null || token.isBlank())
                throw ValidationException.of("tokenCartao", "Informe o cartão (back)");
        }

        MercadoPagoClient.PagamentoGateway resposta;
        try {
            LOG.infof("Pagamento no gateway: pedido %d, método %s", pedido.getId(), metodo.getNome());
            resposta = gateway.criar(new MercadoPagoClient.NovoPagamento(pedido.getValorTotal(),
                    metodoNoGateway(metodo), token, String.valueOf(pedido.getId())));
        } catch (ProcessingException | WebApplicationException e) {
            LOG.errorf("Falha de integração com o Mercado Pago (pedido %d): %s", pedido.getId(), e.getMessage());
            throw new ServicoExternoIndisponivelException("O meio de pagamento está fora do ar. Tente de novo.", e);
        }

        pedido.getPagamentos().forEach(p -> p.setVigente(false));
        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setMetodo(metodo);
        pagamento.setValor(pedido.getValorTotal());
        pagamento.setVigente(true);
        pagamento.setGatewayTransacaoId(resposta.id());
        pagamento.setCartao(cartao);
        pagamento.setStatus(status(resposta.status()));
        pagamentos.persist(pagamento);
        pedido.getPagamentos().add(pagamento);

        if (metodo == MetodoPagamento.CARTAO && cartao == null && Boolean.TRUE.equals(dto.salvarCartao())
                && pagamento.getStatus() == StatusPagamento.APROVADO && dto.ultimosDigitos() != null)
            cartoesService.salvar(idUsuario, new CartaoSalvoRequestDTO(token, dto.ultimosDigitos(), dto.bandeira(),
                    dto.validade(), null));
        if (pagamento.getStatus() == StatusPagamento.APROVADO)
            aprovado(pagamento);
        return pagamento;
    }

    // Pedido PAGO (origem INTEGRACAO), reservas confirmadas e baixa do físico (#23, RF14)
    private void aprovado(Pagamento pagamento) {
        Pedido pedido = pagamento.getPedido();
        if (pedido.getStatus() != StatusPedido.PENDENTE)
            return;
        pedidoService.mudarSemAutor(pedido, StatusPedido.PAGO, OrigemRegistro.INTEGRACAO,
                "Pagamento aprovado no Mercado Pago");
        if (pedido.getCarrinho() != null)
            reservas.confirmar(pedido.getCarrinho().getItens());
    }

    @Override
    @Transactional
    public void webhook(String gatewayTransacaoId) {
        if (gatewayTransacaoId == null)
            return;
        Pagamento pagamento = pagamentos.porGateway(gatewayTransacaoId);
        if (pagamento == null) {
            LOG.warnf("Webhook do Mercado Pago para pagamento desconhecido: %s", gatewayTransacaoId);
            return;
        }
        MercadoPagoClient.PagamentoGateway atual;
        try {
            atual = gateway.consultar(gatewayTransacaoId);
        } catch (ProcessingException | WebApplicationException e) {
            LOG.errorf("Falha de integração ao consultar o pagamento %s: %s", gatewayTransacaoId, e.getMessage());
            throw new ServicoExternoIndisponivelException("Mercado Pago fora do ar.", e);
        }
        StatusPagamento novo = status(atual.status());
        if (novo == pagamento.getStatus() || pagamento.getStatus() != StatusPagamento.PENDENTE)
            return;
        pagamento.setStatus(novo);
        if (novo == StatusPagamento.APROVADO)
            aprovado(pagamento);
    }

    @Override
    @Transactional
    public List<Pagamento> doPedido(Long idPedido) {
        Pedido pedido = pedidos.findById(idPedido);
        if (pedido == null)
            throw new ResourceNotFoundException("Pedido não encontrado.");
        return pedido.getPagamentos();
    }

    @Override
    public Pagamento buscar(Long id) {
        Pagamento p = pagamentos.findById(id);
        if (p == null)
            throw new ResourceNotFoundException("Pagamento não encontrado.");
        return p;
    }
}
