package br.com.droneshop.pagamento.service;

import java.util.List;

import br.com.droneshop.pagamento.dto.CartaoSalvoRequestDTO;
import br.com.droneshop.pagamento.model.CartaoSalvo;

public interface CartaoSalvoService {

    List<CartaoSalvo> listar(Long idUsuario);

    CartaoSalvo salvar(Long idUsuario, CartaoSalvoRequestDTO dto);

    void excluir(Long idUsuario, Long id);

    void tornarPadrao(Long idUsuario, Long id);
}
