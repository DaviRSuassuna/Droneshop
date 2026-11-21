package br.com.droneshop.atendimento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacaoRequestDTO;
import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacaoResponseDTO;
import br.com.droneshop.atendimento.dto.AvaliacaoDTOs.AvaliacoesProdutoDTO;
import br.com.droneshop.atendimento.model.Avaliacao;
import br.com.droneshop.atendimento.model.StatusModeracao;
import br.com.droneshop.atendimento.repository.AvaliacaoRepository;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.dto.PageResponse;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.identidade.model.PerfilCliente;
import br.com.droneshop.pedido.model.StatusPedido;
import br.com.droneshop.util.Textos;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AvaliacaoServiceImpl implements AvaliacaoService {

    @Inject
    EntityManager em;

    @Inject
    AvaliacaoRepository avaliacoes;

    private Produto ativo(String slug) {
        return em.createQuery("select p from Produto p where p.slug = :s and p.status = :st", Produto.class)
                .setParameter("s", slug).setParameter("st", StatusProduto.ATIVO).getResultStream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
    }

    @Override
    @Transactional
    public AvaliacaoResponseDTO avaliar(Long idUsuario, String slug, AvaliacaoRequestDTO dto) {
        PerfilCliente cliente = em.createQuery("select p from PerfilCliente p where p.usuario.id = :u",
                PerfilCliente.class).setParameter("u", idUsuario).getResultStream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
        Produto produto = ativo(slug);
        if (avaliacoes.count("produto.id = ?1 and cliente.id = ?2", produto.getId(), cliente.getId()) > 0)
            throw new ConflitoException("Você já avaliou este produto.");
        long entregues = em.createQuery("select count(i) from ItemPedido i where i.produto.id = :p"
                + " and i.pedido.cliente.id = :c and i.pedido.status = :s", Long.class)
                .setParameter("p", produto.getId()).setParameter("c", cliente.getId())
                .setParameter("s", StatusPedido.ENTREGUE).getSingleResult();
        Avaliacao a = new Avaliacao();
        a.setProduto(produto);
        a.setCliente(cliente);
        a.setNota(dto.nota());
        a.setComentario(Textos.nuloSeVazio(dto.comentario()));
        a.setCompraVerificada(entregues > 0);
        a.setStatus(StatusModeracao.PENDENTE);
        avaliacoes.persist(a);
        return AvaliacaoResponseDTO.from(a);
    }

    @Override
    @Transactional
    public AvaliacoesProdutoDTO aprovadas(String slug) {
        Produto produto = ativo(slug);
        List<Avaliacao> itens = avaliacoes.list("produto.id = ?1 and status = ?2", Sort.descending("id"),
                produto.getId(), StatusModeracao.APROVADA);
        Double media = itens.isEmpty() ? null
                : BigDecimal.valueOf(itens.stream().mapToInt(Avaliacao::getNota).average().orElse(0))
                        .setScale(1, RoundingMode.HALF_UP).doubleValue();
        return new AvaliacoesProdutoDTO(media, itens.size(), itens.stream().map(AvaliacaoResponseDTO::from).toList());
    }

    @Override
    @Transactional
    public PageResponse<AvaliacaoResponseDTO> listar(Long idStatus, int page, int pageSize) {
        StatusModeracao status = StatusModeracao.valueOf(idStatus);
        var q = status == null ? avaliacoes.findAll(Sort.descending("id"))
                : avaliacoes.find("status", Sort.descending("id"), status);
        return PageResponse.of(q.page(page, pageSize).list(), page, pageSize, q.count(), AvaliacaoResponseDTO::from);
    }

    @Override
    @Transactional
    public void moderar(Long id, Long idStatus) {
        Avaliacao a = avaliacoes.findById(id);
        if (a == null)
            throw new ResourceNotFoundException("Avaliação não encontrada.");
        StatusModeracao novo = StatusModeracao.valueOf(idStatus);
        if (novo == null)
            throw ValidationException.of("idStatus", "Status de moderação inválido (back)");
        a.setStatus(novo);
    }
}
