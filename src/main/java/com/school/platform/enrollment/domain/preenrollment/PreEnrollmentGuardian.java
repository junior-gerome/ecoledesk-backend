package com.school.platform.enrollment.domain.preenrollment;

import com.school.platform.identityaccess.domain.model.BaseEntity; 
import com.school.platform.enrollment.domain.model.RelationshipType; 
import jakarta.persistence.*; 
import lombok.*;

@Entity 
@Table(name="pre_enrollment_guardians") 
@Getter 
@Setter 
@NoArgsConstructor
public class PreEnrollmentGuardian extends BaseEntity { 
  @ManyToOne(fetch=FetchType.LAZY) 
  @JoinColumn(name="pre_enrollment_id",nullable=false) 
  private PreEnrollment preEnrollment; 
  
  @Enumerated(EnumType.STRING) 
  @Column(name="relationship_type",nullable=false,length=30) 
  private RelationshipType relationshipType; 
  
  @Column(name="relationship_details",length=255) 
  private String relationshipDetails; 
  
  @Column(name="first_name",nullable=false,length=100) 
  private String firstName; 
  
  @Column(name="last_name",nullable=false,length=100) 
  private String lastName; 
  
  @Column(length=150) 
  private String email; 
  
  @Column(name="phone_number",length=30) 
  private String phoneNumber; 
  
  @Column(length=255) 
  private String address; 
  
  @Column(name="primary_contact",nullable=false) 
  private boolean primaryContact; 
  
  @Column(name="financial_responsible",nullable=false) 
  private boolean financialResponsible; 
  
  @Column(name="emergency_contact",nullable=false) 
  private boolean emergencyContact; 
  
  public boolean isPrimaryContactable(){
    return primaryContact&&((email!=null&&!email.isBlank())||(phoneNumber!=null&&!phoneNumber.isBlank()));
  }
}
