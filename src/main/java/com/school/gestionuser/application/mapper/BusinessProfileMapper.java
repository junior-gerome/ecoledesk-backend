package com.school.gestionuser.application.mapper;

import com.school.gestionuser.domain.model.profile.BusinessProfile;
import com.school.gestionuser.infrastructure.web.dto.BusinessProfileResponse;

public class BusinessProfileMapper {
    public BusinessProfileResponse toResponse(BusinessProfile profile) {
        return BusinessProfileResponse.from(profile);
    }
}