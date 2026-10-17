package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.PromocaoRequestDTO;
import br.com.droneshop.catalogo.model.Promocao;
import jakarta.validation.Valid;

public interface PromocaoService {
    Promocao create(@Valid PromocaoRequestDTO dto);
    void update(long id, @Valid PromocaoRequestDTO dto);
    void delete(long id);
    Promocao findById(long id);
    List<Promocao> findAll(String situacao, int page, int pageSize);
    long count(String situacao);
}
