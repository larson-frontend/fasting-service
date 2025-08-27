package com.larslab.fasting.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ThemeConverter implements AttributeConverter<UserPreferences.Theme, String> {
    @Override
    public String convertToDatabaseColumn(UserPreferences.Theme attribute) {
        if (attribute == null) return null;
        return attribute.getValue(); // 'light','dark','system'
    }

    @Override
    public UserPreferences.Theme convertToEntityAttribute(String dbData) {
        if (dbData == null) return UserPreferences.Theme.SYSTEM; // default
        return UserPreferences.Theme.fromValue(dbData);
    }
}
