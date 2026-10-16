package br.com.droneshop.administracao.model.converterjpa;

import br.com.droneshop.administracao.model.ModuloSistema;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ModuloSistemaConverter implements AttributeConverter<ModuloSistema, Long> {

    @Override
    public Long convertToDatabaseColumn(ModuloSistema modulo) {
        return modulo == null ? null : modulo.getId();
    }

    @Override
    public ModuloSistema convertToEntityAttribute(Long id) {
        return ModuloSistema.valueOf(id);
    }
}
