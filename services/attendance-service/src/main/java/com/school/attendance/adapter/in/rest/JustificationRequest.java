package com.school.attendance.adapter.in.rest;

import jakarta.validation.constraints.Size;

public record JustificationRequest(
        @Size(max = 1000) String justificationNote,
        boolean justified
) {
}
