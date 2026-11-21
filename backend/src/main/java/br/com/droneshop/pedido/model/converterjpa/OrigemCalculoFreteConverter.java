package br.com.droneshop.pedido.model.converterjpa;

import br.com.droneshop.pedido.model.OrigemCalculoFrete;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrigemCalculoFreteConverter implements AttributeConverter<OrigemCalculoFrete, Long> {

    @Override
    public Long convertToDatabaseColumn(OrigemCalculoFrete valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public OrigemCalculoFrete convertToEntityAttribute(Long id) {
        return OrigemCalculoFrete.valueOf(id);
    }
}
