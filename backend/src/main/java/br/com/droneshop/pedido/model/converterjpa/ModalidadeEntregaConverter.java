package br.com.droneshop.pedido.model.converterjpa;

import br.com.droneshop.pedido.model.ModalidadeEntrega;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ModalidadeEntregaConverter implements AttributeConverter<ModalidadeEntrega, Long> {

    @Override
    public Long convertToDatabaseColumn(ModalidadeEntrega valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public ModalidadeEntrega convertToEntityAttribute(Long id) {
        return ModalidadeEntrega.valueOf(id);
    }
}
