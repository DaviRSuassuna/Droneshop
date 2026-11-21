package br.com.droneshop.dto;

/** Resposta das rotinas disparadas por POST /admin/rotinas/{nome} (D33). */
public record RotinaResponseDTO(int processados) {
}
