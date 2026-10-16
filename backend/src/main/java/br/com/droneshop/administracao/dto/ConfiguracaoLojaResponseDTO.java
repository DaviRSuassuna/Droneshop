package br.com.droneshop.administracao.dto;

import br.com.droneshop.administracao.model.ConfiguracaoLoja;

public record ConfiguracaoLojaResponseDTO(
    Long id,
    String dadosFiscais,
    String cepOrigemFrete,
    String remetentePadrao,
    int prazoArrependimentoDias,
    int prazoGarantiaDias,
    int prazoAnaliseDevolucaoDiasUteis,
    boolean freteReversoPorContaLoja
) {
    public static ConfiguracaoLojaResponseDTO from(ConfiguracaoLoja configuracao) {
        return new ConfiguracaoLojaResponseDTO(
                configuracao.getId(),
                configuracao.getDadosFiscais(),
                configuracao.getCepOrigemFrete(),
                configuracao.getRemetentePadrao(),
                configuracao.getPrazoArrependimentoDias(),
                configuracao.getPrazoGarantiaDias(),
                configuracao.getPrazoAnaliseDevolucaoDiasUteis(),
                configuracao.isFreteReversoPorContaLoja());
    }
}
