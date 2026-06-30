package com.school.gestionuser.domain.port.in.profile;

import com.school.gestionuser.domain.model.profile.BusinessProfile;
import java.util.Map;

public interface UpdateProfileFieldValuesUseCase {
    BusinessProfile updateFieldValues(String profileId, Map<String, String> fieldValues);
}