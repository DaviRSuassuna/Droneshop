package br.com.droneshop.endereco.service;

import java.util.List;

import br.com.droneshop.endereco.model.Municipio;

public interface MunicipioService {

    List<Municipio> findByEstado(Long idEstado);
    Municipio findById(long id);

}
