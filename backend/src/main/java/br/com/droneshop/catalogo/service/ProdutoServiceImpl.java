package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.model.Acessorio;
import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.catalogo.model.TipoProduto;
import br.com.droneshop.catalogo.repository.ProdutoRepository;
import br.com.droneshop.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProdutoServiceImpl implements ProdutoService {

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    MidiaProdutoService midiaProdutoService;

    // remove pela entidade (e nao por deleteById) para apagar as linhas de produto e do subtipo.
    // Compatibilidade nao e "uso" (D17): os vinculos que apontam para ele saem junto.
    @Override
    @Transactional
    public void delete(long id) {
        Produto produto = buscarProdutoOuFalhar(id);
        midiaProdutoService.excluirDoProduto(id);
        for (Acessorio acessorio : produto.getAcessoriosCompativeis())
            acessorio.getCompativeis().remove(produto);
        produtoRepository.delete(produto);
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
    public List<Produto> findComFiltro(Long idTipo, String nome, Long idMarca, Long idCategoria, Long idSubcategoria,
                                      Long idStatus, int page, int pageSize) {
        return produtoRepository.findComFiltro(TipoProduto.valueOf(idTipo), nome, idMarca, idCategoria,
                        idSubcategoria, StatusProduto.valueOf(idStatus))
                .page(page, pageSize)
                .list();
    }

    @Override
    public long countComFiltro(Long idTipo, String nome, Long idMarca, Long idCategoria, Long idSubcategoria,
                               Long idStatus) {
        return produtoRepository.findComFiltro(TipoProduto.valueOf(idTipo), nome, idMarca, idCategoria,
                        idSubcategoria, StatusProduto.valueOf(idStatus))
                .count();
    }

    private Produto buscarProdutoOuFalhar(long id) {
        Produto produto = produtoRepository.findById(id);
        if (produto == null)
            throw new ResourceNotFoundException("Produto não encontrado.");
        return produto;
    }

}
