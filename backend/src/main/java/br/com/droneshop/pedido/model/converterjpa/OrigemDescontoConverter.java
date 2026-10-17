package br.com.droneshop.pedido.model.converterjpa;

import br.com.droneshop.pedido.model.OrigemDesconto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrigemDescontoConverter implements AttributeConverter<OrigemDesconto, Long> {

    @Override
    public Long convertToDatabaseColumn(OrigemDesconto origem) {
        return origem == null ? null : origem.getId();
    }

    @Override
    public OrigemDesconto convertToEntityAttribute(Long id) {
        return OrigemDesconto.valueOf(id);
    }
}
