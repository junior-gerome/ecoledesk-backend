package com.school.gestionuser.domain.model.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value Object immuable représentant une adresse postale.
 * @Embeddable = pas de table propre, colonnes intégrées dans la table parente.
 */
@Embeddable
public class Address {

    @Column(name = "address_street", length = 255)
    private String street;

    @Column(name = "address_city", length = 100)
    private String city;

    @Column(name = "address_state", length = 100)
    private String state;

    @Column(name = "address_postal_code", length = 20)
    private String postalCode;

    @Column(name = "address_country", length = 100)
    private String country;

    // Constructeur protégé pour JPA
    protected Address() {}

    public Address(String street, String city, String state,
                   String postalCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    // Getters (pas de setters = immuable)
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
}