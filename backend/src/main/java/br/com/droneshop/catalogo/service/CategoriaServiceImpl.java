package br.com.droneshop.catalogo.service;

import java.util.HashSet;
import br.com.droneshop.catalogo.dto.CategoriaOrdemRequestDTO;
import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.catalogo.dto.CategoriaRequestDTO;
import br.com.droneshop.catalogo.model.Categoria;
import br.com.droneshop.catalogo.model.StatusCategoria;
import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.catalogo.repository.CategoriaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.exception.ConflitoException;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CategoriaServiceImpl implements CategoriaService {

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    SubcategoriaRepository subcategoriaRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public void reordenar(CategoriaOrdemRequestDTO dto) {
        if (new HashSet<>(dto.ids()).size() != dto.ids().size())
            throw ValidationException.of("ids", "A categoria não pode se repetir na ordem (back).");
        // Resolve tudo antes de alterar: id inexistente não deixa uma ordem parcial.
        List<Categoria> categorias = dto.ids().stream().map(this::buscarCategoriaOuFalhar).toList();
        for (int i = 0; i < categorias.size(); i++) categorias.get(i).setOrdemExibicao(i + 1);
    }

    @Override
    @Transactional
    public Categoria create(CategoriaRequestDTO dto) {
        StatusCategoria status = buscarStatusOuFalhar(dto.idStatus());
        TipoProduto tipo = buscarTipo(dto.idTipo());
        validarObrigatoriedade(dto, status);
        String slug = resolverSlug(dto.slug(), dto.nome());
        validarSlugUnico(slug, null);

        Categoria novaCategoria = new Categoria();
        aplicarDados(novaCategoria, dto, status, tipo, slug);
        categoriaRepository.persist(novaCategoria);
        return novaCategoria;
    }

    @Override
    @Transactional
    public void update(long id, CategoriaRequestDTO dto) {
        Categoria edicaoCategoria = buscarCategoriaOuFalhar(id);
        StatusCategoria status = buscarStatusOuFalhar(dto.idStatus());
        TipoProduto tipo = buscarTipo(dto.idTipo());
        validarObrigatoriedade(dto, status);
        String slug = resolverSlug(dto.slug(), dto.nome());
        validarSlugUnico(slug, id);
        validarTrocaDeTipo(edicaoCategoria, tipo);

        aplicarDados(edicaoCategoria, dto, status, tipo, slug);
    }

    @Override
    @Transactional
    public void delete(long id) {
        long emUso = subcategoriaRepository.count("categoria.id", id);
        if (emUso > 0)
            throw new ConflitoException("Esta categoria tem " + emUso + (emUso == 1 ? " subcategoria" : " subcategorias")
                    + ". Inative a categoria em vez de excluir.");
        if (!categoriaRepository.deleteById(id))
            throw new ResourceNotFoundException("Categoria não encontrada.");
    }

    @Override
    public Categoria findById(long id) {
        return buscarCategoriaOuFalhar(id);
    }

    @Override
    public Categoria findBySlug(String slug) {
        Categoria categoria = categoriaRepository.findBySlug(slug);
        if (categoria == null)
            throw new ResourceNotFoundException("Categoria não encontrada.");
        return categoria;
    }

    @Override
    public List<Categoria> findAll(int page, int pageSize) {
        return categoriaRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Categoria> findByNome(String nome, int page, int pageSize) {
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

    // RASCUNHO nao exige nada preenchido; ATIVA/INATIVA exige nome, ordem de exibicao e tipo
    private void validarObrigatoriedade(CategoriaRequestDTO dto, StatusCategoria status) {
        if (status == StatusCategoria.RASCUNHO)
            return;

        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.nome() == null)
            erros.add(new Problem.FieldError("nome", "O campo deve ser informado (back)."));
        if (dto.ordemExibicao() == null)
            erros.add(new Problem.FieldError("ordemExibicao", "O campo deve ser informado (back)."));
        if (dto.idTipo() == null)
            erros.add(new Problem.FieldError("idTipo", "Escolha se a categoria é de drones ou de acessórios (back)."));

        if (!erros.isEmpty())
            throw new ValidationException("Preencha os campos obrigatórios para publicar a categoria.", erros);
    }

    // Com produtos dentro, mudar o tipo deixaria drone em categoria de acessorio (ou o contrario)
    private void validarTrocaDeTipo(Categoria categoria, TipoProduto novoTipo) {
        if (categoria.getTipo() == null || categoria.getTipo() == novoTipo)
            return;
        long produtos = produtoRepository.count("subcategoria.categoria.id", categoria.getId());
        if (produtos > 0)
            throw new ConflitoException("Esta categoria tem " + produtos + (produtos == 1 ? " produto" : " produtos")
                    + ". Não é possível mudar o tipo dela.");
    }

    private TipoProduto buscarTipo(Long idTipo) {
        TipoProduto tipo = TipoProduto.valueOf(idTipo);
        if (idTipo != null && tipo == null)
            throw ValidationException.of("idTipo", "Tipo de produto não encontrado.");
        // kit entra em subcategoria de qualquer tipo; a categoria e so de drones ou de acessorios
        if (tipo == TipoProduto.KIT)
            throw ValidationException.of("idTipo", "Escolha se a categoria é de drones ou de acessórios (back).");
        return tipo;
    }

    private void validarSlugUnico(String slug, Long id) {
        if (categoriaRepository.findBySlugExceptId(slug, id) != null)
            throw ValidationException.of("slug", "Já existe uma categoria cadastrada com esse slug.");
    }

    private String resolverSlug(String slug, String nome) {
        if (slug != null)
            return Textos.slug(slug);
        if (nome != null)
            return Textos.slug(nome);
        return "rascunho-" + System.currentTimeMillis();
    }

    private void aplicarDados(Categoria destino, CategoriaRequestDTO origem, StatusCategoria status, TipoProduto tipo,
                              String slug) {
        destino.setNome(origem.nome() == null ? "" : origem.nome());
        destino.setSlug(slug);
        destino.setOrdemExibicao(origem.ordemExibicao() == null ? 0 : origem.ordemExibicao());
        destino.setDescricao(origem.descricao());
        destino.setStatus(status);
        destino.setTipo(tipo);
        destino.setExibirMenu(Boolean.TRUE.equals(origem.exibirMenu()));
        destino.setIconeUrl(origem.iconeUrl());
    }

    private Categoria buscarCategoriaOuFalhar(long id) {
        Categoria categoria = categoriaRepository.findById(id);
        if (categoria == null)
            throw new ResourceNotFoundException("Categoria não encontrada.");
        return categoria;
    }

    private StatusCategoria buscarStatusOuFalhar(Long idStatus) {
        StatusCategoria status = StatusCategoria.valueOf(idStatus);
        if (status == null)
            throw ValidationException.of("idStatus", "Status não encontrado.");
        return status;
    }

}
