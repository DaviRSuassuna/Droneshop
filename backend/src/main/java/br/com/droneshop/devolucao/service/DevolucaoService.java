package br.com.droneshop.devolucao.service;

import java.util.List;

import br.com.droneshop.devolucao.dto.DevolucaoRequestDTO;
import br.com.droneshop.devolucao.model.Devolucao;

public interface DevolucaoService {

    Devolucao solicitar(Long idUsuario, DevolucaoRequestDTO dto);

    /** 409 se a transição não for permitida (D30); grava histórico MANUAL com o autor. */
    void mudarStatus(Long id, Long idStatus, String observacao, Long idAutor);

    Devolucao buscar(Long id);

    Devolucao doCliente(Long id, Long idUsuario);

    List<Devolucao> doCliente(Long idUsuario);
}
