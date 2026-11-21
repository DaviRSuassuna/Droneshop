package br.com.droneshop.identidade.dto;

import java.time.LocalDateTime;

import br.com.droneshop.identidade.model.ContaSocial;
import br.com.droneshop.identidade.model.ProvedorSocial;

public record ContaSocialResponseDTO(ProvedorSocial provedor, LocalDateTime dataCadastro) {

    public static ContaSocialResponseDTO de(ContaSocial c) {
        return new ContaSocialResponseDTO(c.getProvedor(), c.getDataCadastro());
    }
}
