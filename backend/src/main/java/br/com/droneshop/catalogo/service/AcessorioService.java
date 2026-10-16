package br.com.droneshop.catalogo.service;

import br.com.droneshop.catalogo.dto.AcessorioRequestDTO;
import br.com.droneshop.catalogo.model.Acessorio;
import jakarta.validation.Valid;

public interface AcessorioService {

    Acessorio create(@Valid AcessorioRequestDTO acessorio);
    void update(long id, @Valid AcessorioRequestDTO acessorio);

}
