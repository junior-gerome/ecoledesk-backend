// com.school.gestionuser.domain.port.out.BusinessProfileRepositoryPort
package com.school.gestionuser.domain.port.out;

import com.school.gestionuser.domain.model.profile.BusinessProfile;
import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepositoryPort {
    BusinessProfile save(BusinessProfile profile);
    Optional<BusinessProfile> findById(String id);
    List<BusinessProfile> findByPersonId(String personId);
    List<BusinessProfile> findByProfileTypeCode(String code, int page, int size);
    boolean existsByPersonIdAndProfileTypeCode(String personId, String profileTypeCode);
    Optional<BusinessProfile> findByPersonIdAndProfileTypeCode(String personId,
                                                                String profileTypeCode);
    List<BusinessProfile> findActiveByProfileTypeCode(String code);
}
