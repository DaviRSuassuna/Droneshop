package br.com.droneshop.catalogo.model.converterjpa;

import br.com.droneshop.catalogo.model.StatusMarca;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusMarcaConverter implements AttributeConverter<StatusMarca, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusMarca status) {
        return (status == null) ? null : status.getId();
    }

    @Override
    public StatusMarca convertToEntityAttribute(Long id) {
        return StatusMarca.valueOf(id);
    }

}
