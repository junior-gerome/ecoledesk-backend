package com.school.gestionuser.infrastructure.web.dto;

import com.school.gestionuser.domain.model.profile.BusinessProfile;
import com.school.gestionuser.domain.model.profile.ProfileStatus;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BusinessProfileResponse {
    private String id;
    private String profileTypeCode;
    private String profileTypeName;
    private LocalDate validFrom;
    private LocalDate validTo;
    private ProfileStatus status;
    private Map<String, String> fieldValues = new HashMap<>();

    public static BusinessProfileResponse from(BusinessProfile profile) {
        BusinessProfileResponse response = new BusinessProfileResponse();
        if (profile != null) {
            response.id = profile.getId();
            if (profile.getProfileType() != null) {
                response.profileTypeCode = profile.getProfileType().getCode();
                response.profileTypeName = profile.getProfileType().getName();
            }
            response.validFrom = profile.getValidFrom();
            response.validTo = profile.getValidTo();
            response.status = profile.getStatus();
            response.fieldValues = profile.getAllFieldValuesAsMap();
        }
        return response;
    }

    public String getId() { return id; }
    public String getProfileTypeCode() { return profileTypeCode; }
    public String getProfileTypeName() { return profileTypeName; }
    public LocalDate getValidFrom() { return validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public ProfileStatus getStatus() { return status; }
    public Map<String, String> getFieldValues() { return fieldValues; }
}