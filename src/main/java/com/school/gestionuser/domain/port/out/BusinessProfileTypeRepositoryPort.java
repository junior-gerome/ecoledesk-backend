package com.school.gestionuser.domain.port.out;

import com.school.gestionuser.domain.model.profile.BusinessProfileType;
import java.util.List;
import java.util.Optional;

public interface BusinessProfileTypeRepositoryPort {
    Optional<BusinessProfileType> findById(String id);
    Optional<BusinessProfileType> findByCode(String code);
    List<BusinessProfileType> findAll();
    BusinessProfileType save(BusinessProfileType profileType);
    void delete(BusinessProfileType profileType);
}