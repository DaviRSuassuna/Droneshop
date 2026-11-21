package br.com.droneshop.pagamento.service;

import java.time.YearMonth;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.repository.PerfilClienteRepository;
import br.com.droneshop.pagamento.dto.CartaoSalvoRequestDTO;
import br.com.droneshop.pagamento.model.CartaoSalvo;
import br.com.droneshop.pagamento.repository.CartaoSalvoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/** Cartões salvos só pelo token do gateway (RF24, RN11). */
@ApplicationScoped
public class CartaoSalvoServiceImpl implements CartaoSalvoService {

    private static final Pattern VALIDADE = Pattern.compile("(0[1-9]|1[0-2])/(\\d{2})");

    @Inject
    CartaoSalvoRepository cartoes;

    @Inject
    PerfilClienteRepository clientes;

    @Override
    public List<CartaoSalvo> listar(Long idUsuario) {
        return cartoes.doUsuario(idUsuario);
    }

    @Override
    @Transactional
    public CartaoSalvo salvar(Long idUsuario, CartaoSalvoRequestDTO dto) {
        if (dto.numero() != null && !dto.numero().isBlank())
            throw ValidationException.of("numero", "Não envie o número do cartão, só o token do gateway (back)");
        if (dto.ultimosDigitos() == null || !dto.ultimosDigitos().matches("\\d{4}"))
            throw ValidationException.of("ultimosDigitos", "Informe os 4 últimos dígitos (back)");
        Matcher m = VALIDADE.matcher(dto.validade() == null ? "" : dto.validade().trim());
        if (!m.matches())
            throw ValidationException.of("validade", "Use o formato MM/AA (back)");
        YearMonth validade = YearMonth.of(2000 + Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)));
        if (validade.isBefore(YearMonth.now()))
            throw ValidationException.of("validade", "Cartão vencido (back)");

        PerfilCliente cliente = clientes.porUsuario(idUsuario);
        CartaoSalvo c = new CartaoSalvo();
        c.setCliente(cliente);
        c.setTokenGateway(dto.tokenGateway().trim());
        c.setUltimosDigitos(dto.ultimosDigitos());
        c.setBandeira(dto.bandeira() == null || dto.bandeira().isBlank() ? null : dto.bandeira().trim());
        c.setValidade(dto.validade().trim());
        c.setAtivo(true);
        c.setPrincipal(cartoes.doUsuario(idUsuario).isEmpty());
        cartoes.persist(c);
        return c;
    }

    private CartaoSalvo doUsuario(Long idUsuario, Long id) {
        CartaoSalvo c = cartoes.doUsuario(id, idUsuario);
        if (c == null)
            throw new ResourceNotFoundException("Cartão não encontrado.");
        return c;
    }

    // Pagamentos antigos apontam para o cartão: ele só sai da lista (D24)
    @Override
    @Transactional
    public void excluir(Long idUsuario, Long id) {
        CartaoSalvo c = doUsuario(idUsuario, id);
        c.setAtivo(false);
        if (c.isPrincipal()) {
            c.setPrincipal(false);
            cartoes.doUsuario(idUsuario).stream().filter(x -> !x.getId().equals(id)).findFirst()
                    .ifPresent(x -> x.setPrincipal(true));
        }
    }

    @Override
    @Transactional
    public void tornarPadrao(Long idUsuario, Long id) {
        CartaoSalvo c = doUsuario(idUsuario, id);
        cartoes.doUsuario(idUsuario).forEach(x -> x.setPrincipal(x.getId().equals(c.getId())));
    }
}
