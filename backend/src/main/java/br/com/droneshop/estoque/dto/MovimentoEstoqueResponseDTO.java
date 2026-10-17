package br.com.droneshop.estoque.dto;

import java.time.LocalDateTime;

import br.com.droneshop.estoque.model.TipoMovimento;
import br.com.droneshop.model.OrigemRegistro;

// autor nulo quando a origem nao e MANUAL (D11)
public record MovimentoEstoqueResponseDTO(
    Long id,
    TipoMovimento tipo,
    OrigemRegistro origem,
    int quantidade,
    int saldoResultante,
    String observacao,
    Autor autor,
    LocalDateTime dataCadastro,
    Variante variante
) {
    public record Autor(Long id, String nome) {
    }

    public record Variante(Long id, String sku, String nome) {
    }
}
