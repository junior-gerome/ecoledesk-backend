# ============================================================================
# SIMULATION DU WORKFLOW DE PRE-INSCRIPTION / INSCRIPTION
# Backend : GSB Platform (Spring Boot, profil dev)
#
# Reproduit les 4 etats du mock data (DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED)
# + la branche REJECTED, en passant par les vrais endpoints REST, avec
# persistance en base MySQL.
#
# Scenarios rejoues (base : MockDataBootstrap) :
#   A. Amadou Kane   (CP)      -> parcours COMPLET jusqu'a APPROVED + inscription CONFIRMED
#   B. Salimata Sy   (CE1)     -> SUBMITTED (depose, en attente de revue)
#   C. Oumar Cisse   (6eme)    -> UNDER_REVIEW (revue demarree, documents a verifier)
#   D. Aissatou Mbaye (2nde)   -> REJECTED (document refuse + rejet dossier)
#
# Note : les endpoints de commande renvoient un DTO leger (id, numero, statut).
# Le detail complet (candidat, responsables, documents, paiement) est
# recupere via GET /api/pre-enrollments/{id} a chaque etape.
#
# Pre-requis : backend demarre sur http://localhost:8080 (profil dev).
# Usage      : powershell -ExecutionPolicy Bypass -File scripts\simulate-pre-enrollment-workflow.ps1
# ============================================================================

$ErrorActionPreference = 'Stop'
$baseUrl = 'http://localhost:8080/api'
$academicYearId = 1  # 2026-2027 (active, voir seed academique)

# --- Authentification admin (voir variable_environnement/.env) --------------
function Get-ApiHeaders {
    $body = @{ email = 'admin@admin.local'; password = 'aDmin@123' } | ConvertTo-Json
    $login = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType "application/json" -Body $body
    return @{ Authorization = "Bearer $($login.token)" }
}

# --- Detail complet d'un dossier ---------------------------------------------
function Get-Dossier {
    param($headers, $id)
    return Invoke-RestMethod -Uri "$baseUrl/pre-enrollments/$id" -Headers $headers
}

# --- Liste des classes gerees par le seed (id, nom) --------------------------
function Get-ClassroomId {
    param($headers, $level)
    $classes = Invoke-RestMethod -Uri "$baseUrl/classes" -Headers $headers
    $cls = $classes | Where-Object { $_.nameClasse -eq $level } | Select-Object -First 1
    if (-not $cls) { throw "Classe '$level' introuvable dans GET /api/classes" }
    return $cls.id
}

# --- ETAPE 1 : creation du brouillon (candidat + annee + niveau + frais) -----
function New-PreEnrollment {
    param($headers, $firstName, $lastName, $birthDate, $gender, $level, $requiredFee)
    $body = @{
        firstName       = $firstName
        lastName        = $lastName
        birthDate       = $birthDate
        gender          = $gender
        birthPlace      = 'Dakar'
        academicYearId  = $academicYearId
        requestedLevel  = $level
        requiredFee     = $requiredFee
    } | ConvertTo-Json
    $created = Invoke-RestMethod -Uri "$baseUrl/pre-enrollments" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $body
    return Get-Dossier -headers $headers -id $created.id
}

