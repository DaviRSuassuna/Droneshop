package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.SubcategoriaRequestDTO;
import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.model.StatusCategoria;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.repository.CategoriaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Textos;
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
    public Subcategoria create(SubcategoriaRequestDTO dto) {
        Subcategoria subcategoria = new Subcategoria();
        aplicarDados(subcategoria, dto, null);
        subcategoriaRepository.persist(subcategoria);
        return subcategoria;
    }

    @Override
    @Transactional
    public void update(long id, SubcategoriaRequestDTO dto) {
        aplicarDados(buscarSubcategoriaOuFalhar(id), dto, id);
    }

    @Override
    @Transactional
    public void delete(long id) {
        long emUso = produtoRepository.count("subcategoria.id", id);
        if (emUso > 0)
            throw new ConflitoException("Esta subcategoria tem " + emUso + (emUso == 1 ? " produto" : " produtos")
                    + ". Inative a subcategoria em vez de excluir.");
        if (!subcategoriaRepository.deleteById(id))
            throw new ResourceNotFoundException("Subcategoria não encontrada.");
    }

    @Override
    public Subcategoria findById(long id) {
        return buscarSubcategoriaOuFalhar(id);
    }

    @Override
    public List<Subcategoria> findAll(Long idCategoria, int page, int pageSize) {
        return consulta(idCategoria).page(page, pageSize).list();
    }

    @Override
    public List<Subcategoria> findByNome(String nome, int page, int pageSize) {
        return subcategoriaRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count(Long idCategoria) {
        return consulta(idCategoria).count();
    }

    @Override
    public long count(String nome) {
        return subcategoriaRepository.findByNome(nome).count();
    }

    private PanacheQuery<Subcategoria> consulta(Long idCategoria) {
        return (idCategoria == null)
                ? subcategoriaRepository.findAll()
                : subcategoriaRepository.findByCategoria(idCategoria);
    }

    private void aplicarDados(Subcategoria destino, SubcategoriaRequestDTO dto, Long idAtual) {
        StatusCategoria status = buscarStatusOuFalhar(dto.idStatus());
        if (status != StatusCategoria.RASCUNHO) {
            var erros = new java.util.ArrayList<br.com.droneshop.exception.Problem.FieldError>();
            if (dto.nome() == null) erros.add(new br.com.droneshop.exception.Problem.FieldError("nome", "Informe o nome (back)."));
            if (dto.idCategoria() == null) erros.add(new br.com.droneshop.exception.Problem.FieldError("idCategoria", "Selecione a categoria (back)."));
            if (!erros.isEmpty()) throw new ValidationException("Preencha os campos obrigatórios para publicar a subcategoria.", erros);
        }
        String slug = dto.slug() != null ? Textos.slug(dto.slug()) : dto.nome() != null ? Textos.slug(dto.nome())
                : "rascunho-" + java.util.UUID.randomUUID();
        if (subcategoriaRepository.findBySlugExceptId(slug, idAtual) != null)
            throw ValidationException.of("slug", "Já existe uma subcategoria cadastrada com esse slug.");

        Categoria categoria = dto.idCategoria() == null ? null : buscarCategoriaOuFalhar(dto.idCategoria());
        validarTrocaDeTipo(destino, categoria);

        destino.setNome(dto.nome() == null ? "" : dto.nome().trim());
        destino.setSlug(slug);
        destino.setCategoria(categoria);
        destino.setOrdemExibicao(dto.ordemExibicao() == null ? 0 : dto.ordemExibicao());
        destino.setDescricao(dto.descricao());
        destino.setStatus(status);
    }

    // Mover para categoria de outro tipo deixaria os produtos dela no tipo errado
    private void validarTrocaDeTipo(Subcategoria subcategoria, Categoria nova) {
        Categoria atual = subcategoria.getCategoria();
        if (atual == null || (nova != null && (atual.getTipo() == null || atual.getTipo() == nova.getTipo())))
            return;
        long produtos = produtoRepository.count("subcategoria.id", subcategoria.getId());
        if (produtos > 0)
            throw new ConflitoException("Esta subcategoria tem " + produtos + (produtos == 1 ? " produto" : " produtos")
                    + ". Não é possível movê-la para uma categoria de outro tipo.");
    }

    private Subcategoria buscarSubcategoriaOuFalhar(long id) {
        Subcategoria subcategoria = subcategoriaRepository.findById(id);
        if (subcategoria == null)
            throw new ResourceNotFoundException("Subcategoria não encontrada.");
        return subcategoria;
    }

    private Categoria buscarCategoriaOuFalhar(Long idCategoria) {
        Categoria categoria = categoriaRepository.findById(idCategoria);
        if (categoria == null)
            throw ValidationException.of("idCategoria", "Categoria não encontrada.");
        return categoria;
    }

    private StatusCategoria buscarStatusOuFalhar(Long idStatus) {
        StatusCategoria status = StatusCategoria.valueOf(idStatus);
        if (status == null)
            throw ValidationException.of("idStatus", "Status não encontrado.");
        return status;
    }

}
