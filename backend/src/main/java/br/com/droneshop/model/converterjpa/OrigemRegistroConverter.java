package br.com.droneshop.model.converterjpa;

import br.com.droneshop.model.OrigemRegistro;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrigemRegistroConverter implements AttributeConverter<OrigemRegistro, Long> {

    @Override
    public Long convertToDatabaseColumn(OrigemRegistro origem) {
        return (origem == null) ? null : origem.getId();
    }

    @Override
    public OrigemRegistro convertToEntityAttribute(Long id) {
        return OrigemRegistro.valueOf(id);
    }

}
