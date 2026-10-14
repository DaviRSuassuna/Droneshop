package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.MarcaRequestDTO;
import br.com.droneshop.catalogo.model.Marca;
import jakarta.validation.Valid;

public interface MarcaService {

    Marca create(@Valid MarcaRequestDTO marca);
    void update(long id, @Valid MarcaRequestDTO marca);
    void delete(long id);
    Marca findById(long id);
    Marca findBySlug(String slug);
    List<Marca> findAll(int page, int pageSize);
    List<Marca> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);

}
