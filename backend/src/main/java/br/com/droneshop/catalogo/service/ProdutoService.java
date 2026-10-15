package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.model.Produto;

// Consulta e exclusao de todos os tipos; cadastro e alteracao ficam em DroneService e
// AcessorioService (D27).
public interface ProdutoService {

    void delete(long id);
    Produto findById(long id);
    Produto findBySlug(String slug);
    // filtros opcionais: cada um nulo/vazio e ignorado
    List<Produto> findComFiltro(Long idTipo, String nome, Long idMarca, Long idCategoria, Long idSubcategoria,
                               Long idStatus, int page, int pageSize);
    long countComFiltro(Long idTipo, String nome, Long idMarca, Long idCategoria, Long idSubcategoria, Long idStatus);

}
