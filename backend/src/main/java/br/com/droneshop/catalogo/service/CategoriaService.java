package br.com.droneshop.catalogo.service;

import br.com.droneshop.catalogo.dto.CategoriaOrdemRequestDTO;
import java.util.List;

import br.com.droneshop.catalogo.dto.CategoriaRequestDTO;
import br.com.droneshop.catalogo.model.Categoria;
import jakarta.validation.Valid;

public interface CategoriaService {
    void reordenar(CategoriaOrdemRequestDTO dto);

    Categoria create(@Valid CategoriaRequestDTO categoria);
    void update(long id, @Valid CategoriaRequestDTO categoria);
    void delete(long id);
    Categoria findById(long id);
    Categoria findBySlug(String slug);
    List<Categoria> findAll(int page, int pageSize);
    List<Categoria> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);

}
