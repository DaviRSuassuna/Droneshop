package br.com.droneshop.estoque.model.converterjpa;

import br.com.droneshop.estoque.model.StatusUnidade;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusUnidadeConverter implements AttributeConverter<StatusUnidade, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusUnidade valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public StatusUnidade convertToEntityAttribute(Long id) {
        return StatusUnidade.valueOf(id);
    }
}
