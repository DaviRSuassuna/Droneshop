package br.com.droneshop.atendimento.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.droneshop.atendimento.model.Pergunta;
import br.com.droneshop.atendimento.model.RespostaPergunta;
import br.com.droneshop.atendimento.model.StatusModeracao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTOs de perguntas e respostas sobre produtos (#40). */
public final class PerguntaDTOs {

    private PerguntaDTOs() {
    }

    public record PerguntaRequestDTO(
            @NotBlank(message = "Escreva a pergunta (back)") @Size(min = 10, max = 1000, message = "A pergunta deve ter de 10 a 1000 caracteres (back)") String texto) {
    }

    public record RespostaRequestDTO(
            @NotBlank(message = "Escreva a resposta (back)") @Size(max = 2000, message = "Resposta com até 2000 caracteres (back)") String texto) {
    }

    public record ModeracaoRequestDTO(@NotNull(message = "Informe o status (back)") Long idStatus) {
    }

    public record Autor(String nome) {
    }

    public record ProdutoRef(Long id, String nome, String slug) {
    }

    public record RespostaResponseDTO(Long id, String texto, Autor autor, boolean oficial,
            LocalDateTime dataCadastro) {
        public static RespostaResponseDTO from(RespostaPergunta r) {
            String nome = r.isOficial() ? "Aeria Drones" : primeiroNome(r.getAutor().getNome());
            return new RespostaResponseDTO(r.getId(), r.getTexto(), new Autor(nome), r.isOficial(),
                    r.getDataCadastro());
        }
    }

    public record PerguntaResponseDTO(Long id, StatusModeracao status, String texto, Autor autor, ProdutoRef produto,
            List<RespostaResponseDTO> respostas, LocalDateTime dataCadastro) {
        public static PerguntaResponseDTO from(Pergunta p) {
            return new PerguntaResponseDTO(p.getId(), p.getStatus(), p.getTexto(),
                    new Autor(primeiroNome(p.getCliente().getNome())),
                    new ProdutoRef(p.getProduto().getId(), p.getProduto().getNome(), p.getProduto().getSlug()),
                    p.getRespostas().stream().map(RespostaResponseDTO::from).toList(), p.getDataCadastro());
        }
    }

    /** Na loja aparece só o primeiro nome de quem perguntou ou respondeu (RNF03). */
    static String primeiroNome(String nome) {
        if (nome == null || nome.isBlank())
            return "Cliente";
        return nome.trim().split("\\s+")[0];
    }
}
