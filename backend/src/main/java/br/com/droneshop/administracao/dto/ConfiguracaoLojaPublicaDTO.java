package br.com.droneshop.administracao.dto;

public record ConfiguracaoLojaPublicaDTO(
        int prazoArrependimentoDias,
        boolean freteReversoPorContaLoja
) {
}
