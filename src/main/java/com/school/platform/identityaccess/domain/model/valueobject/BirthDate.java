package com.school.platform.identityaccess.domain.model.valueobject;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BirthDate {
    @Column(name = "value") private LocalDate value;
    protected BirthDate() { }
    private BirthDate(LocalDate value) {
        if (value.isAfter(LocalDate.now())) throw new IllegalArgumentException("birth date cannot be in the future");
        this.value = value;
    }
    public static BirthDate of(LocalDate value) { return value == null ? null : new BirthDate(value); }
    public LocalDate value() { return value; }
    public int ageOn(LocalDate date) { return Period.between(value, Objects.requireNonNull(date, "date must not be null")).getYears(); }
    @Override public boolean equals(Object other) { return other instanceof BirthDate birthDate && Objects.equals(value, birthDate.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
