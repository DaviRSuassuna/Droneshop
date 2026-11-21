package br.com.droneshop.atendimento.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.com.droneshop.atendimento.dto.ChamadoDTOs.ChamadoRequestDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.ChamadoResponseDTO;
import br.com.droneshop.atendimento.dto.ChamadoDTOs.MensagemResponseDTO;
import br.com.droneshop.atendimento.model.Chamado;
import br.com.droneshop.atendimento.model.MensagemChamado;
import br.com.droneshop.atendimento.model.StatusChamado;
import br.com.droneshop.atendimento.model.TipoAutorMensagem;
import br.com.droneshop.atendimento.repository.ChamadoRepository;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.Usuario;
import br.com.droneshop.pedido.model.Pedido;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ChamadoServiceImpl implements ChamadoService {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Inject
    ChamadoRepository chamados;

    @Inject
    EntityManager em;

    @Inject
    Event<ChamadoRespondido> respondido;

    private PerfilCliente cliente(Long idUsuario) {
        return em.createQuery("select p from PerfilCliente p where p.usuario.id = :u", PerfilCliente.class)
                .setParameter("u", idUsuario).getResultStream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    private Chamado doCliente(Long idUsuario, Long idChamado) {
        Chamado c = chamados.findById(idChamado);
        if (c == null || !c.getCliente().getUsuario().getId().equals(idUsuario))
            throw new ResourceNotFoundException("Chamado não encontrado.");
        return c;
    }

    private Chamado chamado(Long idChamado) {
        Chamado c = chamados.findById(idChamado);
        if (c == null)
            throw new ResourceNotFoundException("Chamado não encontrado.");
        return c;
    }

    private static MensagemChamado mensagem(Chamado c, TipoAutorMensagem tipo, Usuario autor, String texto) {
        if (c.getStatus() == StatusChamado.ENCERRADO)
            throw new ConflitoException("Chamado encerrado não recebe mensagem.");
        MensagemChamado m = new MensagemChamado();
        m.setChamado(c);
        m.setTipoAutor(tipo);
        m.setAutor(autor);
        m.setTexto(texto.trim());
        c.getMensagens().add(m);
        return m;
    }

    @Override
    @Transactional
    public ChamadoResponseDTO abrir(Long idUsuario, ChamadoRequestDTO dto) {
        PerfilCliente cliente = cliente(idUsuario);
        Chamado c = new Chamado();
        if (dto.idPedido() != null) {
            Pedido p = em.find(Pedido.class, dto.idPedido());
            if (p == null || !p.getCliente().getId().equals(cliente.getId()))
                throw ValidationException.of("idPedido", "Pedido não encontrado na sua conta (back)");
            c.setPedido(p);
        }
        String prefixo = "AER-" + LocalDate.now().format(DIA) + "-";
        c.setProtocolo(prefixo + String.format("%04d", chamados.doDia(prefixo) + 1));
        c.setStatus(StatusChamado.ABERTO);
        c.setAssunto(dto.assunto().trim());
        c.setCliente(cliente);
        mensagem(c, TipoAutorMensagem.CLIENTE, cliente.getUsuario(), dto.mensagem());
        chamados.persist(c);
        return ChamadoResponseDTO.from(c);
    }

    @Override
    @Transactional
    public List<ChamadoResponseDTO> doCliente(Long idUsuario) {
        return chamados.doUsuario(idUsuario).stream().map(ChamadoResponseDTO::from).toList();
    }

    @Override
    @Transactional
    public List<MensagemResponseDTO> mensagensDoCliente(Long idUsuario, Long idChamado) {
        return doCliente(idUsuario, idChamado).getMensagens().stream().map(MensagemResponseDTO::from).toList();
    }

    @Override
    @Transactional
    public MensagemResponseDTO responderComoCliente(Long idUsuario, Long idChamado, String texto) {
        Chamado c = doCliente(idUsuario, idChamado);
        MensagemChamado m = mensagem(c, TipoAutorMensagem.CLIENTE, c.getCliente().getUsuario(), texto);
        em.flush();
        return MensagemResponseDTO.from(m);
    }

    @Override
    @Transactional
    public PageResponse<ChamadoResponseDTO> listar(Long idStatus, int page, int pageSize) {
        var q = chamados.filtrar(StatusChamado.valueOf(idStatus));
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(), ChamadoResponseDTO::from);
    }

    @Override
    @Transactional
    public ChamadoResponseDTO buscar(Long idChamado) {
        return ChamadoResponseDTO.from(chamado(idChamado));
    }

    @Override
    @Transactional
    public MensagemResponseDTO responderComoAtendente(Long idAtendente, Long idChamado, String texto) {
        Chamado c = chamado(idChamado);
        Usuario atendente = em.find(Usuario.class, idAtendente);
        MensagemChamado m = mensagem(c, TipoAutorMensagem.ATENDENTE, atendente, texto);
        if (c.getAtendente() == null)
            c.setAtendente(atendente);
        c.setStatus(StatusChamado.EM_ANDAMENTO);
        em.flush();
        respondido.fire(new ChamadoRespondido(c.getId()));
        return MensagemResponseDTO.from(m);
    }

    @Override
    @Transactional
    public void alterarStatus(Long idChamado, Long idStatus) {
        Chamado c = chamado(idChamado);
        StatusChamado novo = StatusChamado.valueOf(idStatus);
        if (novo == null)
            throw ValidationException.of("idStatus", "Status de chamado inválido (back)");
        if (c.getStatus() == StatusChamado.ENCERRADO && novo != StatusChamado.ENCERRADO)
            throw new ConflitoException("Chamado encerrado não volta a ser aberto.");
        c.setStatus(novo);
    }
}
