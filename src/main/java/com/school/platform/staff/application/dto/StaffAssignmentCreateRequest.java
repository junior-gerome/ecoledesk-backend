package com.school.platform.staff.application.dto;

import com.school.platform.staff.domain.model.StaffPosition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffAssignmentCreateRequest {

    @NotNull(message = "Le membre du personnel est obligatoire")
    private Long staffMemberId;

    @NotNull(message = "La position est obligatoire")
    private StaffPosition position;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    private LocalDate endDate;
}
