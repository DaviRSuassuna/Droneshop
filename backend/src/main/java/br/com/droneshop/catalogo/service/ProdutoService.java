package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.model.Produto;
import br.com.droneshop.estoque.dto.SituacaoEstoque;

// Consulta e exclusao de todos os tipos; cadastro e alteracao ficam em DroneService,
// AcessorioService e KitService (D27).
public interface ProdutoService {

    void delete(long id);
    Produto findById(long id);
    Produto findBySlug(String slug);
    // filtros opcionais: cada um nulo/vazio e ignorado. idStatus seleciona o status do produto; semKit tira os kits;
    // estoque TODOS nao filtra
    List<Produto> findComFiltro(Long idTipo, String nome, Long idMarca, Long idCategoria, Long idSubcategoria,
                               Long idStatus, Long idGrupo, boolean semKit, SituacaoEstoque estoque,
                               int page, int pageSize);
    long countComFiltro(Long idTipo, String nome, Long idMarca, Long idCategoria, Long idSubcategoria, Long idStatus,
                        Long idGrupo, boolean semKit, SituacaoEstoque estoque);

}
