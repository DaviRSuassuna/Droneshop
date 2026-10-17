package br.com.droneshop.estoque.dto;

// Filtro de GET /estoque?situacao=. Nao e enum de dominio (nao vai para o banco).
public enum SituacaoEstoque {
    TODOS,
    ABAIXO_MINIMO,
    ZERADO
}
