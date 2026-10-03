package br.com.droneshop.endereco.service;

import br.com.droneshop.endereco.dto.CepResponseDTO;

public interface CepService {

    CepResponseDTO buscar(String cep);

}
