package br.com.droneshop.catalogo.service;

import java.text.Normalizer;
import java.util.List;

import br.com.droneshop.catalogo.dto.SubcategoriaDTO;
import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.model.StatusCategoria;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.repository.CategoriaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SubcategoriaServiceImpl implements SubcategoriaService {

    @Inject
    SubcategoriaRepository subcategoriaRepository;

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public Subcategoria create(SubcategoriaDTO dto) {
        Subcategoria subcategoria = new Subcategoria();
        preencher(subcategoria, dto, null);
        subcategoriaRepository.persist(subcategoria);
        return subcategoria;
    }

    @Override
    @Transactional
    public void update(long id, SubcategoriaDTO dto) {
        preencher(findById(id), dto, id);
    }

    private void preencher(Subcategoria subcategoria, SubcategoriaDTO dto, Long idAtual) {
        String slug = slugify((dto.getSlug() == null || dto.getSlug().isBlank()) ? dto.getNome() : dto.getSlug());
        if (subcategoriaRepository.findBySlugExceptId(slug, idAtual) != null)
            throw ValidationException.of("slug", "Já existe uma subcategoria cadastrada com esse slug.");

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId());
        if (categoria == null)
            throw ValidationException.of("categoriaId", "Categoria não encontrada.");

        subcategoria.setNome(dto.getNome().trim());
        subcategoria.setSlug(slug);
        subcategoria.setCategoria(categoria);
        subcategoria.setOrdemExibicao(dto.getOrdemExibicao() == null ? 0 : dto.getOrdemExibicao());
        subcategoria.setDescricao(dto.getDescricao());
        subcategoria.setStatus(parseStatus(dto.getStatus()));
    }

    private StatusCategoria parseStatus(String status) {
        try {
            return StatusCategoria.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ValidationException.of("status", "Status inválido. Use RASCUNHO, ATIVA ou INATIVA.");
        }
    }

    private String slugify(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    @Transactional
    public void delete(long id) {
        long emUso = produtoRepository.count("subcategoria.id", id);
        if (emUso > 0)
            throw new ConflitoException("Esta subcategoria tem " + emUso + (emUso == 1 ? " produto" : " produtos")
                    + ". Inative a subcategoria em vez de excluir.");
        subcategoriaRepository.deleteById(id);
    }

    @Override
    public Subcategoria findById(long id) {
        Subcategoria subcategoria = subcategoriaRepository.findById(id);
        if (subcategoria == null)
            throw new ResourceNotFoundException("Subcategoria não encontrada.");
        return subcategoria;
    }

    // categoriaId opcional: sem ele, devolve todas as subcategorias
    @Override
    public List<Subcategoria> findAll(Long categoriaId, int page, int pageSize) {
        PanacheQuery<Subcategoria> query = (categoriaId == null)
                ? subcategoriaRepository.findAll()
                : subcategoriaRepository.findByCategoria(categoriaId);
        return query.page(page, pageSize).list();
    }

}
