package br.com.droneshop.atendimento.model.converterjpa;

import br.com.droneshop.atendimento.model.StatusChamado;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusChamadoConverter implements AttributeConverter<StatusChamado, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusChamado valor) {
        return valor == null ? null : valor.getId();
    }

    @Override
    public StatusChamado convertToEntityAttribute(Long id) {
        return StatusChamado.valueOf(id);
    }
}
