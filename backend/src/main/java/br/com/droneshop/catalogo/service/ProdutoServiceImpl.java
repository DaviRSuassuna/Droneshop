package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.ProdutoDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import br.com.droneshop.catalogo.model.Marca;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.Subcategoria;
import br.com.droneshop.catalogo.repository.FornecedorRepository;
import br.com.droneshop.catalogo.repository.MarcaRepository;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.catalogo.repository.SubcategoriaRepository;
import br.com.droneshop.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoServiceImpl implements ProdutoService {

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
    public Produto create(ProdutoDTO dto) {
        Produto produto = new Produto();
        preencher(produto, dto, null);
        produtoRepository.persist(produto);
        return produto;
    }

    @Override
    @Transactional
    public void update(long id, ProdutoDTO dto) {
        Produto produto = produtoRepository.findById(id);
        if (produto == null)
            throw ValidationException.of("id", "Produto não encontrado.");
        preencher(produto, dto, id);
    }

    private void preencher(Produto produto, ProdutoDTO dto, Long idAtual) {
        String modelo = dto.getModelo().trim().toUpperCase();
        if (produtoRepository.findByModeloExceptId(modelo, idAtual) != null)
            throw ValidationException.of("modelo", "Já existe um produto com esse modelo/SKU.");

        produto.setNome(dto.getNome().trim());
        produto.setModelo(modelo);
        produto.setDescricaoCurta(dto.getDescricaoCurta());
        produto.setPreco(dto.getPreco());
        produto.setMarca(resolverMarca(dto.getMarcaId()));
        produto.setSubcategoria(resolverSubcategoria(dto.getSubcategoriaId()));
        produto.setFornecedor(resolverFornecedor(dto.getFornecedorId()));
        produto.setStatus(parseStatus(dto.getStatus()));
        produto.setDestaque(Boolean.TRUE.equals(dto.getDestaque()));
    }

    private Marca resolverMarca(Long marcaId) {
        Marca marca = marcaRepository.findById(marcaId);
        if (marca == null)
            throw ValidationException.of("marcaId", "Marca não encontrada.");
        return marca;
    }

    private Subcategoria resolverSubcategoria(Long subcategoriaId) {
        Subcategoria subcategoria = subcategoriaRepository.findById(subcategoriaId);
        if (subcategoria == null)
            throw ValidationException.of("subcategoriaId", "Subcategoria não encontrada.");
        return subcategoria;
    }

    private Fornecedor resolverFornecedor(Long fornecedorId) {
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId);
        if (fornecedor == null)
            throw ValidationException.of("fornecedorId", "Fornecedor não encontrado.");
        return fornecedor;
    }

    private StatusProduto parseStatus(String status) {
        try {
            return StatusProduto.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ValidationException.of("status", "Status inválido. Use RASCUNHO, ATIVO ou INATIVO.");
        }
    }

    // no filtro, status vazio/invalido significa "todos"
    private StatusProduto parseStatusFiltro(String status) {
        if (status == null || status.isBlank())
            return null;
        try {
            return StatusProduto.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void delete(long id) {
        produtoRepository.deleteById(id);
    }

    @Override
    public Produto findById(long id) {
        return produtoRepository.findById(id);
    }

    @Override
    public List<Produto> findComFiltro(String nome, Long marcaId, Long categoriaId, Long subcategoriaId, String status,
                                      int page, int pageSize) {
        return produtoRepository.findComFiltro(nome, marcaId, categoriaId, subcategoriaId, parseStatusFiltro(status))
                .page(page, pageSize)
                .list();
    }

    @Override
    public long countComFiltro(String nome, Long marcaId, Long categoriaId, Long subcategoriaId, String status) {
        return produtoRepository.findComFiltro(nome, marcaId, categoriaId, subcategoriaId, parseStatusFiltro(status)).count();
    }

}
