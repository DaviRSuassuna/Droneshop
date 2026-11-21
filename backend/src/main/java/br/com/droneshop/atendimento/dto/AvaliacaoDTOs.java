package br.com.droneshop.atendimento.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.atendimento.model.Avaliacao;
import br.com.droneshop.atendimento.model.StatusModeracao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTOs das avaliações (#39). */
public final class AvaliacaoDTOs {

    private AvaliacaoDTOs() {
    }

    public record AvaliacaoRequestDTO(
            @NotNull(message = "Informe a nota (back)") @Min(value = 1, message = "A nota vai de 1 a 5 (back)") @Max(value = 5, message = "A nota vai de 1 a 5 (back)") Integer nota,
            @Size(max = 2000, message = "Comentário com até 2000 caracteres (back)") String comentario) {
    }

    public record AvaliacaoResponseDTO(Long id, int nota, String comentario, boolean compraVerificada,
            StatusModeracao status, PerguntaDTOs.Autor autor, PerguntaDTOs.ProdutoRef produto,
            LocalDateTime dataCadastro) {
        public static AvaliacaoResponseDTO from(Avaliacao a) {
            return new AvaliacaoResponseDTO(a.getId(), a.getNota(), a.getComentario(), a.isCompraVerificada(),
                    a.getStatus(), new PerguntaDTOs.Autor(PerguntaDTOs.primeiroNome(a.getCliente().getNome())),
                    new PerguntaDTOs.ProdutoRef(a.getProduto().getId(), a.getProduto().getNome(),
                            a.getProduto().getSlug()),
                    a.getDataCadastro());
        }
    }

    /** Resumo público: média e quantidade das aprovadas, com os itens. */
    public record AvaliacoesProdutoDTO(Double media, long quantidade, List<AvaliacaoResponseDTO> itens) {
    }
}
