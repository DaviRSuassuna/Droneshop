package br.com.droneshop.catalogo.model.converterjpa;

import br.com.droneshop.catalogo.model.TipoProduto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoProdutoConverter implements AttributeConverter<TipoProduto, Long> {

    @Override
    public Long convertToDatabaseColumn(TipoProduto tipo) {
        return (tipo == null) ? null : tipo.getId();
    }

    @Override
    public TipoProduto convertToEntityAttribute(Long id) {
        return TipoProduto.valueOf(id);
    }

}
