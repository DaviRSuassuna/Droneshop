package br.com.droneshop.atendimento.model.converterjpa;

import br.com.droneshop.atendimento.model.StatusLead;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusLeadConverter implements AttributeConverter<StatusLead, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusLead valor) {
        return valor == null ? null : valor.getId();
    }

    @Override
    public StatusLead convertToEntityAttribute(Long id) {
        return StatusLead.valueOf(id);
    }
}
