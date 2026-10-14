package br.com.droneshop.catalogo.model.converterjpa;

import br.com.droneshop.catalogo.model.StatusCategoria;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusCategoriaConverter implements AttributeConverter<StatusCategoria, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusCategoria status) {
        return (status == null) ? null : status.getId();
    }

    @Override
    public StatusCategoria convertToEntityAttribute(Long id) {
        return StatusCategoria.valueOf(id);
    }

}
