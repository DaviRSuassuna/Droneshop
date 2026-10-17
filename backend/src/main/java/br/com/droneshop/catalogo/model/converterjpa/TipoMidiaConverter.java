package br.com.droneshop.catalogo.model.converterjpa;

import br.com.droneshop.catalogo.model.TipoMidia;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoMidiaConverter implements AttributeConverter<TipoMidia, Long> {

    @Override
    public Long convertToDatabaseColumn(TipoMidia tipo) {
        return tipo == null ? null : tipo.getId();
    }

    @Override
    public TipoMidia convertToEntityAttribute(Long id) {
        return TipoMidia.valueOf(id);
    }
}
