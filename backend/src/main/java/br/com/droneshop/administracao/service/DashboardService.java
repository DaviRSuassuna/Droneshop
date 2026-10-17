package br.com.droneshop.administracao.service;

import br.com.droneshop.administracao.dto.DashboardResponseDTO;

public interface DashboardService {

    // datas AAAA-MM-DD, inclusivas (400 por campo); sem datas, os últimos 30 dias até hoje
    DashboardResponseDTO resumo(String dataInicio, String dataFim);
}
