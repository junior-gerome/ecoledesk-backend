package com.school.platform.academic.application.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.academic.application.interfaces.AcademicYearService;
import com.school.platform.academic.application.dto.year.AcademicYearDTO;
import com.school.platform.academic.application.mapper.AcademicYearMapper;
import com.school.platform.academic.domain.model.AcademicYear;
import com.school.platform.academic.infrastructure.persistence.AcademicYearRepository;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final AcademicYearMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AcademicYearDTO> getAllAcademicYear() {
        return academicYearRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AcademicYearDTO createAcademicYear(AcademicYearDTO dto) {
        // Vérifie si un libellé identique existe déjà avant d'insérer
        if (dto.getLibelleAcademicYear() != null
                && academicYearRepository.existsByLibelleAcademicYear(dto.getLibelleAcademicYear())) {
            throw new IllegalArgumentException(
                    "Une année scolaire avec le libellé '" + dto.getLibelleAcademicYear() + "' existe déjà.");
        }
        AcademicYear entity = mapper.toEntity(dto);
        return mapper.toDto(academicYearRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearDTO getById(Long id) {
        return mapper.toDto(findAcademicYear(id));
    }

    @Override
    @Transactional
    public AcademicYearDTO updateAcademicYear(Long id, AcademicYearDTO dto) {
        AcademicYear academicYear = findAcademicYear(id);
        academicYear.setLibelleAcademicYear(dto.getLibelleAcademicYear());
        academicYear.setDateDebut(dto.getDateDebut());
        academicYear.setDateFin(dto.getDateFin());
        academicYear.setStatutCode(dto.isStatutCode());
        return mapper.toDto(academicYearRepository.save(academicYear));
    }

    @Override
    @Transactional
    public void deleteAcademicYear(Long id) {
        academicYearRepository.delete(findAcademicYear(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearDTO getActiveAcademicYear() {
        return mapper.toDto(
                academicYearRepository.findByStatutCode(true)
                        .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "statutCode", true))
        );
    }

    @Override
    @Transactional
    public AcademicYearDTO activateAcademicYear(Long id) {
        AcademicYear academicYear = academicYearRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));
        academicYearRepository.deactivateActiveYears();
        academicYear.setStatutCode(true);
        return mapper.toDto(academicYearRepository.save(academicYear));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByStatutCode(boolean statutCode) {
        return academicYearRepository.findByStatutCode(statutCode).isPresent();
    }

    @Override
    @Transactional
    public AcademicYearDTO inactivateAcademicYear(Long id) {
        AcademicYear academicYear = findAcademicYear(id);
        academicYear.setStatutCode(false);
        return mapper.toDto(academicYearRepository.save(academicYear));
    }

    private AcademicYear findAcademicYear(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", id));
    }
}
