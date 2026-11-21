package br.com.droneshop.atendimento.model.converterjpa;

import br.com.droneshop.atendimento.model.TipoAutorMensagem;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoAutorMensagemConverter implements AttributeConverter<TipoAutorMensagem, Long> {

    @Override
    public Long convertToDatabaseColumn(TipoAutorMensagem valor) {
        return valor == null ? null : valor.getId();
    }

    @Override
    public TipoAutorMensagem convertToEntityAttribute(Long id) {
        return TipoAutorMensagem.valueOf(id);
    }
}
