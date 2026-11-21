package br.com.droneshop.identidade.model.converterjpa;

import br.com.droneshop.identidade.model.ProvedorSocial;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProvedorSocialConverter implements AttributeConverter<ProvedorSocial, Long> {

    @Override
    public Long convertToDatabaseColumn(ProvedorSocial valor) {
        return (valor == null) ? null : valor.getId();
    }

    @Override
    public ProvedorSocial convertToEntityAttribute(Long id) {
        return ProvedorSocial.valueOf(id);
    }
}
