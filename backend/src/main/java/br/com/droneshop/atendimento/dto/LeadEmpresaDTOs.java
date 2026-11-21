package br.com.droneshop.atendimento.dto;

import java.time.LocalDateTime;

import br.com.droneshop.atendimento.model.LeadEmpresa;
import br.com.droneshop.atendimento.model.StatusLead;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** DTOs da proposta comercial para empresas (D36). */
public final class LeadEmpresaDTOs {

    private LeadEmpresaDTOs() {
    }

    public record LeadEmpresaRequestDTO(
            @NotBlank(message = "Informe a razão social (back)") @Size(max = 150, message = "Razão social com até 150 caracteres (back)") String razaoSocial,
            @NotBlank(message = "Informe o CNPJ (back)") @CnpjValido String cnpj,
            @NotBlank(message = "Informe o nome do contato (back)") @Size(max = 120, message = "Nome com até 120 caracteres (back)") String nomeContato,
            @NotBlank(message = "Informe o e-mail do contato (back)") @Email(message = "E-mail inválido (back)") @Size(max = 150, message = "E-mail com até 150 caracteres (back)") String emailContato,
            @Size(max = 20, message = "Telefone com até 20 caracteres (back)") String telefoneContato,
            @Size(max = 80, message = "Segmento com até 80 caracteres (back)") String segmento,
            @Min(value = 1, message = "Quantidade estimada mínima de 1 (back)") Integer quantidadeEstimada,
            @Size(max = 2000, message = "Mensagem com até 2000 caracteres (back)") String mensagem) {
    }

    public record StatusLeadRequestDTO(@NotNull(message = "Informe o status (back)") Long idStatus) {
    }

    public record LeadEmpresaResponseDTO(Long id, String razaoSocial, String cnpj, String nomeContato,
            String emailContato, String telefoneContato, String segmento, Integer quantidadeEstimada,
            String mensagem, StatusLead status, LocalDateTime dataCadastro) {
        public static LeadEmpresaResponseDTO from(LeadEmpresa l) {
            return new LeadEmpresaResponseDTO(l.getId(), l.getRazaoSocial(), l.getCnpj(), l.getNomeContato(),
                    l.getEmailContato(), l.getTelefoneContato(), l.getSegmento(), l.getQuantidadeEstimada(),
                    l.getMensagem(), l.getStatus(), l.getDataCadastro());
        }
    }
}
