package br.com.droneshop.atendimento.service;

import java.util.List;

import br.com.droneshop.atendimento.dto.PerguntaDTOs.PerguntaResponseDTO;
import br.com.droneshop.atendimento.dto.PerguntaDTOs.RespostaResponseDTO;
import br.com.droneshop.atendimento.model.Pergunta;
import br.com.droneshop.atendimento.model.RespostaPergunta;
import br.com.droneshop.atendimento.model.StatusModeracao;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.identidade.model.Usuario;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PerguntaServiceImpl implements PerguntaService {

    @Inject
    EntityManager em;

    @Inject
    br.com.droneshop.atendimento.repository.PerguntaRepository perguntas;

    private Produto ativo(String slug) {
        return em.createQuery("select p from Produto p where p.slug = :s and p.status = :st", Produto.class)
                .setParameter("s", slug).setParameter("st", StatusProduto.ATIVO).getResultStream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
    }

    @Override
    @Transactional
    public PerguntaResponseDTO perguntar(Long idUsuario, String slug, String texto) {
        PerfilCliente cliente = em.createQuery("select p from PerfilCliente p where p.usuario.id = :u",
                PerfilCliente.class).setParameter("u", idUsuario).getResultStream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
        Pergunta p = new Pergunta();
        p.setProduto(ativo(slug));
        p.setCliente(cliente);
        p.setTexto(texto.trim());
        p.setStatus(StatusModeracao.PENDENTE);
        perguntas.persist(p);
        return PerguntaResponseDTO.from(p);
    }

    @Override
    @Transactional
    public List<PerguntaResponseDTO> aprovadas(String slug) {
        Produto produto = ativo(slug);
        return perguntas.list("produto.id = ?1 and status = ?2", Sort.descending("id"), produto.getId(),
                StatusModeracao.APROVADA).stream().map(PerguntaResponseDTO::from).toList();
    }

    @Override
    @Transactional
    public RespostaResponseDTO responder(Long idUsuario, Long idPergunta, String texto, boolean oficial) {
        Pergunta p = perguntas.findById(idPergunta);
        if (p == null)
            throw new ResourceNotFoundException("Pergunta não encontrada.");
        if (p.getStatus() != StatusModeracao.APROVADA)
            throw new ConflitoException("Só pergunta aprovada recebe resposta.");
        RespostaPergunta r = new RespostaPergunta();
        r.setPergunta(p);
        r.setAutor(em.find(Usuario.class, idUsuario));
        r.setTexto(texto.trim());
        r.setOficial(oficial);
        p.getRespostas().add(r);
        em.flush();
        return RespostaResponseDTO.from(r);
    }

    @Override
    @Transactional
    public PageResponse<PerguntaResponseDTO> listar(Long idStatus, int page, int pageSize) {
        StatusModeracao status = StatusModeracao.valueOf(idStatus);
        var q = status == null ? perguntas.findAll(Sort.descending("id"))
                : perguntas.find("status", Sort.descending("id"), status);
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(), PerguntaResponseDTO::from);
    }

    @Override
    @Transactional
    public void moderar(Long idPergunta, Long idStatus) {
        Pergunta p = perguntas.findById(idPergunta);
        if (p == null)
            throw new ResourceNotFoundException("Pergunta não encontrada.");
        StatusModeracao novo = StatusModeracao.valueOf(idStatus);
        if (novo == null)
            throw ValidationException.of("idStatus", "Status de moderação inválido (back)");
        p.setStatus(novo);
    }
}
