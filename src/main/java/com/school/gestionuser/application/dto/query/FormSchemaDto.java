package com.school.gestionuser.application.dto.query;

import com.school.gestionuser.domain.model.profile.FieldType;
import java.util.ArrayList;
import java.util.List;

public class FormSchemaDto {
    private String profileTypeCode;
    private String profileTypeName;
    private List<FieldDto> fields = new ArrayList<>();

    public String getProfileTypeCode() { return profileTypeCode; }
    public void setProfileTypeCode(String profileTypeCode) { this.profileTypeCode = profileTypeCode; }
    public String getProfileTypeName() { return profileTypeName; }
    public void setProfileTypeName(String profileTypeName) { this.profileTypeName = profileTypeName; }
    public List<FieldDto> getFields() { return fields; }
    public void setFields(List<FieldDto> fields) { this.fields = fields == null ? new ArrayList<>() : fields; }

    public static class FieldDto {
        private String code;
        private String label;
        private FieldType type;
        private boolean required;
        private List<OptionDto> options = new ArrayList<>();

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public FieldType getType() { return type; }
        public void setType(FieldType type) { this.type = type; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public List<OptionDto> getOptions() { return options; }
        public void setOptions(List<OptionDto> options) { this.options = options == null ? new ArrayList<>() : options; }
    }

    public static class OptionDto {
        private String value;
        private String label;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
}