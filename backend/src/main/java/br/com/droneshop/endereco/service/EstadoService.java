package br.com.droneshop.endereco.service;

import java.util.List;

import br.com.droneshop.endereco.model.Estado;

// Somente leitura: dados oficiais do IBGE (localidades.sql).
public interface EstadoService {

    Estado findById(long id);
    Estado findBySigla(String sigla);
    List<Estado> findAll(int page, int pageSize);
    List<Estado> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);

}
