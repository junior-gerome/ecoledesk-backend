package com.school.gestionuser.application.dto.query;

public class PersonProfilesQuery {
    private String personId;

    public PersonProfilesQuery() {}

    public PersonProfilesQuery(String personId) {
        this.personId = personId;
    }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }
}