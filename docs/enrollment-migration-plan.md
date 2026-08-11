# Plan de migration Enrollment

1. Ajouter les tables et agregats PreEnrollment/Enrollment de maniere additive.
2. Introduire les nouveaux services et endpoints, puis maintenir les routes legacy comme adaptateurs de transition.
3. Migrer les donnees `inscription_student` avec journalisation explicite des cas ambigus; ne supprimer aucune ligne ni colonne.
4. Transformer progressivement Parent/StudentParent vers Person/StudentGuardian.
5. Reconciler comptes, classes, annees, responsables et paiements.
6. Desactiver les ecritures legacy, migrer les clients puis seulement supprimer l'ancien modele dans une version ulterieure.

Les correspondances ambigues (VALIDEE sans preuve de paiement, inscription sans classe/annee, parent sans Person) seront listees dans `enrollment-data-migration-report.md` et ne seront pas devinees silencieusement.