# --- ETAPE 2 : ajout du responsable ------------------------------------------
function Add-Guardian {
    param($headers, $id, $relationshipType, $firstName, $lastName, $email, $phone)
    $body = @{
        relationshipType     = $relationshipType
        firstName            = $firstName
        lastName             = $lastName
        email                = $email
        phoneNumber          = $phone
        address              = 'Dakar, Senegal'
        primaryContact       = $true
        financialResponsible = $true
        emergencyContact     = $true
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/pre-enrollments/$id/guardians" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $body | Out-Null
    return Get-Dossier -headers $headers -id $id
}

# --- ETAPE 3 : depot des pieces obligatoires ----------------------------------
function Add-Document {
    param($headers, $id, $documentType)
    $body = @{
        documentType     = $documentType
        storageReference = "/mock/pre-enrollments/$id/$documentType.pdf"
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/pre-enrollments/$id/documents" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $body | Out-Null
    return Get-Dossier -headers $headers -id $id
}

# --- ETAPE 4 : frais (enregistrement + verification du versement) ------------
function Record-And-Verify-Fee {
    param($headers, $id, $amount, $transactionReference)
    $body = @{
        amount               = $amount
        paymentDate          = (Get-Date -Format 'yyyy-MM-dd')
        transactionReference = $transactionReference
        receiptNumber        = "REC-$($transactionReference.ToUpper())"
    } | ConvertTo-Json
    $payment = Invoke-RestMethod -Uri "$baseUrl/enrollment-finance/pre-enrollments/$id/fee-payments" `
        -Method Post -Headers $headers -ContentType "application/json" -Body $body
    $verified = Invoke-RestMethod -Uri "$baseUrl/enrollment-finance/pre-enrollment-fee-payments/$($payment.id)/verify" `
        -Method Post -Headers $headers
    $releve = @{ amount = $verified.amount; reference = $verified.transactionReference }
    $dossier = Get-Dossier -headers $headers -id $id
    return @{ dossier = $dossier; payment = $releve }
}

# --- ETAPE 5 : soumission, revue, decision ------------------------------------
function Submit-PreEnrollment {
    param($headers, $id)
    Invoke-RestMethod -Uri "$baseUrl/pre-enrollments/$id/submit" -Method Post -Headers $headers | Out-Null
    return Get-Dossier -headers $headers -id $id
}

function Start-Review {
    param($headers, $id)
    Invoke-RestMethod -Uri "$baseUrl/pre-enrollments/$id/start-review" -Method Post -Headers $headers | Out-Null
    return Get-Dossier -headers $headers -id $id
}

function Review-Document {
    param($headers, $id, $documentId, $status, $reason)
    $body = @{ status = $status; reason = $reason } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/pre-enrollments/$id/documents/$documentId/review" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $body | Out-Null
    return Get-Dossier -headers $headers -id $id
}

function Approve-PreEnrollment {
    param($headers, $id)
    Invoke-RestMethod -Uri "$baseUrl/pre-enrollments/$id/approve" -Method Post -Headers $headers | Out-Null
    return Get-Dossier -headers $headers -id $id
}

function Reject-PreEnrollment {
    param($headers, $id, $reason)
    $body = @{ reason = $reason } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/pre-enrollments/$id/reject" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $body | Out-Null
    return Get-Dossier -headers $headers -id $id
}

# --- Inscription (dossier approuve) ------------------------------------------
function New-Enrollment {
    param($headers, $preEnrollmentId, $classroomId)
    $body = @{ classroomId = $classroomId } | ConvertTo-Json
    return Invoke-RestMethod -Uri "$baseUrl/enrollments/from-pre-enrollment/$preEnrollmentId" -Method Post `
        -Headers $headers -ContentType "application/json" -Body $body
}

function Confirm-Enrollment {
    param($headers, $enrollmentId)
    return Invoke-RestMethod -Uri "$baseUrl/enrollments/$enrollmentId/confirm" -Method Post -Headers $headers
}

# --- Affichage ----------------------------------------------------------------
function Write-Etape {
    param($label, $dossier)
    Write-Host ""
    Write-Host "------------------------------------------------------------------------------"
    Write-Host (" {0}" -f $label)
    Write-Host ("   Dossier  : {0}  |  Statut : {1}" -f $dossier.number, $dossier.status)
    Write-Host ("   Candidat : {0} {1}  |  Niveau : {2}  |  Frais requis : {3}" -f `
        $dossier.applicantFirstName, $dossier.applicantLastName, $dossier.requestedLevel, $dossier.requiredFee)
    Write-Host ("   Responsables : {0}  |  Documents : {1}  |  Ref paiement : {2}" -f `
        $dossier.guardians.Count, $dossier.documents.Count, $dossier.feePaymentReference)
    if ($dossier.rejectionReason) {
        Write-Host ("   Motif de rejet : {0}" -f $dossier.rejectionReason)
    }
    foreach ($doc in $dossier.documents) {
        Write-Host ("       - {0} : {1}" -f $doc.documentType, $doc.reviewStatus)
    }
}

function Write-Enrollment {
    param($label, $enrollment)
    Write-Host "------------------------------------------------------------------------------"
    Write-Host (" {0}" -f $label)
    if ($null -ne $enrollment) {
        Write-Host ("   Matricule : {0}  |  Statut : {1}" -f $enrollment.number, $enrollment.status)
    }
}

# ==============================================================================
Write-Host "=============================================================="
Write-Host "  SIMULATION DES WORKFLOWS PRE-INSCRIPTION (mock data backend)"
Write-Host "=============================================================="

$headers = Get-ApiHeaders
Write-Host "Authentification : OK ($($headers.Authorization.Substring(7, 20))...)"
Write-Host "Annee academique active : $academicYearId (2026-2027)"

# Reference de transaction unique par execution (contrainte d'unicite en base)
$runId = Get-Date -Format 'yyyyMMddHHmmss'
Write-Host "Identifiant de run : $runId"
Write-Host ""

# ==============================================================================
# SCENARIO A : Amadou Kane (CP, Primaire) - parcours complet jusqu'a l'inscription
# ==============================================================================
Write-Host "##############################################################"
Write-Host "  SCENARIO A : Amadou Kane (CP) - PARCOURS COMPLET"
Write-Host "  DRAFT -> SUBMITTED -> UNDER_REVIEW -> APPROVED -> CONFIRMED"
Write-Host "##############################################################"

# 1. Candidat
$dossier = New-PreEnrollment -headers $headers -firstName 'Amadou' -lastName 'Kane' `
    -birthDate '2016-08-14' -gender 'MASCULIN' -level 'CP' -requiredFee 25000
Write-Etape "ETAPE 1 - Candidat : brouillon cree" $dossier
Write-Host "          -> etat initial DRAFT"

# 2. Responsables
$dossier = Add-Guardian -headers $headers -id $dossier.id -relationshipType 'MOTHER' `
    -firstName 'Astou' -lastName 'Kane' -email 'astou.kane@example.com' -phone '+221771234507'
Write-Etape "ETAPE 2 - Responsables : tuteur ajoute (Astou Kane, mere)" $dossier

# 3. Documents
$dossier = Add-Document -headers $headers -id $dossier.id -documentType 'BIRTH_CERTIFICATE'
$dossier = Add-Document -headers $headers -id $dossier.id -documentType 'REPORT_CARD'
Write-Etape "ETAPE 3 - Documents : acte de naissance + bulletin deposes" $dossier

# 4. Frais
$releve = Record-And-Verify-Fee -headers $headers -id $dossier.id -amount 25000 -transactionReference "sim-$runId-kna-001"
$dossier = $releve.dossier
Write-Etape ("ETAPE 4 - Frais : versement commande et verifie (montant = {0})" -f $releve.payment.amount) $dossier

# 5. Soumission (Recapitulatif -> Soumis)
$dossier = Submit-PreEnrollment -headers $headers -id $dossier.id
Write-Etape "ETAPE 5 - Recapitulatif : dossier SOUMIS" $dossier
Write-Host "          -> transition : DRAFT -> SUBMITTED"

# 6. Revue
$dossier = Start-Review -headers $headers -id $dossier.id
Write-Etape "ETAPE 6 - Debut de la revue (En cours d'etude)" $dossier
Write-Host "          -> transition : SUBMITTED -> UNDER_REVIEW"

$docIds = @($dossier.documents.id)
foreach ($docId in $docIds) {
    $dossier = Review-Document -headers $headers -id $dossier.id -documentId $docId -status 'APPROVED' -reason 'Document conforme'
}
Write-Etape "ETAPE 7 - Revue des documents : toutes les pieces approuvees" $dossier

# 7. Approbation
$dossier = Approve-PreEnrollment -headers $headers -id $dossier.id
Write-Etape "ETAPE 8 - Decision finale : dossier APPROUVE" $dossier
Write-Host "          -> transition : UNDER_REVIEW -> APPROVED"
Write-Host "          (validation : frais payes + documents obligatoires approuves)"

# 8. Inscription
$classroomId = Get-ClassroomId -headers $headers -level 'CP'
$enrollment = New-Enrollment -headers $headers -preEnrollmentId $dossier.id -classroomId $classroomId
Write-Enrollment "ETAPE 9 - Inscription creee depuis le dossier approuve" $enrollment

$enrollment = Confirm-Enrollment -headers $headers -enrollmentId $enrollment.id
Write-Enrollment "ETAPE 10 - Inscription CONFIRMEE" $enrollment
Write-Host "          -> transition : PENDING_CONFIRMATION -> CONFIRMED"

# ==============================================================================
# SCENARIO B : Salimata Sy (CE1, Primaire) - SOUMIS (attente revue)
# ==============================================================================
Write-Host ""
Write-Host "##############################################################"
Write-Host "  SCENARIO B : Salimata Sy (CE1) - DOSSIER SOUMIS"
Write-Host "  DRAFT -> SUBMITTED (simulation arretee a ce stade)"
Write-Host "##############################################################"

$dossierB = New-PreEnrollment -headers $headers -firstName 'Salimata' -lastName 'Sy' `
    -birthDate '2015-05-03' -gender 'FEMININ' -level 'CE1' -requiredFee 25000
Write-Etape "ETAPE 1 - Candidat : brouillon cree" $dossierB

$dossierB = Add-Guardian -headers $headers -id $dossierB.id -relationshipType 'FATHER' `
    -firstName 'Moussa' -lastName 'Sy' -email 'moussa.sy@example.com' -phone '+221771234508'
Write-Etape "ETAPE 2 - Responsables : tuteur ajoute (Moussa Sy, pere)" $dossierB

$dossierB = Add-Document -headers $headers -id $dossierB.id -documentType 'BIRTH_CERTIFICATE'
$dossierB = Add-Document -headers $headers -id $dossierB.id -documentType 'REPORT_CARD'
Write-Etape "ETAPE 3 - Documents : deposes" $dossierB

$releveB = Record-And-Verify-Fee -headers $headers -id $dossierB.id -amount 25000 -transactionReference "sim-$runId-syb-002"
$dossierB = $releveB.dossier
Write-Etape "ETAPE 4 - Frais : verifies" $dossierB

$dossierB = Submit-PreEnrollment -headers $headers -id $dossierB.id
Write-Etape "ETAPE 5 - Recapitulatif : dossier SOUMIS (simulation arretee)" $dossierB
Write-Host "          -> transition : DRAFT -> SUBMITTED"

# ==============================================================================
# SCENARIO C : Oumar Cisse (6eme, Secondaire) - EN COURS D'ETUDE
# ==============================================================================
Write-Host ""
Write-Host "##############################################################"
Write-Host "  SCENARIO C : Oumar Cisse (6eme) - EN COURS D'ETUDE"
Write-Host "  DRAFT -> SUBMITTED -> UNDER_REVIEW (simulation arretee)"
Write-Host "##############################################################"

$dossierC = New-PreEnrollment -headers $headers -firstName 'Oumar' -lastName 'Cisse' `
    -birthDate '2013-12-20' -gender 'MASCULIN' -level '6eme' -requiredFee 35000
Write-Etape "ETAPE 1 - Candidat : brouillon cree" $dossierC

$dossierC = Add-Guardian -headers $headers -id $dossierC.id -relationshipType 'MOTHER' `
    -firstName 'Bineta' -lastName 'Cisse' -email 'bineta.cisse@example.com' -phone '+221771234509'
Write-Etape "ETAPE 2 - Responsables : tuteur ajoute (Bineta Cisse)" $dossierC

$dossierC = Add-Document -headers $headers -id $dossierC.id -documentType 'BIRTH_CERTIFICATE'
$dossierC = Add-Document -headers $headers -id $dossierC.id -documentType 'REPORT_CARD'
Write-Etape "ETAPE 3 - Documents : deposes" $dossierC

$releveC = Record-And-Verify-Fee -headers $headers -id $dossierC.id -amount 35000 -transactionReference "sim-$runId-cis-003"
$dossierC = $releveC.dossier
Write-Etape "ETAPE 4 - Frais : verifies" $dossierC

$dossierC = Submit-PreEnrollment -headers $headers -id $dossierC.id
$dossierC = Start-Review -headers $headers -id $dossierC.id
Write-Etape "ETAPE 5 - Revue demarree : dossier EN COURS D'ETUDE (simulation arretee)" $dossierC
Write-Host "          -> transition : SUBMITTED -> UNDER_REVIEW"
Write-Host "          (documents encore en attente de verification par l'agent)"

# ==============================================================================
# SCENARIO D : Aissatou Mbaye (2nde, Secondaire) - DOSSIER REJETE
# ==============================================================================
Write-Host ""
Write-Host "##############################################################"
Write-Host "  SCENARIO D : Aissatou Mbaye (2nde) - DOSSIER REJETE"
Write-Host "  DRAFT -> SUBMITTED -> UNDER_REVIEW -> REJECTED"
Write-Host "##############################################################"

$dossierD = New-PreEnrollment -headers $headers -firstName 'Aissatou' -lastName 'Mbaye' `
    -birthDate '2008-04-17' -gender 'FEMININ' -level '2nde' -requiredFee 35000
Write-Etape "ETAPE 1 - Candidat : brouillon cree" $dossierD

$dossierD = Add-Guardian -headers $headers -id $dossierD.id -relationshipType 'FATHER' `
    -firstName 'El Hadj' -lastName 'Mbaye' -email 'elhaj.mbaye@example.com' -phone '+221771234510'
Write-Etape "ETAPE 2 - Responsables : tuteur ajoute (El Hadj Mbaye)" $dossierD

$dossierD = Add-Document -headers $headers -id $dossierD.id -documentType 'BIRTH_CERTIFICATE'
$dossierD = Add-Document -headers $headers -id $dossierD.id -documentType 'REPORT_CARD'
Write-Etape "ETAPE 3 - Documents : deposes" $dossierD

$releveD = Record-And-Verify-Fee -headers $headers -id $dossierD.id -amount 35000 -transactionReference "sim-$runId-mby-004"
$dossierD = $releveD.dossier
Write-Etape "ETAPE 4 - Frais : verifies" $dossierD

$dossierD = Submit-PreEnrollment -headers $headers -id $dossierD.id
$dossierD = Start-Review -headers $headers -id $dossierD.id
Write-Etape "ETAPE 5 - Revue demarree" $dossierD

$docIdsD = @($dossierD.documents.id)
$dossierD = Review-Document -headers $headers -id $dossierD.id -documentId $docIdsD[0] -status 'APPROVED' -reason 'Document conforme'
$dossierD = Review-Document -headers $headers -id $dossierD.id -documentId $docIdsD[1] -status 'REJECTED' -reason 'Bulletin illisible, veuillez en fournir un exemplaire clair'
Write-Etape "ETAPE 6 - Revue des documents : bulletin refuse" $dossierD

$motifRejet = 'Dossier incomplet : bulletin illisible. Nouvelle soumission possible apres correction.'
$dossierD = Reject-PreEnrollment -headers $headers -id $dossierD.id -reason $motifRejet
Write-Etape "ETAPE 7 - Decision : dossier REJETE" $dossierD
Write-Host "          -> transition : UNDER_REVIEW -> REJECTED"

# ==============================================================================
# BILAN : verification de la persistance en base (GET /api/pre-enrollments)
# ==============================================================================
Write-Host ""
Write-Host "##############################################################"
Write-Host "  BILAN - DOSSIERS EN BASE (etats du workflow simules)"
Write-Host "##############################################################"

$page = Invoke-RestMethod -Uri "$baseUrl/pre-enrollments?page=0&size=100" -Headers $headers
foreach ($p in $page.content) {
    if ($p.applicantFirstName) {
        Write-Host ("  {0} | {1,-12} | {2} {3} | {4}" -f `
            $p.number, $p.status, $p.applicantFirstName, $p.applicantLastName, $p.requestedLevel)
    }
}

$total = Invoke-RestMethod -Uri "$baseUrl/pre-enrollments?page=0&size=1" -Headers $headers
Write-Host ""
Write-Host "Total de dossiers de pre-inscription en base : $($total.totalElements)"
Write-Host ""
Write-Host "=============================================================="
Write-Host "  SIMULATION TERMINEE - workflow persiste en MySQL (bd_gsbp)"
Write-Host "=============================================================="