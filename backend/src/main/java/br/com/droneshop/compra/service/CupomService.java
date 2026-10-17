package br.com.droneshop.compra.service;

import java.util.List;

import br.com.droneshop.compra.dto.CupomRequestDTO;
import br.com.droneshop.compra.model.Cupom;
import br.com.droneshop.compra.model.UsoCupom;
import jakarta.validation.Valid;

public interface CupomService {
    Cupom create(@Valid CupomRequestDTO dto);
    void update(long id, @Valid CupomRequestDTO dto);
    void delete(long id);
    Cupom findById(long id);
    List<Cupom> findAll(String situacao, String codigo, int page, int pageSize);
    long count(String situacao, String codigo);
    List<UsoCupom> usos(long idCupom, int page, int pageSize);
    long countUsos(long idCupom);
}
