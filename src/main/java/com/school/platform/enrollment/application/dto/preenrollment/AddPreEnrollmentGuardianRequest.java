package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.model.RelationshipType; 
import lombok.Data;

@Data 
public class AddPreEnrollmentGuardianRequest { 
  private RelationshipType relationshipType; 
  private String relationshipDetails; 
  private String firstName; 
  private String lastName; 
  private String email; 
  private String phoneNumber; 
  private String address; 
  private boolean primaryContact; 
  private boolean financialResponsible; 
  private boolean emergencyContact; 
}
