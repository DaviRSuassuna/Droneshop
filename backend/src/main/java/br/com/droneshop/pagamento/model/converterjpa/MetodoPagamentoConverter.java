package br.com.droneshop.pagamento.model.converterjpa;

import br.com.droneshop.pagamento.model.MetodoPagamento;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MetodoPagamentoConverter implements AttributeConverter<MetodoPagamento, Long> {

    @Override
    public Long convertToDatabaseColumn(MetodoPagamento valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public MetodoPagamento convertToEntityAttribute(Long id) {
        return MetodoPagamento.valueOf(id);
    }
}
