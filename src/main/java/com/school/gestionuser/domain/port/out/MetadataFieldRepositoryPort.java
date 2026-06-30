package com.school.gestionuser.domain.port.out;

import com.school.gestionuser.domain.model.profile.MetadataField;
import java.util.List;
import java.util.Optional;

public interface MetadataFieldRepositoryPort {
    Optional<MetadataField> findById(String id);
    Optional<MetadataField> findByFieldCode(String fieldCode);
    List<MetadataField> findByProfileTypeId(String profileTypeId);
    MetadataField save(MetadataField metadataField);
    void delete(MetadataField metadataField);
}