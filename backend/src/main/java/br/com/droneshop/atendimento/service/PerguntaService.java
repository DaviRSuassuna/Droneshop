package br.com.droneshop.atendimento.service;

import java.util.List;

import br.com.droneshop.atendimento.dto.PerguntaDTOs.PerguntaResponseDTO;
import br.com.droneshop.atendimento.dto.PerguntaDTOs.RespostaResponseDTO;
import br.com.droneshop.dto.PageResponse;

public interface PerguntaService {

    PerguntaResponseDTO perguntar(Long idUsuario, String slug, String texto);

    /** Só as aprovadas, com as respostas (RN05). */
    List<PerguntaResponseDTO> aprovadas(String slug);

    RespostaResponseDTO responder(Long idUsuario, Long idPergunta, String texto, boolean oficial);

    PageResponse<PerguntaResponseDTO> listar(Long idStatus, int page, int pageSize);

    void moderar(Long idPergunta, Long idStatus);
}
