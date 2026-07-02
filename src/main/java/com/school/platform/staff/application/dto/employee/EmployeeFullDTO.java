package com.school.platform.staff.application.dto.employee;

import com.school.platform.identityaccess.application.dto.person.PersonFullDTO;
import com.school.platform.staff.application.dto.position.PositionBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFullDTO {
    private Long id;
    private PersonFullDTO person;
    private String employeeNumber;
    private LocalDate hireDate;
    private List<EmployeePositionDTO> positions;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeePositionDTO {
        private Long id;
        private PositionBasicDTO position;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean active;
    }
}
