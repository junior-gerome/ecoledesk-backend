package com.school.platform.billing.domain.model;

import com.school.platform.academic.domain.model.ClasseRoom;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.school.platform.billing.domain.model.TypePaiement;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "classeRoom")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "montant")
public class Montant {
  
  @Id
  @EqualsAndHashCode.Include
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "count", nullable = false)
  private BigDecimal count;

  @ManyToOne
  @JoinColumn(name = "classe_room_id", nullable = false)
  private ClasseRoom classeRoom;

  @Enumerated(EnumType.STRING)
  @Column(name ="type_paiement", nullable = false)
  private TypePaiement typePaiement;

}
