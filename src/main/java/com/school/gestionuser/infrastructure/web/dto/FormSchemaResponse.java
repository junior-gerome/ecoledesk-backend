package com.school.gestionuser.infrastructure.web.dto;

import com.school.gestionuser.application.dto.query.FormSchemaDto;

public class FormSchemaResponse {
    private FormSchemaDto schema;

    public FormSchemaResponse() {}

    public FormSchemaResponse(FormSchemaDto schema) {
        this.schema = schema;
    }

    public FormSchemaDto getSchema() { return schema; }
    public void setSchema(FormSchemaDto schema) { this.schema = schema; }
}