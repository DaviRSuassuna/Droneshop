package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.ProdutoDTO;
import br.com.droneshop.catalogo.model.Produto;
import jakarta.validation.Valid;

public interface ProdutoService {

    Produto create(@Valid ProdutoDTO produto);
    void update(long id, @Valid ProdutoDTO produto);
    void delete(long id);
    Produto findById(long id);
    List<Produto> findComFiltro(String nome, Long marcaId, Long categoriaId, Long subcategoriaId, String status,
                               int page, int pageSize);
    long countComFiltro(String nome, Long marcaId, Long categoriaId, Long subcategoriaId, String status);

}
