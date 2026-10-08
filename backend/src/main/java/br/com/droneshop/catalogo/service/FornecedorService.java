package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.FornecedorDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import jakarta.validation.Valid;

public interface FornecedorService {

    Fornecedor create(@Valid FornecedorDTO fornecedor);
    void update(long id, @Valid FornecedorDTO fornecedor);
    void delete(long id);
    Fornecedor findById(long id);
    Fornecedor findByCnpj(String cnpj);
    List<Fornecedor> findAll(Integer page, Integer pageSize);
    List<Fornecedor> findByNome(String nome, Integer page, Integer pageSize);
    long count();
    long count(String nome);

}
