package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.ProdutoRequestDTO;
import br.com.droneshop.catalogo.model.Produto;
import jakarta.validation.Valid;

public interface ProdutoService {

    Produto create(@Valid ProdutoRequestDTO produto);
    void update(long id, @Valid ProdutoRequestDTO produto);
    void delete(long id);
    Produto findById(long id);
    Produto findBySlug(String slug);
    // filtros opcionais: cada um nulo/vazio e ignorado
    List<Produto> findComFiltro(String nome, Long idMarca, Long idCategoria, Long idSubcategoria, Long idStatus,
                               int page, int pageSize);
    long countComFiltro(String nome, Long idMarca, Long idCategoria, Long idSubcategoria, Long idStatus);

}
