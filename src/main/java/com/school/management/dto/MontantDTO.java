package com.school.management.dto;

import lombok.Data;

import com.school.management.enums.TypePaiement;

@Data
public class MontantDTO {
  private Long id;
  private Long count;
  private Long classeRoomId;
  private TypePaiement typePaiement;

}
