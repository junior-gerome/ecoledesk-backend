package com.school.platform.enrollment.domain.model;

import com.school.platform.enrollment.infrastructure.persistence.converter.GenderConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenderConverterTest {

    private final GenderConverter converter = new GenderConverter();

    @Test
    void shouldHandleNullAndEmptyString() {
        assertThat(Gender.fromValue(null)).isNull();
        assertThat(Gender.fromValue("")).isNull();
        assertThat(Gender.fromValue("   ")).isNull();

        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("")).isNull();
        assertThat(converter.convertToEntityAttribute("   ")).isNull();
    }

    @Test
    void shouldParseStandardValues() {
        assertThat(Gender.fromValue("MASCULIN")).isEqualTo(Gender.MASCULIN);
        assertThat(Gender.fromValue("FEMININ")).isEqualTo(Gender.FEMININ);
        assertThat(Gender.fromValue("masculin")).isEqualTo(Gender.MASCULIN);
        assertThat(Gender.fromValue("feminin")).isEqualTo(Gender.FEMININ);

        assertThat(converter.convertToDatabaseColumn(Gender.MASCULIN)).isEqualTo("MASCULIN");
        assertThat(converter.convertToDatabaseColumn(Gender.FEMININ)).isEqualTo("FEMININ");
        assertThat(converter.convertToEntityAttribute("MASCULIN")).isEqualTo(Gender.MASCULIN);
        assertThat(converter.convertToEntityAttribute("FEMININ")).isEqualTo(Gender.FEMININ);
    }

    @Test
    void shouldParseSynonymsAndAbbreviations() {
        assertThat(Gender.fromValue("M")).isEqualTo(Gender.MASCULIN);
        assertThat(Gender.fromValue("F")).isEqualTo(Gender.FEMININ);
        assertThat(Gender.fromValue("MALE")).isEqualTo(Gender.MASCULIN);
        assertThat(Gender.fromValue("FEMALE")).isEqualTo(Gender.FEMININ);
        assertThat(Gender.fromValue("HOMME")).isEqualTo(Gender.MASCULIN);
        assertThat(Gender.fromValue("FEMME")).isEqualTo(Gender.FEMININ);
        assertThat(Gender.fromValue("homme")).isEqualTo(Gender.MASCULIN);
        assertThat(Gender.fromValue("femme")).isEqualTo(Gender.FEMININ);
        assertThat(Gender.fromValue("GARCON")).isEqualTo(Gender.MASCULIN);
        assertThat(Gender.fromValue("FILLE")).isEqualTo(Gender.FEMININ);

        assertThat(converter.convertToEntityAttribute("M")).isEqualTo(Gender.MASCULIN);
        assertThat(converter.convertToEntityAttribute("F")).isEqualTo(Gender.FEMININ);
        assertThat(converter.convertToEntityAttribute("HOMME")).isEqualTo(Gender.MASCULIN);
        assertThat(converter.convertToEntityAttribute("FEMME")).isEqualTo(Gender.FEMININ);
    }

    @Test
    void shouldReturnNullForUnrecognizedValuesWithoutException() {
        assertThat(Gender.fromValue("UNKNOWN_VALUE")).isNull();
        assertThat(converter.convertToEntityAttribute("UNKNOWN_VALUE")).isNull();
    }
}
