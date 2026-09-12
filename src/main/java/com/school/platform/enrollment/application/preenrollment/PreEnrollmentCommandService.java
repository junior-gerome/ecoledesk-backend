package com.school.platform.enrollment.application.preenrollment;

import com.school.platform.enrollment.application.dto.preenrollment.*;
// import com.school.platform.enrollment.application.dto.preenrollment.AddPreEnrollmentGuardianRequest;
// import com.school.platform.enrollment.application.dto.preenrollment.CreatePreEnrollmentRequest;
// import com.school.platform.enrollment.application.dto.preenrollment.DecisionRequest;

import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.DocumentReviewStatus;




public interface PreEnrollmentCommandService {

  //Créer une préinscription
  public PreEnrollmentResponse createDraft(CreatePreEnrollmentRequest request);
   
  //Ajouter un responsable
  public PreEnrollmentResponse addGuardian(Long id, AddPreEnrollmentGuardianRequest request);

  //Ajouter un document 
  PreEnrollmentResponse addDocument(Long preEnrollmentId, AddPreEnrollmentDocumentRequest request);

  //Recuperer le contenu d'un document du dossier pour telechargement
  PreEnrollmentDocumentContent downloadDocument(Long preEnrollmentId, Long documentId);

  //permet à un agent administratif de vérifier un document et de lui attribuer un statut
  PreEnrollmentResponse reviewDocument(Long preEnrollmentId, Long documentId,  DocumentReviewStatus status, Long reviewedBy,  String reason);
   
  //Soumettre le dossier
  public PreEnrollmentResponse submit(Long id);
   
  //Methode indique que l’administration commence l’étude du dossier.
  public PreEnrollmentResponse startReview(Long id, Long reviewedBy);
   
  //Approuver
  public PreEnrollmentResponse approve(Long id, Long reviewedBy);
    
  //Rejeter
  public PreEnrollmentResponse reject(Long id, Long reviewedBy, String reason);

  //Commencer l’étude 
 // public PreEnrollmentResponse response(PreEnrollment preEnrollment);


}
