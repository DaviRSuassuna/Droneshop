package br.com.droneshop.identidade.mapper;

import br.com.droneshop.identidade.dto.ClienteResponseDTO;
import br.com.droneshop.identidade.dto.PerfilClienteResponseDTO;
import br.com.droneshop.identidade.model.PerfilCliente;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static ClienteResponseDTO toConta(PerfilCliente p) {
        return new ClienteResponseDTO(p.getUsuario().getId(), p.getNome(), p.getUsuario().getEmail(),
                p.getTipoPessoa(), p.getDocumento(), p.getTelefone());
    }

    public static PerfilClienteResponseDTO toPerfil(PerfilCliente p) {
        return new PerfilClienteResponseDTO(p.getNome(), p.getUsuario().getEmail(), p.getTipoPessoa(),
                p.getDocumento(), p.getTelefone());
    }
}
