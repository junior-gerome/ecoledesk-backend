-- ===========================================================================
-- V4 : Conversion des colonnes ENUM en VARCHAR
-- ---------------------------------------------------------------------------
-- Hibernate utilise @Enumerated(EnumType.STRING) ou @Convert (AttributeConverter)
-- pour mapper les enums Java en String. Avec JPA_DDL_AUTO=validate, la
-- validation échoue si la colonne est de type ENUM MySQL car Hibernate attend
-- VARCHAR. Cette migration convertit toutes les colonnes ENUM en VARCHAR pour
-- aligner le schéma MySQL avec le mapping JPA.
--
-- Règle de longueur :
--   * Pas de @Column(length=N) dans l'entité → VARCHAR(255) (défaut Hibernate)
--   * @Column(length=N) présent→ VARCHAR(N)
-- ===========================================================================

-- ── persons ──────────────────────────────────────────────────────────────────
-- @Convert(converter = GenderConverter.class) @Column(name = "gender")  → VARCHAR(255)
ALTER TABLE persons
    MODIFY COLUMN gender VARCHAR(255) DEFAULT NULL;

-- ── pre_enrollments ──────────────────────────────────────────────────────────
-- gender   : @Column(name="gender")               → VARCHAR(255)
-- status   : @Column(nullable=false, length=30)   → VARCHAR(30)
ALTER TABLE pre_enrollments
    MODIFY COLUMN gender VARCHAR(255) DEFAULT NULL,
    MODIFY COLUMN status VARCHAR(30)  NOT NULL;

-- ── pre_enrollment_documents ─────────────────────────────────────────────────
-- review_status : @Column(name="review_status", nullable=false, length=30) → VARCHAR(30)
ALTER TABLE pre_enrollment_documents
    MODIFY COLUMN review_status VARCHAR(30) NOT NULL;

-- ── pre_enrollment_guardians ─────────────────────────────────────────────────
-- relationship_type : @Column(name="relationship_type", nullable=false, length=30) → VARCHAR(30)
ALTER TABLE pre_enrollment_guardians
    MODIFY COLUMN relationship_type VARCHAR(30) NOT NULL;

-- ── enrollments ──────────────────────────────────────────────────────────────
-- type   : @Column(nullable=false, length=30) → VARCHAR(30)
-- status : @Column(nullable=false, length=30) → VARCHAR(30)
ALTER TABLE enrollments
    MODIFY COLUMN type   VARCHAR(30) NOT NULL,
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

-- ── guardians ────────────────────────────────────────────────────────────────
-- relationship_type : @Column(name="relationship_type", nullable=false, length=20) → VARCHAR(20)
-- Vérification : StudentGuardian.relationship_type length=20, Guardian pas de length → VARCHAR(255)
# ALTER TABLE guardians
#     MODIFY COLUMN relationship_type VARCHAR(255) NOT NULL;

-- ── student_guardians ────────────────────────────────────────────────────────
-- relationship_type : @Column(name="relationship_type", nullable=false, length=20) → VARCHAR(20)
ALTER TABLE student_guardians
    MODIFY COLUMN relationship_type VARCHAR(20) NOT NULL;

-- ── staff_assignments ────────────────────────────────────────────────────────
-- professional_function : @Column(name="professional_function", nullable=false, length=50) → VARCHAR(50)
ALTER TABLE staff_assignments
    MODIFY COLUMN professional_function VARCHAR(50) NOT NULL;

-- ── academic_grade ───────────────────────────────────────────────────────────
-- status : @Column(nullable=false, length=20) → VARCHAR(20)
ALTER TABLE academic_grade
    MODIFY COLUMN status VARCHAR(20) NOT NULL;

-- ── document ─────────────────────────────────────────────────────────────────
-- type_document : @Column(name="type_document", nullable=false) → VARCHAR(255)
ALTER TABLE document
    MODIFY COLUMN type_document VARCHAR(255) NOT NULL;

-- ── montant ──────────────────────────────────────────────────────────────────
-- type_paiement : @Column(name="type_paiement", nullable=false) → VARCHAR(255)
ALTER TABLE montant
    MODIFY COLUMN type_paiement VARCHAR(255) NOT NULL;

-- ── paiements ────────────────────────────────────────────────────────────────
-- type_paiement : @Column(name="type_paiement", nullable=false) → VARCHAR(255)
ALTER TABLE paiements
    MODIFY COLUMN type_paiement VARCHAR(255) NOT NULL;

-- ── payment_installments ─────────────────────────────────────────────────────
-- status : @Column(nullable=false, length=30) → VARCHAR(30)
ALTER TABLE payment_installments
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

-- ── tuition_payment_plans ────────────────────────────────────────────────────
-- status : @Column(nullable=false, length=30) → VARCHAR(30)
ALTER TABLE tuition_payment_plans
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

-- ── support_ticket ───────────────────────────────────────────────────────────
-- priority : @Column(nullable=false, length=20) → VARCHAR(20)
-- status   : @Column(nullable=false, length=20) → VARCHAR(20)
ALTER TABLE support_ticket
    MODIFY COLUMN priority VARCHAR(20) NOT NULL,
    MODIFY COLUMN status   VARCHAR(20) NOT NULL;
