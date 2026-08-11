# Architecture cible Enrollment

## Agregats

`PreEnrollment` est la racine du dossier d'admission : candidat (identite embarquee), responsables declarés, documents, frais de preinscription et decision. Etats : DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CANCELLED, EXPIRED. Il ne cree jamais Student.

`Enrollment` est la racine de l'inscription officielle : reference PreEnrollment approuvee, Student, AnneeScolaire, ClasseRoom, type et statut. Etats : PENDING_CONFIRMATION, CONFIRMED, CANCELLED, WITHDRAWN, COMPLETED. Il conserve l'historique.

Le profil Student et StudentGuardian sont crees ou rattaches seulement lors de la confirmation. La finance reste separee : PreEnrollmentFeePayment pour les frais de dossier, TuitionPaymentPlan et PaymentInstallment pour la scolarite.

## Invariants

- un PreEnrollment soumis possede candidat, annee, niveau, responsable principal contactable, documents requis et preuve de frais/exoneration;
- seules les demandes SUBMITTED/UNDER_REVIEW sont decidees; rejet et annulation exigent un motif;
- une Enrollment provient d'une demande APPROVED; une seule inscription active par eleve/annee est admise;
- la capacite, l'annee ouverte et la coherence niveau/classe sont verifiees a confirmation;
- les tranches sont des lignes de donnees, jamais une enum figée.