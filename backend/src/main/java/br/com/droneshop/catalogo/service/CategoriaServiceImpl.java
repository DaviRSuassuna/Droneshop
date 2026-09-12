package br.com.droneshop.catalogo.service;

import java.text.Normalizer;
import java.util.List;
import br.com.droneshop.catalogo.dto.CategoriaDTO;
import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.model.StatusCategoria;
import br.com.droneshop.catalogo.repository.CategoriaRepository;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ValidationException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CategoriaServiceImpl implements CategoriaService {
    @Inject CategoriaRepository categoriaRepository;

    @Override @Transactional
    public Categoria create(CategoriaDTO dto) { return salvar(null, dto); }

    @Override @Transactional
    public void update(long id, CategoriaDTO dto) { salvar(id, dto); }

    private Categoria salvar(Long id, CategoriaDTO dto) {
        StatusCategoria status = parseStatus(dto.getStatus());
        validar(dto, status);
        String slug = resolverSlug(dto.getSlug(), dto.getNome());
        if (categoriaRepository.findBySlugExceptId(slug, id) != null)
            throw ValidationException.of("slug", "Já existe uma categoria cadastrada com esse slug.");

        Categoria categoria = id == null ? new Categoria() : categoriaRepository.findById(id);
        if (categoria == null) throw ValidationException.of("id", "Categoria não encontrada.");
        if (dto.getCategoriaPaiId() != null && dto.getCategoriaPaiId().equals(id))
            throw ValidationException.of("categoriaPaiId", "Uma categoria não pode ser pai dela mesma.");
        categoria.setNome(valor(dto.getNome()));
        categoria.setSlug(slug);
        categoria.setCategoriaPai(dto.getCategoriaPaiId() == null ? null : categoriaRepository.findById(dto.getCategoriaPaiId()));
        if (dto.getCategoriaPaiId() != null && categoria.getCategoriaPai() == null)
            throw ValidationException.of("categoriaPaiId", "Categoria pai não encontrada.");
        categoria.setOrdemExibicao(dto.getOrdemExibicao() == null ? 0 : dto.getOrdemExibicao());
        categoria.setDescricao(dto.getDescricao());
        categoria.setStatus(status);
        categoria.setExibirMenu(Boolean.TRUE.equals(dto.getExibirMenu()));
        categoria.setIconeUrl(dto.getIconeUrl());
        if (id == null) categoriaRepository.persist(categoria);
        return categoria;
    }

    private void validar(CategoriaDTO dto, StatusCategoria status) {
        if (status == StatusCategoria.RASCUNHO) return;
        List<Problem.FieldError> erros = new java.util.ArrayList<>();
        if (dto.getNome() == null || dto.getNome().isBlank()) erros.add(new Problem.FieldError("nome", "O campo deve ser informado."));
        if (dto.getOrdemExibicao() == null || dto.getOrdemExibicao() < 0) erros.add(new Problem.FieldError("ordemExibicao", "Informe uma ordem válida."));
        if (!erros.isEmpty()) throw new ValidationException("Preencha os campos obrigatórios para publicar a categoria.", erros);
    }

    private StatusCategoria parseStatus(String status) {
        try { return StatusCategoria.valueOf(status.trim().toUpperCase()); }
        catch (Exception e) { throw ValidationException.of("status", "Status inválido. Use RASCUNHO, ATIVA ou INATIVA."); }
    }
    private String valor(String valor) { return valor == null ? "" : valor; }
    private String resolverSlug(String slug, String nome) {
        String origem = slug == null || slug.isBlank() ? nome : slug;
        if (origem == null || origem.isBlank()) return "rascunho-" + System.currentTimeMillis();
        return Normalizer.normalize(origem, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s-]", "").replaceAll("[\\s-]+", "-").replaceAll("^-|-$", "");
    }
    @Override @Transactional public void delete(long id) { categoriaRepository.deleteById(id); }
    @Override public Categoria findById(long id) { return categoriaRepository.findById(id); }
    @Override public List<Categoria> findAll(Integer page, Integer pageSize) { return categoriaRepository.findAll().page(page, pageSize).list(); }
}
