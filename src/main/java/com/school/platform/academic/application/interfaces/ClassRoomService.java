package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.ClasseRoomDTO;
import com.school.platform.academic.application.dto.TeacherDTO;
import java.util.List;

public interface ClassRoomService {

    ClasseRoomDTO createClassRoom(ClasseRoomDTO dto);

    ClasseRoomDTO getClassRoomById(Long id);

    ClasseRoomDTO updateClassRoom(Long id, ClasseRoomDTO dto);

    void deleteClassRoom(Long id);

    List<ClasseRoomDTO> getAllClassRooms();

    ClasseRoomDTO getClassRoomByNameClasse(String nameClasse);

    List<ClasseRoomDTO> getClassRoomsBySection(Long sectionId);

    long getTotalClassRooms();

    List<TeacherDTO> getAvailableTeachers();

    void assignTeacher(Long classId, Long staffMemberId);

    void removeTeacher(Long classId);
}
