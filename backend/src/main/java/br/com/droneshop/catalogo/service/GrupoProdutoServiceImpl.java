package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.GrupoProdutoRequestDTO;
import br.com.droneshop.catalogo.model.GrupoProduto;
import br.com.droneshop.catalogo.repository.GrupoProdutoRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GrupoProdutoServiceImpl implements GrupoProdutoService {

    @Inject
    GrupoProdutoRepository grupoRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public GrupoProduto create(GrupoProdutoRequestDTO dto) {
        GrupoProduto grupo = new GrupoProduto();
        aplicar(grupo, dto, null);
        grupoRepository.persist(grupo);
        return grupo;
    }

    @Override
    @Transactional
    public void update(long id, GrupoProdutoRequestDTO dto) {
        aplicar(buscarOuFalhar(id), dto, id);
    }

    // Grupo com produtos nao se exclui (D24): tire as versoes dele ou inative o grupo
    @Override
    @Transactional
    public void delete(long id) {
        GrupoProduto grupo = buscarOuFalhar(id);
        long emUso = produtoRepository.countByGrupo(id);
        if (emUso > 0)
            throw new ConflitoException("Este grupo tem " + emUso + (emUso == 1 ? " produto" : " produtos")
                    + ". Tire os produtos do grupo ou inative-o em vez de excluir.");
        grupoRepository.delete(grupo);
    }

    @Override
    public GrupoProduto findById(long id) {
        return buscarOuFalhar(id);
    }

    @Override
    public List<GrupoProduto> findComFiltro(String nome, Boolean ativo, int page, int pageSize) {
        return grupoRepository.findComFiltro(nome, ativo).page(page, pageSize).list();
    }

    @Override
    public long count(String nome, Boolean ativo) {
        return grupoRepository.findComFiltro(nome, ativo).count();
    }

    private void aplicar(GrupoProduto destino, GrupoProdutoRequestDTO dto, Long idAtual) {
        String slug = Textos.slug(dto.slug() != null ? dto.slug() : dto.nome());
        if (grupoRepository.findBySlugExceptId(slug, idAtual) != null)
            throw ValidationException.of("slug", "Já existe um grupo com esse slug.");

        destino.setNome(dto.nome().trim());
        destino.setSlug(slug);
        destino.setDescricaoCurta(dto.descricaoCurta());
        destino.setAtivo(dto.ativo() == null || dto.ativo());
    }

    private GrupoProduto buscarOuFalhar(long id) {
        GrupoProduto grupo = grupoRepository.findById(id);
        if (grupo == null)
            throw new ResourceNotFoundException("Grupo não encontrado.");
        return grupo;
    }

}
