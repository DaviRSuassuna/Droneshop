package br.com.droneshop.catalogo.service;

import java.util.List;
import br.com.droneshop.catalogo.dto.CategoriaDTO;
import br.com.droneshop.catalogo.model.Categoria;

public interface CategoriaService {
    Categoria create(CategoriaDTO dto);
    void update(long id, CategoriaDTO dto);
    void delete(long id);
    Categoria findById(long id);
    List<Categoria> findAll(Integer page, Integer pageSize);
}
