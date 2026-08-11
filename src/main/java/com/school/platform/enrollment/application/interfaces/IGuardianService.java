package com.school.platform.enrollment.application.interfaces;

import com.school.platform.enrollment.application.dto.GuardianDTO;
import com.school.platform.enrollment.application.dto.guardian.GuardianBasicDTO;
import com.school.platform.enrollment.application.dto.guardian.GuardianFullDTO;
import com.school.platform.enrollment.application.dto.guardian.GuardianMediumDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IGuardianService {

    // Legacy (rétrocompatibilité)
    GuardianDTO createGuardian(GuardianDTO dto);
    GuardianDTO updateGuardian(Long id, GuardianDTO dto);
    void deleteGuardian(Long id);
    GuardianDTO getGuardianById(Long id);
    List<GuardianDTO> getAllGuardians();
    Page<GuardianDTO> getAllGuardians(Pageable pageable);

    // Projections
    List<GuardianBasicDTO> getAllBasic();
    Page<GuardianMediumDTO> getAllMedium(Pageable pageable);
    GuardianFullDTO getFullById(Long id);
}
