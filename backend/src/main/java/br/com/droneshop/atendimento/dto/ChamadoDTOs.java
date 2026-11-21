package br.com.droneshop.atendimento.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.atendimento.model.Chamado;
import br.com.droneshop.atendimento.model.MensagemChamado;
import br.com.droneshop.atendimento.model.StatusChamado;
import br.com.droneshop.atendimento.model.TipoAutorMensagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTOs do chamado de suporte (#38). */
public final class ChamadoDTOs {

    private ChamadoDTOs() {
    }

    public record ChamadoRequestDTO(
            @NotBlank(message = "Informe o assunto (back)") @Size(max = 150, message = "Assunto com até 150 caracteres (back)") String assunto,
            Long idPedido,
            @NotBlank(message = "Escreva a mensagem (back)") @Size(max = 5000, message = "Mensagem com até 5000 caracteres (back)") String mensagem) {
    }

    public record MensagemRequestDTO(
            @NotBlank(message = "Escreva a mensagem (back)") @Size(max = 5000, message = "Mensagem com até 5000 caracteres (back)") String texto) {
    }

    public record StatusChamadoRequestDTO(@NotNull(message = "Informe o status (back)") Long idStatus) {
    }

    public record Ref(Long id, String nome) {
    }

    public record MensagemResponseDTO(Long id, TipoAutorMensagem tipoAutor, String texto, LocalDateTime dataCadastro) {
        public static MensagemResponseDTO from(MensagemChamado m) {
            return new MensagemResponseDTO(m.getId(), m.getTipoAutor(), m.getTexto(), m.getDataCadastro());
        }
    }

    public record ChamadoResponseDTO(Long id, String protocolo, StatusChamado status, String assunto, Ref pedido,
            Ref cliente, Ref atendente, List<MensagemResponseDTO> mensagens, LocalDateTime dataCadastro,
            LocalDateTime dataAlteracao) {

        public static ChamadoResponseDTO from(Chamado c) {
            return new ChamadoResponseDTO(c.getId(), c.getProtocolo(), c.getStatus(), c.getAssunto(),
                    c.getPedido() == null ? null : new Ref(c.getPedido().getId(), null),
                    new Ref(c.getCliente().getId(), c.getCliente().getNome()),
                    c.getAtendente() == null ? null : new Ref(c.getAtendente().getId(), c.getAtendente().getNome()),
                    c.getMensagens().stream().map(MensagemResponseDTO::from).toList(), c.getDataCadastro(),
                    c.getDataAlteracao());
        }
    }
}
