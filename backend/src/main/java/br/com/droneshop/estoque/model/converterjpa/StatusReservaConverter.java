package br.com.droneshop.estoque.model.converterjpa;

import br.com.droneshop.estoque.model.StatusReserva;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusReservaConverter implements AttributeConverter<StatusReserva, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusReserva valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public StatusReserva convertToEntityAttribute(Long id) {
        return StatusReserva.valueOf(id);
    }
}
