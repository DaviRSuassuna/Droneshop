package br.com.droneshop.catalogo.model.converterjpa;

import br.com.droneshop.catalogo.model.StatusFornecedor;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusFornecedorConverter implements AttributeConverter<StatusFornecedor, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusFornecedor status) {
        return (status == null) ? null : status.getId();
    }

    @Override
    public StatusFornecedor convertToEntityAttribute(Long id) {
        return StatusFornecedor.valueOf(id);
    }

}
