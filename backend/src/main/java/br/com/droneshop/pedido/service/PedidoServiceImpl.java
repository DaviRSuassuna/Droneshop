package br.com.droneshop.pedido.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.pedido.dto.PedidoFiltro;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.identidade.repository.UsuarioRepository;
import br.com.droneshop.model.OrigemRegistro;
import br.com.droneshop.pedido.dto.ResumoStatusPedidoResponseDTO;
import br.com.droneshop.pedido.dto.StatusPedidoRequestDTO;
import br.com.droneshop.pedido.model.HistoricoStatusPedido;
import br.com.droneshop.pedido.model.Pedido;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.pedido.repository.PedidoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PedidoServiceImpl implements PedidoService {

    // "AER-000123", "aer123", "#AER-000123" ou só "123"
    private static final Pattern NUMERO_PEDIDO = Pattern.compile("(?i)^#?(?:AER-?)?0*(\\d{1,18})$");

    @Inject
    PedidoRepository pedidoRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Override
    public PedidoFiltro filtro(String idStatus, String dataInicio, String dataFim, String busca) {
        StatusPedido status = null;
        if (temTexto(idStatus)) {
            try {
                status = StatusPedido.valueOf(Long.valueOf(idStatus.trim()));
            } catch (NumberFormatException e) {
                status = null;
            }
            if (status == null)
                throw ValidationException.of("idStatus", "Status de pedido inexistente.");
        }

        LocalDate inicio = data(dataInicio, "dataInicio");
        LocalDate fim = data(dataFim, "dataFim");
        if (inicio != null && fim != null && fim.isBefore(inicio))
            throw ValidationException.of("dataFim", "A data final não pode ser anterior à inicial.");

        String nome = null;
        Long idBusca = null;
        if (temTexto(busca)) {
            nome = busca.trim();
            Matcher m = NUMERO_PEDIDO.matcher(nome);
            if (m.matches())
                idBusca = Long.valueOf(m.group(1));
        }

        return new PedidoFiltro(status,
                inicio == null ? null : inicio.atStartOfDay(),
                fim == null ? null : fim.plusDays(1).atStartOfDay(),
                nome, idBusca);
    }

    @Override
    public List<Pedido> findComFiltro(PedidoFiltro filtro, int page, int pageSize) {
        return pedidoRepository.findComFiltro(filtro).page(page, pageSize).list();
    }

    @Override
    public long countComFiltro(PedidoFiltro filtro) {
        return pedidoRepository.findComFiltro(filtro).count();
    }

    @Override
    public List<ResumoStatusPedidoResponseDTO> resumoPorStatus() {
        Map<StatusPedido, Long> contagem = pedidoRepository.contarPorStatus();
        return Arrays.stream(StatusPedido.values())
                .map(s -> new ResumoStatusPedidoResponseDTO(s, contagem.getOrDefault(s, 0L)))
                .toList();
    }

    @Override
    public Pedido findById(long id) {
        Pedido pedido = pedidoRepository.findById(id);
        if (pedido == null)
            throw new ResourceNotFoundException("Pedido não encontrado.");
        return pedido;
    }

    @Override
    @Transactional
    public void alterarStatus(long id, StatusPedidoRequestDTO dto, long idAutor) {
        Pedido pedido = findById(id);
        StatusPedido novo = dto == null ? null : StatusPedido.valueOf(dto.idStatus());
        if (novo == null)
            throw ValidationException.of("idStatus", "Status de pedido inexistente.");

        StatusPedido atual = pedido.getStatus();
        if (!atual.podeIrPara(novo))
            throw new ConflitoException(
                    "O pedido não pode passar de " + atual.getNome() + " para " + novo.getNome() + ".");

        Usuario autor = usuarioRepository.findById(idAutor);
        if (autor == null)
            throw new ResourceNotFoundException("Usuário não encontrado.");

        HistoricoStatusPedido historico = new HistoricoStatusPedido();
        historico.setPedido(pedido);
        historico.setStatusAnterior(atual);
        historico.setStatusNovo(novo);
        historico.setOrigem(OrigemRegistro.MANUAL);
        historico.setAutor(autor);
        historico.setObservacao(dto.observacao());

        pedido.setStatus(novo);
        pedido.getHistorico().add(historico);
    }

    @Override
    public List<HistoricoStatusPedido> historico(long id) {
        return findById(id).getHistorico();
    }

    private static boolean temTexto(String s) {
        return s != null && !s.isBlank();
    }

    private static LocalDate data(String valor, String campo) {
        if (!temTexto(valor))
            return null;
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw ValidationException.of(campo, "Data inválida; use o formato AAAA-MM-DD.");
        }
    }
}
