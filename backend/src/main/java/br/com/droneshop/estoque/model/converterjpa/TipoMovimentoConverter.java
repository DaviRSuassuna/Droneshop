package br.com.droneshop.estoque.model.converterjpa;

import br.com.droneshop.estoque.model.TipoMovimento;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoMovimentoConverter implements AttributeConverter<TipoMovimento, Long> {

    @Override
    public Long convertToDatabaseColumn(TipoMovimento tipo) {
        return (tipo == null) ? null : tipo.getId();
    }

    @Override
    public TipoMovimento convertToEntityAttribute(Long id) {
        return TipoMovimento.valueOf(id);
    }

}
