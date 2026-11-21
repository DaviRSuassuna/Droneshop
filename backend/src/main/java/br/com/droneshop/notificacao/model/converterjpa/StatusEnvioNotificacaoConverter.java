package br.com.droneshop.notificacao.model.converterjpa;

import br.com.droneshop.notificacao.model.StatusEnvioNotificacao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusEnvioNotificacaoConverter implements AttributeConverter<StatusEnvioNotificacao, Long> {

    @Override
    public Long convertToDatabaseColumn(StatusEnvioNotificacao valor) {
        return valor == null ? null : valor.getId();
    }

    @Override
    public StatusEnvioNotificacao convertToEntityAttribute(Long id) {
        return StatusEnvioNotificacao.valueOf(id);
    }
}
