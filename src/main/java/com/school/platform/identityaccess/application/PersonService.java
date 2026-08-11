package com.school.platform.identityaccess.application;

import org.springframework.data.domain.*;

import com.school.platform.identityaccess.application.dto.person.*;

// Legacy interface kept for reference - superseded by application.interfaces.IPersonService
public interface PersonService {
  public PersonFullDTO create(PersonFullDTO dto);

  public PersonFullDTO update(Long id, PersonFullDTO dto);

  public Page<PersonMediumDTO> findAll(Pageable pageable);
  
  public void delete(Long id);
}
