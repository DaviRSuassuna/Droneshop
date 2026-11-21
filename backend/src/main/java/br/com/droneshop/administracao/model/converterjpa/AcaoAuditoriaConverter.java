package br.com.droneshop.administracao.model.converterjpa;

import br.com.droneshop.administracao.model.AcaoAuditoria;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AcaoAuditoriaConverter implements AttributeConverter<AcaoAuditoria, Long> {

    @Override
    public Long convertToDatabaseColumn(AcaoAuditoria valor) {
        return valor == null ? null : valor.getId();
    }

    @Override
    public AcaoAuditoria convertToEntityAttribute(Long id) {
        return AcaoAuditoria.valueOf(id);
    }
}
