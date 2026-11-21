package br.com.droneshop.pagamento.model.converterjpa;

import br.com.droneshop.pagamento.model.StatusPagamento;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusPagamentoConverter implements AttributeConverter<StatusPagamento, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusPagamento valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public StatusPagamento convertToEntityAttribute(Long id) {
        return StatusPagamento.valueOf(id);
    }
}
