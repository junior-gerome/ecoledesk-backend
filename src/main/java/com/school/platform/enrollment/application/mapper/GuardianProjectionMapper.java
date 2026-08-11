package com.school.platform.enrollment.application.mapper;

import com.school.platform.enrollment.application.dto.guardian.GuardianBasicDTO;
import com.school.platform.enrollment.application.dto.guardian.GuardianFullDTO;
import com.school.platform.enrollment.application.dto.guardian.GuardianMediumDTO;
import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.identityaccess.application.mapper.PersonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PersonMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuardianProjectionMapper {

    @Mapping(target = "person", source = "person")
    GuardianBasicDTO toBasicDTO(Guardian entity);

    @Mapping(target = "person", source = "person")
    @Mapping(target = "childrenCount", expression = "java(entity.getStudentGuardians() == null ? 0 : entity.getStudentGuardians().size())")
    GuardianMediumDTO toMediumDTO(Guardian entity);

    @Mapping(target = "person", source = "person")
    @Mapping(target = "students", expression = "java(mapStudents(entity))")
    GuardianFullDTO toFullDTO(Guardian entity);

    List<GuardianBasicDTO> toBasicDTOList(List<Guardian> entities);

    List<GuardianMediumDTO> toMediumDTOList(List<Guardian> entities);

    default List<GuardianFullDTO.GuardianStudentDTO> mapStudents(Guardian entity) {
        if (entity.getStudentGuardians() == null) return List.of();
        return entity.getStudentGuardians().stream()
                .map(sg -> {
                    var studentBasic = new com.school.platform.enrollment.application.dto.student.StudentBasicDTO();
                    if (sg.getStudent() != null) {
                        studentBasic.setId(sg.getStudent().getId());
                        studentBasic.setStudentNumber(sg.getStudent().getStudentNumber());
                        studentBasic.setCurrentLevel(sg.getStudent().getCurrentLevel());
                        studentBasic.setActive(sg.getStudent().getActive());
                        if (sg.getStudent().getPerson() != null) {
                            var p = new com.school.platform.identityaccess.application.dto.person.PersonBasicDTO();
                            p.setId(sg.getStudent().getPerson().getId());
                            p.setFirstName(sg.getStudent().getPerson().getFirstName());
                            p.setLastName(sg.getStudent().getPerson().getLastName());
                            p.setEmail(sg.getStudent().getPerson().getEmail());
                            p.setPhone(sg.getStudent().getPerson().getPhone());
                            p.setGender(sg.getStudent().getPerson().getGender());
                            studentBasic.setPerson(p);
                        }
                    }
                    return new GuardianFullDTO.GuardianStudentDTO(
                            studentBasic,
                            sg.getRelationshipType() == null ? null : sg.getRelationshipType().name());
                })
                .collect(Collectors.toList());
    }
}
