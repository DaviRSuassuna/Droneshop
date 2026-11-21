package br.com.droneshop.administracao.dto;

/** Só sai na geração, antes de ligar; depois de ligado o segredo nunca volta (#45). */
public record DoisFatoresResponseDTO(String secret, String otpauthUrl) {
}
