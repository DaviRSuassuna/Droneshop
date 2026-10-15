package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.SubcategoriaRequestDTO;
import br.com.droneshop.catalogo.model.Subcategoria;
import jakarta.validation.Valid;

public interface SubcategoriaService {

    Subcategoria create(@Valid SubcategoriaRequestDTO subcategoria);
    void update(long id, @Valid SubcategoriaRequestDTO subcategoria);
    void delete(long id);
    Subcategoria findById(long id);
    // idCategoria opcional: sem ele, considera todas as subcategorias
    List<Subcategoria> findAll(Long idCategoria, int page, int pageSize);
    List<Subcategoria> findByNome(String nome, int page, int pageSize);
    long count(Long idCategoria);
    long count(String nome);

}
