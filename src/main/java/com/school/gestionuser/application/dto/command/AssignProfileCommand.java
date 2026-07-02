package com.school.gestionuser.application.dto.command;

import java.util.HashMap;
import java.util.Map;

public class AssignProfileCommand {
    private String personId;
    private String profileTypeCode;
    private Map<String, String> fieldValues = new HashMap<>();
    private String assignedBy;

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }
    public String getProfileTypeCode() { return profileTypeCode; }
    public void setProfileTypeCode(String profileTypeCode) { this.profileTypeCode = profileTypeCode; }
    public Map<String, String> getFieldValues() { return fieldValues; }
    public void setFieldValues(Map<String, String> fieldValues) { this.fieldValues = fieldValues == null ? new HashMap<>() : fieldValues; }
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
}