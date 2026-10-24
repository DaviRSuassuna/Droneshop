package br.com.droneshop.catalogo.dto;

import java.util.List;

public record ProdutoLoteResponseDTO(int processados, List<Falha> falhas) {
    public record Falha(Long id, String motivo) {}
}
