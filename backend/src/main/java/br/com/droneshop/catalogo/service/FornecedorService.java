package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.FornecedorRequestDTO;
import br.com.droneshop.catalogo.model.Fornecedor;
import jakarta.validation.Valid;

public interface FornecedorService {

    Fornecedor create(@Valid FornecedorRequestDTO fornecedor);
    void update(long id, @Valid FornecedorRequestDTO fornecedor);
    void delete(long id);
    Fornecedor findById(long id);
    Fornecedor findByCnpj(String cnpj);
    List<Fornecedor> findAll(int page, int pageSize);
    List<Fornecedor> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);

}
