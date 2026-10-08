package br.com.droneshop.endereco.service;

import java.util.List;

import br.com.droneshop.endereco.model.Estado;

public interface EstadoService {

    List<Estado> findAll();
    Estado findById(long id);

}
