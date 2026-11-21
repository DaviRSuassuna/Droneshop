package br.com.droneshop.devolucao.model.converterjpa;

import br.com.droneshop.devolucao.model.StatusDevolucao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusDevolucaoConverter implements AttributeConverter<StatusDevolucao, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusDevolucao valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public StatusDevolucao convertToEntityAttribute(Long id) {
        return StatusDevolucao.valueOf(id);
    }
}
