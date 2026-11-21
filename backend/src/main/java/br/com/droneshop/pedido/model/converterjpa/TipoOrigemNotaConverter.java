package br.com.droneshop.pedido.model.converterjpa;

import br.com.droneshop.pedido.model.TipoOrigemNota;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoOrigemNotaConverter implements AttributeConverter<TipoOrigemNota, Long> {

    @Override
    public Long convertToDatabaseColumn(TipoOrigemNota valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public TipoOrigemNota convertToEntityAttribute(Long id) {
        return TipoOrigemNota.valueOf(id);
    }
}
