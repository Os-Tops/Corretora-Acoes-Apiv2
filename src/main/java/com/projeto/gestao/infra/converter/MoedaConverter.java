package com.projeto.gestao.infra.converter;

import com.projeto.gestao.domain.enums.Moeda;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MoedaConverter implements AttributeConverter<Moeda, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Moeda moeda) {
        return moeda == null ? null : moeda.getId();
    }
    @Override
    public Moeda convertToEntityAttribute(Integer dbValue) {
        return Moeda.toEnum(dbValue);
    }
}
