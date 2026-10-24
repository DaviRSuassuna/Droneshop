package br.com.droneshop.catalogo.service;

import java.util.List;

import br.com.droneshop.catalogo.dto.GrupoProdutoRequestDTO;
import br.com.droneshop.catalogo.model.GrupoProduto;
import jakarta.validation.Valid;

public interface GrupoProdutoService {

    GrupoProduto create(@Valid GrupoProdutoRequestDTO grupo);
    void update(long id, @Valid GrupoProdutoRequestDTO grupo);
    void delete(long id);
    GrupoProduto findById(long id);
    // filtros opcionais: nome vazio e ativo nulo sao ignorados
    List<GrupoProduto> findComFiltro(String nome, Boolean ativo, int page, int pageSize);
    long count(String nome, Boolean ativo);

}
