package br.com.droneshop.catalogo.service;

import java.util.List;

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
import br.com.droneshop.exception.ResourceNotFoundException;
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
        StatusProduto status = StatusProduto.valueOf(dto.idStatus());
        if (status == null)
            throw ValidationException.of("idStatus", "Status não encontrado.");

        String modelo = dto.modelo().trim().toUpperCase();
        if (produtoRepository.findByModeloExceptId(modelo, idAtual) != null)
            throw ValidationException.of("modelo", "Já existe um produto com esse modelo/SKU.");

        destino.setNome(dto.nome().trim());
        destino.setModelo(modelo);
        destino.setDescricaoCurta(dto.descricaoCurta());
        destino.setPreco(dto.preco());
        destino.setMarca(buscarMarcaOuFalhar(dto.idMarca()));
        destino.setSubcategoria(buscarSubcategoriaOuFalhar(dto.idSubcategoria()));
        destino.setFornecedor(buscarFornecedorOuFalhar(dto.idFornecedor()));
        destino.setStatus(status);
        destino.setDestaque(Boolean.TRUE.equals(dto.destaque()));
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
