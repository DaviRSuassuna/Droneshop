package br.com.droneshop.catalogo.service;

import br.com.droneshop.catalogo.dto.DroneRequestDTO;
import br.com.droneshop.catalogo.model.Drone;
import jakarta.validation.Valid;

public interface DroneService {

    Drone create(@Valid DroneRequestDTO drone);
    void update(long id, @Valid DroneRequestDTO drone);

}
