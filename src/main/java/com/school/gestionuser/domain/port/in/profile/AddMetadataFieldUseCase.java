package com.school.gestionuser.domain.port.in.profile;

import com.school.gestionuser.domain.model.profile.MetadataField;

public interface AddMetadataFieldUseCase {
    MetadataField addMetadataField(String profileTypeCode, MetadataField field);
}