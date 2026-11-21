package br.com.droneshop.notificacao.model.converterjpa;

import br.com.droneshop.notificacao.model.CategoriaNotificacao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoriaNotificacaoConverter implements AttributeConverter<CategoriaNotificacao, Long> {

    @Override
    public Long convertToDatabaseColumn(CategoriaNotificacao valor) {
        return valor == null ? null : valor.getId();
    }

    @Override
    public CategoriaNotificacao convertToEntityAttribute(Long id) {
        return CategoriaNotificacao.valueOf(id);
    }
}
