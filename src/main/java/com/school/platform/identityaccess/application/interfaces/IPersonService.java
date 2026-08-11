package com.school.platform.identityaccess.application.interfaces;

import com.school.platform.identityaccess.application.dto.person.PersonFullDTO;
import com.school.platform.identityaccess.application.dto.person.PersonMediumDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPersonService {
    PersonFullDTO create(PersonFullDTO dto);
    PersonFullDTO update(Long id, PersonFullDTO dto);
    PersonFullDTO findById(Long id);
    Page<PersonMediumDTO> findAll(Pageable pageable);
    void delete(Long id);
}
