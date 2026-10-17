package br.com.droneshop.identidade.model.converterjpa;

import br.com.droneshop.identidade.model.TipoPessoa;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoPessoaConverter implements AttributeConverter<TipoPessoa, Long> {

    @Override
    public Long convertToDatabaseColumn(TipoPessoa tipo) {
        return (tipo == null) ? null : tipo.getId();
    }

    @Override
    public TipoPessoa convertToEntityAttribute(Long id) {
        return TipoPessoa.valueOf(id);
    }

}
