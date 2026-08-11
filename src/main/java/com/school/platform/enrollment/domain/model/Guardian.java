package com.school.platform.enrollment.domain.model;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import com.school.platform.identityaccess.domain.model.Person;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Profil légal d'une personne responsable d'un ou plusieurs élèves. */
@Entity
@Table(name = "guardians")
@Getter
@Setter
@NoArgsConstructor
public class Guardian extends BaseEntity {

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @OneToMany(mappedBy = "guardian")
    private Set<StudentGuardian> studentGuardians = new HashSet<>();
}
