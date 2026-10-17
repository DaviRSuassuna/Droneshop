package br.com.droneshop.catalogo.model.converterjpa;

import br.com.droneshop.catalogo.model.EscopoPromocao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EscopoPromocaoConverter implements AttributeConverter<EscopoPromocao, Long> {

    @Override
    public Long convertToDatabaseColumn(EscopoPromocao escopo) {
        return escopo == null ? null : escopo.getId();
    }

    @Override
    public EscopoPromocao convertToEntityAttribute(Long id) {
        return EscopoPromocao.valueOf(id);
    }
}
