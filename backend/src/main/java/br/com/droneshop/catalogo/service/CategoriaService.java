package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.CategoriaDTO;
import br.com.droneshop.catalogo.model.Categoria;
import jakarta.validation.Valid;

public interface CategoriaService {

    Categoria create(@Valid CategoriaDTO categoria);
    void update(long id, @Valid CategoriaDTO categoria);
    void delete(long id);
    Categoria findById(long id);
    Categoria findBySlug(String slug);
    List<Categoria> findAll(Integer page, Integer pageSize);
    List<Categoria> findByNome(String nome, Integer page, Integer pageSize);
    long count();
    long count(String nome);

}
