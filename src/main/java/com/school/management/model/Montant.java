package com.school.management.model;

import jakarta.persistence.*;
import lombok.Data;
import com.school.management.enums.TypePaiement;

@Data
@Entity
@Table(name = "montant")
public class Montant {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "count", nullable = false)
  private Long count;

  @ManyToOne
  @JoinColumn(name = "classRoom_id", nullable = false)
  private ClasseRoom classeRoom;

  @Enumerated(EnumType.STRING)
  @Column(name ="type_paiement", nullable = false)
  private TypePaiement typePaiement;

}
