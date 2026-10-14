package br.com.droneshop.catalogo.model.converterjpa;

import br.com.droneshop.catalogo.model.StatusProduto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusProdutoConverter implements AttributeConverter<StatusProduto, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusProduto status) {
        return (status == null) ? null : status.getId();
    }

    @Override
    public StatusProduto convertToEntityAttribute(Long id) {
        return StatusProduto.valueOf(id);
    }

}
