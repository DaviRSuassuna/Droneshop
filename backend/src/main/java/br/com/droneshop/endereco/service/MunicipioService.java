package br.com.droneshop.endereco.service;

import java.util.List;

import br.com.droneshop.endereco.model.Municipio;

// Somente leitura: dados oficiais do IBGE (localidades.sql).
public interface MunicipioService {

    Municipio findById(long id);
    // sao 5.571 municipios: a listagem exige o estado
    List<Municipio> findByEstado(Long idEstado, int page, int pageSize);
    List<Municipio> findByNome(String nome, int page, int pageSize);
    long count(Long idEstado);
    long count(String nome);

}
