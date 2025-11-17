package com.school.management.dto;

import com.school.management.enums.TypeParent;

import lombok.Data;

@Data
public class ParentDTO {
  private Long id;
  private String lastNameParent;
  private String firstNameParent;
  private String email;
  private String phoneNumber;
  private String address;
  private String professionParent;
  private TypeParent typeParent;

}
