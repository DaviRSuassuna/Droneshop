package br.com.droneshop.devolucao.model.converterjpa;

import br.com.droneshop.devolucao.model.ResolucaoDevolucao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ResolucaoDevolucaoConverter implements AttributeConverter<ResolucaoDevolucao, Long> {

    @Override
    public Long convertToDatabaseColumn(ResolucaoDevolucao valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public ResolucaoDevolucao convertToEntityAttribute(Long id) {
        return ResolucaoDevolucao.valueOf(id);
    }
}
