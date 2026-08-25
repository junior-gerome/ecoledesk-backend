package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.classeroom.ClasseRoomDTO;
import com.school.platform.staff.application.dto.StaffMemberBasicDTO;
import java.util.List;

public interface ClassRoomService {

    ClasseRoomDTO createClassRoom(ClasseRoomDTO dto);

    ClasseRoomDTO getClassRoomById(Long id);

    ClasseRoomDTO updateClassRoom(Long id, ClasseRoomDTO dto);

    void deleteClassRoom(Long id);

    List<ClasseRoomDTO> getAllClassRooms();

    List<ClasseRoomDTO> getAllClassRooms(Long academicYearId);

    ClasseRoomDTO getClassRoomByNameClasse(String nameClasse);

    List<ClasseRoomDTO> getClassRoomsBySection(Long sectionId);

    List<ClasseRoomDTO> getClassRoomsBySection(Long sectionId, Long academicYearId);

    long getTotalClassRooms();

    List<StaffMemberBasicDTO> getTeachers();

    List<StaffMemberBasicDTO> getAvailableTeachers();

    void assignTeacher(Long classId, Long staffMemberId);

    void removeTeacher(Long classId);
}
