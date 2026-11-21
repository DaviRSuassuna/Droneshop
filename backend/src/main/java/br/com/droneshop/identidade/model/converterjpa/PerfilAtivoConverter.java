package br.com.droneshop.identidade.model.converterjpa;

import br.com.droneshop.identidade.model.PerfilAtivo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PerfilAtivoConverter implements AttributeConverter<PerfilAtivo, Long> {

    @Override
    public Long convertToDatabaseColumn(PerfilAtivo perfil) {
        return (perfil == null) ? null : perfil.getId();
    }

    @Override
    public PerfilAtivo convertToEntityAttribute(Long id) {
        return PerfilAtivo.valueOf(id);
    }
}
