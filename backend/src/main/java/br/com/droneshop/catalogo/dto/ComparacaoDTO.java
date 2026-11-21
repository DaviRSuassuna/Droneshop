package br.com.droneshop.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

/** Comparação de drones lado a lado (#44, D14). */
public record ComparacaoDTO(List<Produto> produtos, List<Spec> specs) {

    public record Produto(Long id, String nome, String slug, BigDecimal preco, Double nota, long totalAvaliacoes,
            boolean doGrupo, List<String> itensExtras) {
    }

    /** melhor = índice do maior valor nas specs numéricas em que mais é melhor; null nas demais. */
    public record Spec(String campo, String rotulo, List<Object> valores, boolean diferente, Integer melhor) {
    }
}
