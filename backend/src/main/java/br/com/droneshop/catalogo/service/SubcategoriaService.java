package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.SubcategoriaDTO;
import br.com.droneshop.catalogo.model.Subcategoria;
import jakarta.validation.Valid;

public interface SubcategoriaService {

    Subcategoria create(@Valid SubcategoriaDTO subcategoria);
    void update(long id, @Valid SubcategoriaDTO subcategoria);
    void delete(long id);
    Subcategoria findById(long id);
    List<Subcategoria> findAll(Long categoriaId, int page, int pageSize);

}
