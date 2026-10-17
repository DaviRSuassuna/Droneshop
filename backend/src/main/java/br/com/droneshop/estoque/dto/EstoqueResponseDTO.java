package br.com.droneshop.estoque.dto;

// Item de GET /estoque: saldo de uma variante. No kit (D7) estoqueFisico e nulo e o
// disponivel vem dos componentes; reservado e 0 ate a #23.
public record EstoqueResponseDTO(
    Variante variante,
    Produto produto,
    Integer estoqueFisico,
    int reservado,
    int disponivel,
    int estoqueMinimo,
    boolean abaixoDoMinimo
) {
    public record Variante(Long id, String sku, String nome, boolean ehKit, boolean ativa) {
    }

    public record Produto(Long id, String nome) {
    }
}
