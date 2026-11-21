package br.com.droneshop.administracao.service;

import br.com.droneshop.administracao.dto.DashboardResponseDTO;

public interface DashboardService {

    // datas AAAA-MM-DD, inclusivas (400 por campo); sem datas, os últimos 30 dias até hoje
    DashboardResponseDTO resumo(String dataInicio, String dataFim);

    /** Vendas por dia do período, para o CSV (nova-dashboard-indicadores). */
    java.util.List<DashboardResponseDTO.VendaDia> vendasPorDia(String dataInicio, String dataFim);
}
