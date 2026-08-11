package com.school.platform.academic.application.interfaces;

import java.util.List;

import com.school.platform.academic.application.dto.AcademicYearDTO;

public interface AcademicYearService {

    List<AcademicYearDTO> getAllAcademicYear();

    AcademicYearDTO createAcademicYear(AcademicYearDTO dto);

    AcademicYearDTO getById(Long id);

    AcademicYearDTO updateAcademicYear(Long id, AcademicYearDTO dto);

    void deleteAcademicYear(Long id);

    AcademicYearDTO getActiveAcademicYear();

    AcademicYearDTO activateAcademicYear(Long id);

    boolean existsByStatutCode(boolean statutCode);

    AcademicYearDTO inactivateAcademicYear(Long id);
}
