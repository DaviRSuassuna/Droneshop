package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.droneshop.catalogo.dto.ProdutoRequestDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.repository.FornecedorRepository;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.exception.Problem;
import br.com.droneshop.exception.ResourceNotFoundException;
import br.com.droneshop.exception.ValidationException;
import br.com.droneshop.util.Textos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoServiceImpl implements ProdutoService {

    // provisorio: quando ConfiguracaoLoja existir, ler prazoGarantiaDias dela e remover esta constante
    private static final int PRAZO_GARANTIA_PADRAO_DIAS = 365;

    private static final String OBRIGATORIO = "O campo deve ser informado (back).";

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    MarcaRepository marcaRepository;

    @Inject
    SubcategoriaRepository subcategoriaRepository;

    @Inject
    FornecedorRepository fornecedorRepository;

    @Override
    @Transactional
    public Produto create(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        aplicarDados(produto, dto, null);
        produtoRepository.persist(produto);
        return produto;
    }

    @Override
    @Transactional
    public void update(long id, ProdutoRequestDTO dto) {
        aplicarDados(buscarProdutoOuFalhar(id), dto, id);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!produtoRepository.deleteById(id))
            throw new ResourceNotFoundException("Produto não encontrado.");
    }

    @Override
    public Produto findById(long id) {
        return buscarProdutoOuFalhar(id);
    }

    @Override
    public Produto findBySlug(String slug) {
        Produto produto = produtoRepository.findBySlug(slug);
        if (produto == null)
            throw new ResourceNotFoundException("Produto não encontrado.");
        return produto;
    }

    @Override
    public List<Produto> findComFiltro(String nome, Long idMarca, Long idCategoria, Long idSubcategoria, Long idStatus,
                                      int page, int pageSize) {
        return produtoRepository.findComFiltro(nome, idMarca, idCategoria, idSubcategoria, StatusProduto.valueOf(idStatus))
                .page(page, pageSize)
                .list();
    }

    @Override
    public long countComFiltro(String nome, Long idMarca, Long idCategoria, Long idSubcategoria, Long idStatus) {
        return produtoRepository.findComFiltro(nome, idMarca, idCategoria, idSubcategoria, StatusProduto.valueOf(idStatus))
                .count();
    }

    private void aplicarDados(Produto destino, ProdutoRequestDTO dto, Long idAtual) {
        StatusProduto status = validarObrigatoriedade(dto);

        String modelo = (dto.modelo() == null) ? null : dto.modelo().trim().toUpperCase();
        if (modelo != null && produtoRepository.findByModeloExceptId(modelo, idAtual) != null)
            throw ValidationException.of("modelo", "Já existe um produto com esse modelo/SKU.");

        String slug = resolverSlug(dto.slug(), dto.nome());
        if (produtoRepository.findBySlugExceptId(slug, idAtual) != null)
            throw ValidationException.of("slug", "Já existe um produto com esse slug.");

        destino.setNome(dto.nome() == null ? null : dto.nome().trim());
        destino.setModelo(modelo);
        destino.setDescricaoCurta(dto.descricaoCurta());
        destino.setDescricao(dto.descricao());
        destino.setSlug(slug);
        destino.setMetaTitle(dto.metaTitle());
        destino.setMetaDescription(dto.metaDescription());
        destino.setTags(dto.tags());
        destino.setGarantiaMeses(dto.garantiaMeses() != null ? dto.garantiaMeses() : garantiaPadraoMeses());
        destino.setDataLancamento(dto.dataLancamento());
        destino.setPreco(dto.preco());
        destino.setMarca(dto.idMarca() == null ? null : buscarMarcaOuFalhar(dto.idMarca()));
        destino.setSubcategoria(dto.idSubcategoria() == null ? null : buscarSubcategoriaOuFalhar(dto.idSubcategoria()));
        destino.setFornecedor(dto.idFornecedor() == null ? null : buscarFornecedorOuFalhar(dto.idFornecedor()));
        destino.setStatus(status);
        destino.setDestaque(Boolean.TRUE.equals(dto.destaque()));
    }

    private StatusProduto validarObrigatoriedade(ProdutoRequestDTO dto) {
        StatusProduto status = StatusProduto.valueOf(dto.idStatus());
        if (dto.idStatus() != null && status == null)
            throw ValidationException.of("idStatus", "Status não encontrado.");
        if (status == StatusProduto.RASCUNHO)
            return status;

        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.nome() == null)
            erros.add(new Problem.FieldError("nome", OBRIGATORIO));
        if (dto.modelo() == null)
            erros.add(new Problem.FieldError("modelo", OBRIGATORIO));
        if (dto.preco() == null)
            erros.add(new Problem.FieldError("preco", OBRIGATORIO));
        if (dto.idMarca() == null)
            erros.add(new Problem.FieldError("idMarca", "Selecione a marca (back)."));
        if (dto.idSubcategoria() == null)
            erros.add(new Problem.FieldError("idSubcategoria", "Selecione a subcategoria (back)."));
        if (dto.idFornecedor() == null)
            erros.add(new Problem.FieldError("idFornecedor", "Selecione o fornecedor (back)."));
        if (status == null)
            erros.add(new Problem.FieldError("idStatus", "O status deve ser informado (back)."));

        if (!erros.isEmpty())
            throw new ValidationException("Preencha os campos obrigatórios para publicar o produto.", erros);
        return status;
    }

    private String resolverSlug(String slug, String nome) {
        if (slug != null)
            return Textos.slug(slug);
        if (nome != null)
            return Textos.slug(nome);
        return "rascunho-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private int garantiaPadraoMeses() {
        return (int) Math.ceil(PRAZO_GARANTIA_PADRAO_DIAS / 30.0);
    }

    private Produto buscarProdutoOuFalhar(long id) {
        Produto produto = produtoRepository.findById(id);
        if (produto == null)
            throw new ResourceNotFoundException("Produto não encontrado.");
        return produto;
    }

    private Marca buscarMarcaOuFalhar(Long idMarca) {
        Marca marca = marcaRepository.findById(idMarca);
        if (marca == null)
            throw ValidationException.of("idMarca", "Marca não encontrada.");
        return marca;
    }

    private Subcategoria buscarSubcategoriaOuFalhar(Long idSubcategoria) {
        Subcategoria subcategoria = subcategoriaRepository.findById(idSubcategoria);
        if (subcategoria == null)
            throw ValidationException.of("idSubcategoria", "Subcategoria não encontrada.");
        return subcategoria;
    }

    private Fornecedor buscarFornecedorOuFalhar(Long idFornecedor) {
        Fornecedor fornecedor = fornecedorRepository.findById(idFornecedor);
        if (fornecedor == null)
            throw ValidationException.of("idFornecedor", "Fornecedor não encontrado.");
        return fornecedor;
    }

}
