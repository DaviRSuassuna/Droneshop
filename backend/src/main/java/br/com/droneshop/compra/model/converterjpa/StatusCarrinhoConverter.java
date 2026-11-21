package br.com.droneshop.compra.model.converterjpa;

import br.com.droneshop.compra.model.StatusCarrinho;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusCarrinhoConverter implements AttributeConverter<StatusCarrinho, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusCarrinho valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public StatusCarrinho convertToEntityAttribute(Long id) {
        return StatusCarrinho.valueOf(id);
    }
}
