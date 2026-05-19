package com.projeto.gestao.infra.converter;

import com.projeto.gestao.domain.enums.Mercado;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MercadoConverter implements AttributeConverter<Mercado, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Mercado mercado) {
        return mercado == null ? null : mercado.getId();
    }
    @Override
    public Mercado convertToEntityAttribute(Integer dbValue) {
        return Mercado.toEnum(dbValue);
    }
}
