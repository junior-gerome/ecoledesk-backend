package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import lombok.Data;

import com.school.platform.billing.domain.model.TypePaiement;

@Data
public class MontantDTO {
  private Long id;
  private BigDecimal count;
  private Long classeRoomId;
  private TypePaiement typePaiement;

}
