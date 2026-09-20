package br.com.droneshop.catalogo.service;

import java.text.Normalizer;
import java.util.ArrayList;
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

    @Inject
    CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public Categoria create(CategoriaDTO dto) {
        StatusCategoria status = parseStatus(dto.getStatus());
        validarObrigatoriedade(dto, status);

        String slug = resolverSlug(dto.getSlug(), dto.getNome());
        validarSlugUnico(slug, null);

        Categoria novaCategoria = new Categoria();
        novaCategoria.setNome(valorOuVazio(dto.getNome()));
        novaCategoria.setSlug(slug);
        novaCategoria.setCategoriaPai(resolverCategoriaPai(dto.getCategoriaPaiId(), null));
        novaCategoria.setOrdemExibicao(valorOuZero(dto.getOrdemExibicao()));
        novaCategoria.setDescricao(dto.getDescricao());
        novaCategoria.setStatus(status);
        novaCategoria.setExibirMenu(Boolean.TRUE.equals(dto.getExibirMenu()));
        novaCategoria.setIconeUrl(dto.getIconeUrl());

        // realizando inclusao
        categoriaRepository.persist(novaCategoria);

        return novaCategoria;
    }

    @Override
    @Transactional
    public void update(long id, CategoriaDTO dto) {
        StatusCategoria status = parseStatus(dto.getStatus());
        validarObrigatoriedade(dto, status);

        String slug = resolverSlug(dto.getSlug(), dto.getNome());
        validarSlugUnico(slug, id);

        Categoria edicaoCategoria = categoriaRepository.findById(id);

        edicaoCategoria.setNome(valorOuVazio(dto.getNome()));
        edicaoCategoria.setSlug(slug);
        edicaoCategoria.setCategoriaPai(resolverCategoriaPai(dto.getCategoriaPaiId(), id));
        edicaoCategoria.setOrdemExibicao(valorOuZero(dto.getOrdemExibicao()));
        edicaoCategoria.setDescricao(dto.getDescricao());
        edicaoCategoria.setStatus(status);
        edicaoCategoria.setExibirMenu(Boolean.TRUE.equals(dto.getExibirMenu()));
        edicaoCategoria.setIconeUrl(dto.getIconeUrl());
    }

    private StatusCategoria parseStatus(String status) {
        try {
            return StatusCategoria.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ValidationException.of("status", "Status inválido. Use RASCUNHO, ATIVA ou INATIVA.");
        }
    }

    // RASCUNHO nao exige nada preenchido; ATIVA/INATIVA exige nome e ordem de exibicao
    private void validarObrigatoriedade(CategoriaDTO dto, StatusCategoria status) {
        if (status == StatusCategoria.RASCUNHO)
            return;

        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.getNome() == null || dto.getNome().isBlank())
            erros.add(new Problem.FieldError("nome", "O campo deve ser informado (back)."));
        if (dto.getOrdemExibicao() == null || dto.getOrdemExibicao() < 0)
            erros.add(new Problem.FieldError("ordemExibicao", "O campo deve ser informado (back)."));

        if (!erros.isEmpty())
            throw new ValidationException("Preencha os campos obrigatórios para publicar a categoria.", erros);
    }

    // a categoria pai precisa existir e nao pode ser a propria categoria em edicao
    private Categoria resolverCategoriaPai(Long categoriaPaiId, Long idAtual) {
        if (categoriaPaiId == null)
            return null;
        if (categoriaPaiId.equals(idAtual))
            throw ValidationException.of("categoriaPaiId", "Uma categoria não pode ser pai dela mesma.");

        Categoria pai = categoriaRepository.findById(categoriaPaiId);
        if (pai == null)
            throw ValidationException.of("categoriaPaiId", "Categoria pai não encontrada.");

        return pai;
    }

    private String valorOuVazio(String valor) {
        return (valor == null) ? "" : valor;
    }

    private Integer valorOuZero(Integer valor) {
        return (valor == null) ? 0 : valor;
    }

    private void validarSlugUnico(String slug, Long id) {
        Categoria categoria = categoriaRepository.findBySlugExceptId(slug, id);
        if (categoria != null)
            throw ValidationException.of("slug", "Já existe uma categoria cadastrada com esse slug.");
    }

    // se o slug nao vier preenchido no DTO, gera a partir do nome;
    // se nome tambem estiver vazio (rascunho totalmente em branco), gera um slug temporario
    private String resolverSlug(String slugInformado, String nome) {
        if (slugInformado != null && !slugInformado.isBlank())
            return slugify(slugInformado);
        if (nome != null && !nome.isBlank())
            return slugify(nome);
        return "rascunho-" + System.currentTimeMillis();
    }

    private String slugify(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    @Transactional
    public void delete(long id) {
        categoriaRepository.deleteById(id);
    }

    @Override
    public Categoria findById(long id) {
        return categoriaRepository.findById(id);
    }

    @Override
    public Categoria findBySlug(String slug) {
        return categoriaRepository.findBySlug(slug);
    }

    @Override
    public List<Categoria> findAll(Integer page, Integer pageSize) {
        PanacheQuery<Categoria> query = categoriaRepository.findAll().page(page, pageSize);
        return query.list();
    }

    @Override
    public List<Categoria> findByNome(String nome, Integer page, Integer pageSize) {
        return categoriaRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return categoriaRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return categoriaRepository.findByNome(nome).count();
    }

}
