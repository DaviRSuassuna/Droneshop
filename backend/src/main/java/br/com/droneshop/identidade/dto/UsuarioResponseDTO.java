package br.com.droneshop.identidade.dto;

import java.util.List;

public record UsuarioResponseDTO(Long id, String nome, String email, List<String> papeis) {
}
