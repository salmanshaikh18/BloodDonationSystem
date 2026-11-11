package com.group2.blooddonation.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores a {@link BloodGroup} in the database as its label ("A+", "O-", ...),
 * so the table is easy to read and to query with plain SQL.
 */
@Converter
public class BloodGroupConverter implements AttributeConverter<BloodGroup, String> {

    @Override
    public String convertToDatabaseColumn(BloodGroup bloodGroup) {
        return bloodGroup == null ? null : bloodGroup.getLabel();
    }

    @Override
    public BloodGroup convertToEntityAttribute(String label) {
        return label == null ? null : BloodGroup.fromLabel(label);
    }
}
