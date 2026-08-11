package com.school.platform.identityaccess.domain.model.valueobject;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
    @Column(name = "line") private String line;
    @Column(name = "city") private String city;
    @Column(name = "region") private String region;
    @Column(name = "country") private String country;
    @Column(name = "complement") private String complement;
    protected Address() { }

    private Address(String line, String city, String region, String country, String complement) {
        this.line = trim(line);
        this.city = trim(city);
        this.region = trim(region);
        this.country = trim(country);
        this.complement = trim(complement);
    }
    
    public static Address of(String line, String city, String region, String country, String complement) {
        return line == null && city == null && region == null && country == null && complement == null ? null
                : new Address(line, city, region, country, complement);
    }
    
    public String line() {
        return line;
    }
    
    public String city() {
        return city;
    }
    
    public String region() { return region; }

    public String country() { return country; }

    public String complement() {
        return complement;
    }
    
    private static String trim(String value) { return value == null ? null : value.trim(); }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Address address))
            return false;
        return Objects.equals(line, address.line) && Objects.equals(city, address.city)
                && Objects.equals(region, address.region) && Objects.equals(country, address.country)
                && Objects.equals(complement, address.complement);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(line, city, region, country, complement);
        
    }

}
