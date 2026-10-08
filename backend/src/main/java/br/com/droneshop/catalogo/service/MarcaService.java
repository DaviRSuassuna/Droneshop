package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.MarcaDTO;
import br.com.droneshop.catalogo.model.Marca;
import jakarta.validation.Valid;

public interface MarcaService {

    Marca create(@Valid MarcaDTO marca);
    void update(long id, @Valid MarcaDTO marca);
    void delete(long id);
    Marca findById(long id);
    Marca findBySlug(String slug);
    List<Marca> findAll(Integer page, Integer pageSize);
    List<Marca> findByNome(String nome, Integer page, Integer pageSize);
    long count();
    long count(String nome);

}
