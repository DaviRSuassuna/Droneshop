package br.com.droneshop.estoque.service;

import java.util.List;

import br.com.droneshop.estoque.dto.UnidadeIndividualRequestDTO;
import br.com.droneshop.estoque.model.UnidadeIndividual;

public interface UnidadeIndividualService {

    UnidadeIndividual criar(UnidadeIndividualRequestDTO dto);

    /** Uma unidade DISPONIVEL por unidade do item; viram VENDIDA com a garantia calculada. */
    void vincular(Long idPedido, Long idItem, List<Long> idsUnidades);

    /** Cliente informa o registro ANAC do próprio drone (#88). */
    void registrarAnac(Long idUsuario, Long idUnidade, String registroAnac);
}
