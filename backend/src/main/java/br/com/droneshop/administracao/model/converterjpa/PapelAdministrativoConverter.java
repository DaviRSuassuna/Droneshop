package br.com.droneshop.administracao.model.converterjpa;

import br.com.droneshop.administracao.model.PapelAdministrativo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PapelAdministrativoConverter implements AttributeConverter<PapelAdministrativo, Long> {

    @Override
    public Long convertToDatabaseColumn(PapelAdministrativo papel) {
        return papel == null ? null : papel.getId();
    }

    @Override
    public PapelAdministrativo convertToEntityAttribute(Long id) {
        return PapelAdministrativo.valueOf(id);
    }
}
