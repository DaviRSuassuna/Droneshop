package br.com.droneshop.administracao.service;

import br.com.droneshop.administracao.dto.ConfiguracaoLojaRequestDTO;
import br.com.droneshop.administracao.model.ConfiguracaoLoja;

public interface ConfiguracaoLojaService {
    ConfiguracaoLoja buscar();

    void atualizar(ConfiguracaoLojaRequestDTO dto);
}
