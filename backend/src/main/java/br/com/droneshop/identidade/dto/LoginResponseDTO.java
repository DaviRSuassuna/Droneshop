package br.com.droneshop.identidade.dto;

import java.util.List;

public record LoginResponseDTO(String accessToken, long expiraEm, List<String> papeis) {
}
