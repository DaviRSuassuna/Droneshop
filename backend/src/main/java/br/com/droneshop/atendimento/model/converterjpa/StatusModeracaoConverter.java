package br.com.droneshop.atendimento.model.converterjpa;

import br.com.droneshop.atendimento.model.StatusModeracao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusModeracaoConverter implements AttributeConverter<StatusModeracao, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusModeracao valor) {
        return valor == null ? null : valor.getId();
    }

    @Override
    public StatusModeracao convertToEntityAttribute(Long id) {
        return StatusModeracao.valueOf(id);
    }
}
