package br.com.droneshop.identidade.dto;

public record Login2faRequestDTO(String tokenTemporario, String codigo) {
}
