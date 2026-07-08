# Langage Ubiquitaire — Plateforme École Primaire

Ce document définit les termes partagés entre les experts métier et l'équipe technique.
Tout terme ambigu ou spécifique au domaine doit être défini ici et utilisé de manière cohérente
dans le code, les discussions et la documentation.

---

## Bounded Contexts

### 1. Enrollment (Inscription)
### 2. Academic (Académique)
### 3. Billing (Facturation)
### 4. Attendance (Présences)
### 5. Identity & Access (Identité et Accès)
### 6. Reporting (Rapports)
### 7. Staff (Personnel)

---

## Glossaire

### Élève (`Student`)
**Contexte :** Enrollment, Academic, Billing, Attendance

Un enfant physiquement inscrit ou en cours d'inscription dans l'école.
Un élève est identifié par un numéro unique (`studentNumber`) et est rattaché à une `Person`.

> ⚠️ Ambiguïté : Ne pas confondre avec `Person` (entité générique d'identité) ni avec `Parent`.
> Un élève n'est pas encore un élève tant que son inscription n'est pas `VALIDEE` ou `INSCRITE`.

---

### Préinscription (`PreinscriptionStatus`)
**Contexte :** Enrollment

Étape préalable à l'inscription officielle. Elle suit un cycle de vie :

| Statut       | Signification métier                                      |
|--------------|-----------------------------------------------------------|
| `BROUILLON`  | Formulaire commencé mais non soumis                       |
| `EN_ATTENTE` | Soumis, en attente de décision de la direction            |
| `VALIDEE`    | Acceptée par la direction, peut devenir une inscription   |
| `REFUSEE`    | Rejetée par la direction (avec motif obligatoire)         |
| `ANNULEE`    | Annulée par la famille ou l'administration                |
| `INSCRITE`   | Convertie en inscription officielle                       |

> ⚠️ Ambiguïté : `VALIDEE` ≠ `INSCRITE`. Une préinscription validée n'est pas encore une inscription
> tant que le paiement des frais d'inscription n'est pas enregistré.

---

### Inscription (`InscriptionStudent`)
**Contexte :** Enrollment, Billing

Acte officiel qui lie un `Student` à une `ClasseRoom` pour une `AnneeScolaire` donnée.
Une inscription implique un `Montant` (frais associés) et un statut de préinscription.

> ⚠️ Ambiguïté : Le terme "inscription" est utilisé à la fois pour la préinscription et l'inscription
> définitive. Dans le code, `InscriptionStudent` représente toujours l'inscription officielle.

---

### Année Scolaire (`AnneeScolaire`)
**Contexte :** Academic, Enrollment, Billing

Période académique de référence (ex : 2024-2025). Elle a une date de début, une date de fin
et un statut actif (`statutCode`). Une seule année scolaire peut être active à la fois.

---

### Section (`Section`)
**Contexte :** Academic

Regroupement pédagogique de classes partageant un même cycle ou programme
(ex : Maternelle, Primaire). Une section contient plusieurs `ClasseRoom`.

> ⚠️ Ambiguïté : "Section" peut désigner une division administrative dans d'autres contextes.
> Ici, c'est strictement un regroupement pédagogique.

---

### Classe (`ClasseRoom`)
**Contexte :** Academic, Enrollment, Billing

Groupe d'élèves d'un même niveau, encadré par un enseignant titulaire, pour une année scolaire.
Une classe appartient à une `Section` et a une capacité maximale.

> ⚠️ Ambiguïté : Le nom technique `ClasseRoom` est utilisé pour éviter le conflit avec le mot-clé
> Java `class`. Dans les échanges métier, on dit simplement "classe".

---

### Niveau (`niveau` / `level`)
**Contexte :** Academic, Enrollment

Désigne le degré scolaire d'un élève ou d'une classe (ex : CP, CE1, CE2, CM1, CM2).

> ⚠️ Ambiguïté : Le champ `niveau` existe à la fois sur `Teacher` (spécialité de niveau enseigné)
> et sur `ClasseRoom` (niveau de la classe). Ces deux usages sont distincts.

---

### Enseignant (`Teacher`)
**Contexte :** Academic

Membre du corps enseignant affecté à une ou plusieurs classes et matières.
Identifié par son email et son numéro de téléphone (uniques).

> ⚠️ Ambiguïté : Un `Teacher` est distinct d'un `Employee` (personnel administratif/technique)
> et d'un `UserAccount` (compte d'accès à la plateforme). Un enseignant peut avoir un compte
> utilisateur, mais ce n'est pas obligatoire.

---

### Affectation (`Affectation`)
**Contexte :** Academic

Lien formel entre un `Teacher`, une `ClasseRoom`, une `Subject` et une `AnneeScolaire`.
Définit qui enseigne quoi, dans quelle classe et pour quelle année.

---

### Matière (`Subject`)
**Contexte :** Academic

