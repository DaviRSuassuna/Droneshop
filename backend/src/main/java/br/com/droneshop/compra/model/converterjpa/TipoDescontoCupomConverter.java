package br.com.droneshop.compra.model.converterjpa;

import br.com.droneshop.compra.model.TipoDescontoCupom;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoDescontoCupomConverter implements AttributeConverter<TipoDescontoCupom, Long> {

    @Override
    public Long convertToDatabaseColumn(TipoDescontoCupom tipo) {
        return tipo == null ? null : tipo.getId();
    }

    @Override
    public TipoDescontoCupom convertToEntityAttribute(Long id) {
        return TipoDescontoCupom.valueOf(id);
    }
}
