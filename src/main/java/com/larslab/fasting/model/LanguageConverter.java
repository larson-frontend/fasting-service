package com.larslab.fasting.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LanguageConverter implements AttributeConverter<UserPreferences.Language, String> {
    @Override
    public String convertToDatabaseColumn(UserPreferences.Language attribute) {
        if (attribute == null) return null;
        return attribute.getCode(); // already lowercase 'en'/'de'
    }

    @Override
    public UserPreferences.Language convertToEntityAttribute(String dbData) {
        if (dbData == null) return UserPreferences.Language.EN; // default
        return UserPreferences.Language.fromCode(dbData);
    }
}