Discipline enseignée (ex : Mathématiques, Français). Chaque matière a un coefficient
qui pondère les notes dans le calcul de la moyenne.

---

### Trimestre (`Trimestre`)
**Contexte :** Academic

Division de l'année scolaire en trois périodes d'évaluation. Chaque trimestre contient
deux `Sequence`.

---

### Séquence (`Sequence`)
**Contexte :** Academic

Sous-période d'évaluation à l'intérieur d'un trimestre. Les notes (`Grade`) sont saisies
par séquence. Deux séquences composent un trimestre.

> ⚠️ Ambiguïté : "Séquence" peut désigner une séquence pédagogique (progression de cours)
> dans d'autres systèmes. Ici, c'est exclusivement une période d'évaluation.

---

### Note (`Grade`)
**Contexte :** Academic

Résultat chiffré (sur 20) obtenu par un élève pour une matière, lors d'une séquence.
Une note a un coefficient, un type d'évaluation et suit un cycle de validation :

| Statut      | Signification métier                              |
|-------------|---------------------------------------------------|
| `DRAFT`     | Saisie en cours, non publiée aux parents          |
| `VALIDATED` | Validée par l'enseignant, visible                 |
| `LOCKED`    | Verrouillée après clôture de la séquence          |

---

### Type d'Évaluation (`AssessmentType`)
**Contexte :** Academic

Nature de l'évaluation ayant produit une note :
`HOMEWORK` (devoir maison), `CLASS_TEST` (interrogation), `MIDTERM_EXAM`, `FINAL_EXAM`,
`PROJECT`, `ORAL_EXAM`, `PARTICIPATION`.

---

### Parent (`Parent`)
**Contexte :** Enrollment

Tuteur légal ou responsable d'un élève. Un parent peut être responsable de plusieurs élèves.
La relation entre un parent et un élève est typée (`RelationshipType` : FATHER, MOTHER, GUARDIAN…).

---

### Paiement (`Paiement`)
**Contexte :** Billing

Versement effectué par une famille pour couvrir tout ou partie d'un `Montant` dû.
Un paiement est lié à une `InscriptionStudent` et peut être annulé (avec motif).

---

### Montant (`Montant`)
**Contexte :** Billing

Tarif officiel défini par l'école pour un type de frais et une classe donnée.
C'est le montant de référence, pas le montant effectivement payé.

> ⚠️ Ambiguïté : `montantPaye` (sur `Paiement`) est le montant réellement versé.
> `Montant` (entité) est le barème de référence. Ne pas confondre les deux.

---

### Type de Paiement (`TypePaiement`)
**Contexte :** Billing

Catégorie du frais facturé. Exemples : `FRAIS_INSCRIPTION`, `FRAIS_SCOLAIRE`,
`FRAIS_TRANSPORT`, `FRAIS_CANTINE`, `FRAIS_UNIFORME`, etc.

---

### Absence (`Absence`)
**Contexte :** Attendance

Enregistrement d'une non-présence d'un élève à l'école pour une date donnée.
Une absence peut être justifiée (`justified = true`) avec une note explicative.

---

### Bulletin (`BulletinDTO`)
**Contexte :** Reporting

Document récapitulatif des notes et appréciations d'un élève pour un trimestre.
Généré en PDF et remis aux parents en fin de trimestre.

---

### Remise (`remise`)
**Contexte :** Billing

Réduction accordée sur le montant dû d'un paiement (ex : bourse, fratrie).
Exprimée en valeur absolue (pas en pourcentage).

---

### CNI (`cniNumber`, `cniPhotoUrl`)
**Contexte :** Academic (Teacher)

Carte Nationale d'Identité. Numéro et photo du document d'identité officiel de l'enseignant,
requis pour le dossier administratif.

---

### Date d'Embauche (`dateEmbauche`)
**Contexte :** Academic (Teacher)

Date à laquelle l'enseignant a rejoint l'établissement. Utilisée pour le calcul de l'ancienneté
et les rapports RH.

---

### École Précédente (`ecolePrecedente`)
**Contexte :** Enrollment (Student)

Nom de l'établissement scolaire fréquenté par l'élève avant son admission.
Information facultative, utile pour le suivi pédagogique.

---

### Reçu (`receiptNumber`)
**Contexte :** Billing

Numéro unique attribué à chaque paiement, servant de justificatif officiel remis à la famille.

---

## Règles métier clés

1. **Une seule année scolaire active** à la fois (`statutCode = true`).
2. **Une inscription = un élève + une classe + une année scolaire** (combinaison unique).
3. **Une note ne peut être saisie** que si l'élève est inscrit dans la classe correspondante.
4. **Une note `LOCKED` ne peut plus être modifiée**, même par un administrateur.
5. **Un paiement annulé** conserve sa trace avec `cancelledAt` et `cancellationReason`.
6. **La préinscription doit être `VALIDEE`** avant de pouvoir créer une inscription officielle.
