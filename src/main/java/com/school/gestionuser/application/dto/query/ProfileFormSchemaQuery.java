package com.school.gestionuser.application.dto.query;

public class ProfileFormSchemaQuery {
    private String profileTypeCode;

    public ProfileFormSchemaQuery() {}

    public ProfileFormSchemaQuery(String profileTypeCode) {
        this.profileTypeCode = profileTypeCode;
    }

    public String getProfileTypeCode() { return profileTypeCode; }
    public void setProfileTypeCode(String profileTypeCode) { this.profileTypeCode = profileTypeCode; }
}