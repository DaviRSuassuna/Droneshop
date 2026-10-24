package br.com.droneshop.catalogo.service;

import br.com.droneshop.catalogo.dto.KitRequestDTO;
import br.com.droneshop.catalogo.model.Kit;
import jakarta.validation.Valid;

public interface KitService {

    Kit create(@Valid KitRequestDTO kit);
    void update(long id, @Valid KitRequestDTO kit);

}
