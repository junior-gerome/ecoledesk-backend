-- Baseline du schema genere depuis les entites JPA actuelles.
-- A appliquer uniquement sur une base MySQL neuve et vide.


    create table absences (
        date date not null,
        hours integer not null,
        justified bit not null,
        id bigint not null auto_increment,
        student_id bigint not null,
        updated_at datetime(6),
        status varchar(20) not null,
        justification_note varchar(500),
        primary key (id)
    ) engine=InnoDB;

    create table academic_grade (
        coefficient decimal(4,2) not null,
        grade decimal(4,2) not null,
        version integer,
        assessment_date datetime(6) not null,
        classe_id bigint not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        sequence_id bigint not null,
        student_id bigint not null,
        subject_id bigint not null,
        trimestre_id bigint,
        updated_at datetime(6),
        pedagogical_comment varchar(1000),
        period varchar(255),
        status enum ('DRAFT','LOCKED','VALIDATED') not null,
        primary key (id)
    ) engine=InnoDB;

    create table academic_year (
        active bit,
        date_debut date not null,
        date_fin date not null,
        statut_code bit not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        updated_at datetime(6),
        libelle_academic_year varchar(50) not null,
        primary key (id)
    ) engine=InnoDB;

    create table affectation (
        date_debut date,
        date_fin date,
        academic_year_id bigint not null,
        classe_id bigint not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        subject_id bigint not null,
        teacher_id bigint not null,
        updated_at datetime(6),
        type_code varchar(20) not null,
        primary key (id)
    ) engine=InnoDB;

    create table app_preferences (
        notify_absence bit not null,
        notify_email bit not null,
        notify_grades bit not null,
        notify_sms bit not null,
        page_size integer not null,
        password_min_length integer not null,
        require_two_factor bit not null,
        session_timeout_minutes integer not null,
        time_format varchar(5) not null,
        id bigint not null,
        currency varchar(10) not null,
        language varchar(10) not null,
        date_format varchar(20) not null,
        start_of_week varchar(20) not null,
        theme varchar(20) not null,
        school_phone varchar(40),
        school_code varchar(50),
        timezone varchar(80) not null,
        school_email varchar(150) not null,
        school_name varchar(160) not null,
        school_website varchar(200),
        school_address varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table authentication_audit_events (
        successful bit not null,
        created_at datetime(6) not null,
        id bigint not null auto_increment,
        user_id bigint,
        client_ip varchar(45),
        event_type varchar(50) not null,
        username varchar(100),
        reason varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table classes (
        actif bit,
        capacite integer,
        academic_year_id bigint,
        created_at datetime(6),
        id bigint not null auto_increment,
        section_id bigint,
        teacher_id bigint,
        updated_at datetime(6),
        niveau varchar(50) not null,
        name_classe varchar(100) not null,
        description varchar(500),
        primary key (id)
    ) engine=InnoDB;

    create table document (
        date_ajout datetime(6),
        id bigint not null auto_increment,
        student_id bigint,
        chemin_fichier varchar(255) not null,
        type_document enum ('AUTRE','BULLETIN','CERTIFICAT_NAISSANCE','CERTIFICAT_SCOLARITE','CNI_ENSEIGNANT','CNI_PARENT','PHOTO_ELEVE','PHOTO_ENSEIGNANT','PHOTO_PARENT','RECU') not null,
        primary key (id)
    ) engine=InnoDB;

    create table enrollments (
        active bit,
        confirmation_date date,
        enrollment_date date not null,
        withdrawal_date date,
        academic_year_id bigint not null,
        classroom_id bigint,
        created_at datetime(6),
        id bigint not null auto_increment,
        pre_enrollment_id bigint not null,
        student_id bigint,
        updated_at datetime(6),
        number varchar(50) not null,
        cancellation_reason varchar(500),
        status enum ('CANCELLED','COMPLETED','CONFIRMED','PENDING_CONFIRMATION','WITHDRAWN') not null,
        type enum ('NEW_ADMISSION','REENROLLMENT','TRANSFER') not null,
        primary key (id)
    ) engine=InnoDB;

    create table enseignant (
        date_embauche date,
        created_at datetime(6),
        id bigint not null auto_increment,
        updated_at datetime(6),
        phone_number varchar(20) not null,
        cni_number varchar(50),
        niveau varchar(50),
        email varchar(100) not null,
        firstname_teacher varchar(100) not null,
        lastname_teacher varchar(100) not null,
        speciality varchar(100),
        cni_photo_url varchar(500),
        photo_url varchar(500),
        adress varchar(255),
        gender ENUM('MASCULIN', 'FEMININ') not null,
        primary key (id)
    ) engine=InnoDB;

    create table log_activite (
        date_action datetime(6),
        id bigint not null auto_increment,
        reference_id bigint,
        user_id bigint not null,
        ip_adresse varchar(45),
        table_cible varchar(100),
        action varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table montant (
        count decimal(38,2) not null,
        classe_room_id bigint not null,
        id bigint not null auto_increment,
        type_paiement enum ('FRAIS_ACTIVITE','FRAIS_ASSURANCE','FRAIS_AUTRES','FRAIS_BIBLIOTHEQUE','FRAIS_CANTINE','FRAIS_EXAMEN','FRAIS_INSCRIPTION','FRAIS_MATERIEL','FRAIS_MATIERES','FRAIS_PHOTO','FRAIS_PREINSCRIPTION','FRAIS_SCOLAIRE','FRAIS_TRANSPORT','FRAIS_UNIFORME','SOUTIEN_ADMINISTRATIF','SOUTIEN_JURIDIQUE','SOUTIEN_MEDICAL','SOUTIEN_PSYCHOLOGIQUE','SOUTIEN_SOCIAL','SOUTIEN_TECHNIQUE') not null,
        primary key (id)
    ) engine=InnoDB;

    create table notifications (
        is_read bit,
        created_at datetime(6),
        id bigint not null auto_increment,
        user_id bigint,
        message varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table paiements (
        date_paiement date,
        due_date date,
        montant_paye decimal(38,2),
        montant_restant decimal(38,2),
        remise decimal(38,2),
        cancelled_at datetime(6),
        enrollment_id bigint not null,
        id bigint not null auto_increment,
        montant_id bigint not null,
        student_id bigint not null,
        version bigint,
        cancellation_reason varchar(500),
        description varchar(255),
        payment_method varchar(255),
        receipt_number varchar(255),
        type_paiement enum ('FRAIS_ACTIVITE','FRAIS_ASSURANCE','FRAIS_AUTRES','FRAIS_BIBLIOTHEQUE','FRAIS_CANTINE','FRAIS_EXAMEN','FRAIS_INSCRIPTION','FRAIS_MATERIEL','FRAIS_MATIERES','FRAIS_PHOTO','FRAIS_PREINSCRIPTION','FRAIS_SCOLAIRE','FRAIS_TRANSPORT','FRAIS_UNIFORME','SOUTIEN_ADMINISTRATIF','SOUTIEN_JURIDIQUE','SOUTIEN_MEDICAL','SOUTIEN_PSYCHOLOGIQUE','SOUTIEN_SOCIAL','SOUTIEN_TECHNIQUE') not null,
        primary key (id)
    ) engine=InnoDB;

    create table parents (
        active bit,
        created_at datetime(6),
        id bigint not null auto_increment,
        person_id bigint not null,
        updated_at datetime(6),
        occupation varchar(100),
        primary key (id)
    ) engine=InnoDB;

    create table payment_installments (
        active bit,
        due_date date not null,
        expected_amount decimal(12,2) not null,
        paid_amount decimal(12,2) not null,
        sequence_number integer not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        plan_id bigint not null,
        updated_at datetime(6),
        status enum ('CANCELLED','OVERDUE','PAID','PARTIALLY_PAID','PENDING') not null,
        primary key (id)
    ) engine=InnoDB;

    create table permissions (
        active bit,
        created_at datetime(6),
        id bigint not null auto_increment,
        updated_at datetime(6),
        action varchar(50),
        code varchar(50) not null,
        resource varchar(50),
        description varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table persons (
        active bit,
        birth_date date,
        created_at datetime(6),
        id bigint not null auto_increment,
        updated_at datetime(6),
        phone varchar(20),
        city varchar(100),
        country varchar(100),
        first_name varchar(100) not null,
        last_name varchar(100) not null,
        region varchar(100),
        email varchar(150),
        photo_url varchar(500),
        address varchar(255),
        address_complement varchar(255),
        gender enum ('FEMININ','MASCULIN'),
        primary key (id)
    ) engine=InnoDB;

    create table pre_enrollment_documents (
        active bit,
        created_at datetime(6),
        id bigint not null auto_increment,
        pre_enrollment_id bigint not null,
        reviewed_at datetime(6),
        reviewed_by bigint,
        submitted_at datetime(6),
        updated_at datetime(6),
        document_type varchar(50) not null,
        rejection_reason varchar(500),
        storage_reference varchar(500) not null,
        review_status enum ('APPROVED','REJECTED','REPLACEMENT_REQUIRED','REQUIRED','SUBMITTED') not null,
        primary key (id)
    ) engine=InnoDB;

    create table pre_enrollment_fee_payments (
        active bit,
        amount decimal(12,2) not null,
        refundable bit not null,
        verified bit not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        payment_date datetime(6) not null,
        pre_enrollment_id bigint not null,
        updated_at datetime(6),
        receipt_number varchar(100),
        transaction_reference varchar(100) not null,
        primary key (id)
    ) engine=InnoDB;

    create table pre_enrollment_guardians (
        active bit,
        emergency_contact bit not null,
        financial_responsible bit not null,
        primary_contact bit not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        pre_enrollment_id bigint not null,
        updated_at datetime(6),
        phone_number varchar(30),
        first_name varchar(100) not null,
        last_name varchar(100) not null,
        email varchar(150),
        address varchar(255),
        relationship_details varchar(255),
        relationship_type enum ('FATHER','GUARDIAN','MOTHER','OTHER','TUTOR') not null,
        primary key (id)
    ) engine=InnoDB;

    create table pre_enrollments (
        active bit,
        birth_date date,
        required_fee decimal(12,2),
        academic_year_id bigint not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        reviewed_at datetime(6),
        reviewed_by bigint,
        submitted_at datetime(6),
        updated_at datetime(6),
        number varchar(50) not null,
        fee_payment_reference varchar(100),
        requested_level varchar(100) not null,
        rejection_reason varchar(500),
        administrative_comment varchar(1000),
        birth_place varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        gender enum ('FEMININ','MASCULIN'),
        status enum ('APPROVED','CANCELLED','DRAFT','EXPIRED','REJECTED','SUBMITTED','UNDER_REVIEW') not null,
        primary key (id)
    ) engine=InnoDB;

    create table refresh_tokens (
        expires_at datetime(6) not null,
        id bigint not null auto_increment,
        issued_at datetime(6) not null,
        revoked_at datetime(6),
        user_id bigint not null,
        replaced_by_token_hash varchar(64),
        token_hash varchar(64) not null,
        primary key (id)
    ) engine=InnoDB;

    create table role_permissions (
        permission_id bigint not null,
        role_id bigint not null,
        primary key (permission_id, role_id)
    ) engine=InnoDB;

    create table roles (
        active bit,
        created_at datetime(6),
        id bigint not null auto_increment,
        updated_at datetime(6),
        code varchar(50) not null,
        label varchar(100) not null,
        description varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table search_favorites (
        created_at datetime(6) not null,
        id varchar(36) not null,
        name varchar(120) not null,
        criteria_json TEXT not null,
        primary key (id)
    ) engine=InnoDB;

    create table sections (
        id bigint not null auto_increment,
        description varchar(255),
        libelle varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table sequence (
        created_at datetime(6),
        id bigint not null auto_increment,
        trimestre_id bigint,
        updated_at datetime(6),
        libelle_sequence varchar(50) not null,
        primary key (id)
    ) engine=InnoDB;

    create table staff_assignments (
        active bit,
        end_date date,
        start_date date not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        staff_member_id bigint not null,
        updated_at datetime(6),
        professional_function enum ('ACCOUNTANT','CLEANER','DIRECTOR','SECRETARY','SECURITY_GUARD','TEACHER') not null,
        primary key (id)
    ) engine=InnoDB;

    create table staff_members (
        active bit,
        employment_date date not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        person_id bigint not null,
        updated_at datetime(6),
        employee_number varchar(50) not null,
        primary key (id)
    ) engine=InnoDB;

    create table statut_year (
        code varchar(20) not null,
        libelle varchar(100) not null,
        primary key (code)
    ) engine=InnoDB;

    create table student_guardians (
        active bit,
        created_at datetime(6),
        guardian_person_id bigint not null,
        id bigint not null auto_increment,
        student_id bigint not null,
        updated_at datetime(6),
        relationship_type enum ('FATHER','GUARDIAN','MOTHER','OTHER','TUTOR') not null,
        primary key (id)
    ) engine=InnoDB;

    create table student_parents (
        active bit,
        created_at datetime(6),
        id bigint not null auto_increment,
        parent_id bigint not null,
        student_id bigint not null,
        updated_at datetime(6),
        relationship_type enum ('FATHER','GUARDIAN','MOTHER','OTHER','TUTOR') not null,
        primary key (id)
    ) engine=InnoDB;

    create table student_progress_report (
        attendance_rate float(53),
        average_grade float(53),
        created_at datetime(6),
        id bigint not null auto_increment,
        student_id bigint not null,
        updated_at datetime(6),
        period varchar(20) not null,
        comments varchar(1000),
        primary key (id)
    ) engine=InnoDB;

    create table students (
        active bit,
        admission_date date not null,
        created_at datetime(6),
        id bigint not null auto_increment,
        person_id bigint not null,
        updated_at datetime(6),
        current_level varchar(50),
        student_number varchar(50) not null,
        ecole_precedente varchar(100),
        primary key (id)
    ) engine=InnoDB;

    create table subject (
        actif bit,
        coefficient integer,
        created_at datetime(6),
        id bigint not null auto_increment,
        updated_at datetime(6),
        code varchar(20) not null,
        name_subject varchar(100) not null,
        description varchar(500),
        primary key (id)
    ) engine=InnoDB;

    create table support_ticket (
        created_at datetime(6) not null,
        id bigint not null auto_increment,
        name varchar(120) not null,
        email varchar(150) not null,
        subject varchar(160) not null,
        message varchar(2000) not null,
        priority enum ('HIGH','LOW','MEDIUM') not null,
        status enum ('CLOSED','IN_PROGRESS','OPEN') not null,
        primary key (id)
    ) engine=InnoDB;

    create table trimestre (
        academic_year_id bigint,
        created_at datetime(6),
        id bigint not null auto_increment,
        updated_at datetime(6),
        libelle_trimestre varchar(50) not null,
        primary key (id)
    ) engine=InnoDB;

    create table tuition_payment_plans (
        active bit,
        discount_amount decimal(12,2) not null,
        net_amount decimal(12,2) not null,
        total_amount decimal(12,2) not null,
        created_at datetime(6),
        enrollment_id bigint not null,
        id bigint not null auto_increment,
        updated_at datetime(6),
        status enum ('ACTIVE','CANCELLED','COMPLETED','DRAFT') not null,
        primary key (id)
    ) engine=InnoDB;

    create table type_affectation (
        code varchar(20) not null,
        libelle varchar(100) not null,
        primary key (code)
    ) engine=InnoDB;

    create table user_accounts (
        active bit,
        email_verified bit,
        enabled bit,
        created_at datetime(6),
        id bigint not null auto_increment,
        last_login datetime(6),
        person_id bigint not null,
        reset_token_expires_at datetime(6),
        updated_at datetime(6),
        username varchar(50) not null,
        reset_token varchar(64),
        password varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table user_roles (
        role_id bigint not null,
        user_id bigint not null,
        primary key (role_id, user_id)
    ) engine=InnoDB;

    create index idx_auth_audit_username 
       on authentication_audit_events (username);

    create index idx_auth_audit_user_id 
       on authentication_audit_events (user_id);

    create index idx_auth_audit_event_type 
       on authentication_audit_events (event_type);

    create index idx_auth_audit_created_at 
       on authentication_audit_events (created_at);

    alter table enrollments 
       add constraint uk_enrollments_pre_enrollment unique (pre_enrollment_id);

    alter table enrollments 
       add constraint UKmykvnxiq3f22wk4d13nwqi8hv unique (number);

    alter table enseignant 
       add constraint UKi5emqyn1m68wtc19jle960n5a unique (phone_number);

    alter table enseignant 
       add constraint UK6kxdv8s2oqch2dcl5euax55hj unique (email);

    alter table parents 
       add constraint UKt38nk3trrb5rndg921p0n3d16 unique (person_id);

    alter table payment_installments 
       add constraint uk_payment_installment_sequence unique (plan_id, sequence_number);

    alter table permissions 
       add constraint UK7lcb6glmvwlro3p2w2cewxtvd unique (code);

    alter table persons 
       add constraint UK1x5aosta48fbss4d5b3kuu0rd unique (email);

    alter table pre_enrollment_fee_payments 
       add constraint UKsnjyfyo9fkawxeml1jdbp0x0 unique (transaction_reference);

    alter table pre_enrollments 
       add constraint UKpy34i3v62y93ei2t118qefxor unique (number);

    alter table refresh_tokens 
       add constraint UKo2mlirhldriil2y7krapq4frt unique (token_hash);

    alter table roles 
       add constraint UKch1113horj4qr56f91omojv8 unique (code);

    alter table staff_members 
       add constraint UKkuaak93a34fpjxpkt14c2vl6i unique (person_id);

    alter table staff_members 
       add constraint UK28n8apnj73x9s0m5yqbnqp8g9 unique (employee_number);

    alter table student_guardians 
       add constraint uk_student_guardians_student_person unique (student_id, guardian_person_id);

    alter table students 
       add constraint UKeaxjc9jjy2ylssnue0eqcxrjp unique (person_id);

    alter table students 
       add constraint UKh7gboo6v79gig1eo7lt1fubew unique (student_number);

    alter table subject 
       add constraint UKdb5g1rfeug7aywnpb6gab85ep unique (code);

    alter table tuition_payment_plans 
       add constraint UKcav7dufhe410x2vk7m6k8ngl unique (enrollment_id);

    alter table user_accounts 
       add constraint UK4nq5c82rn9wf9m8yklxmm6mkd unique (person_id);

    alter table user_accounts 
       add constraint UKlxwlgwuy2yrbye2vgs9w9x7mr unique (username);

    alter table absences 
       add constraint FKrpq757dcd0kr3im7c2bjc5hkw 
       foreign key (student_id) 
       references students (id);

    alter table academic_grade 
       add constraint FK84d37oag2pkrl0hjpsc69mlfc 
       foreign key (classe_id) 
       references classes (id);

    alter table academic_grade 
       add constraint FKonvcjleggwbqqk627v1xcpdyl 
       foreign key (sequence_id) 
       references sequence (id);

    alter table academic_grade 
       add constraint FKos2xeig5ok3uwag6l2rebgp7y 
       foreign key (student_id) 
       references students (id);

    alter table academic_grade 
       add constraint FKfeywxa82hj98nn0hbd7sxrgu6 
       foreign key (subject_id) 
       references subject (id);

    alter table academic_grade 
       add constraint FK8i2lj2gxaihcf0nd3tmgoqtwr 
       foreign key (trimestre_id) 
       references trimestre (id);

    alter table affectation 
       add constraint FKrrrcesn8170mlm21wg855y3qg 
       foreign key (academic_year_id) 
       references academic_year (id);

    alter table affectation 
       add constraint FK2m089s93k4ko5lg3df2go77dd 
       foreign key (classe_id) 
       references classes (id);

    alter table affectation 
       add constraint FKnihj0h6n8xmdvarf5hbvq0jc9 
       foreign key (subject_id) 
       references subject (id);

    alter table affectation 
       add constraint FKjdgtflhhg6iitmvlql8xxyayb 
       foreign key (teacher_id) 
       references enseignant (id);

    alter table classes 
       add constraint FKspuxc9nwvpsede4y4j5ije2ad 
       foreign key (academic_year_id) 
       references academic_year (id);

    alter table classes 
       add constraint FK6ug67axcp5rn2y19gwrslro4k 
       foreign key (section_id) 
       references sections (id);

    alter table classes 
       add constraint FKqt3ksgqd0ag0cdikx7fhe22yh 
       foreign key (teacher_id) 
       references enseignant (id);

    alter table document 
       add constraint FKcsj33iyd6mdv741ym8e1rfu3q 
       foreign key (student_id) 
       references students (id);

    alter table enrollments 
       add constraint FKghrlxkh5u3baldjqb30jtw56m 
       foreign key (academic_year_id) 
       references academic_year (id);

    alter table enrollments 
       add constraint FKbyqlrgxp59y0j279i9e93230p 
       foreign key (classroom_id) 
       references classes (id);

    alter table enrollments 
       add constraint FKjwnuwdj1g5u58foo9oyisnu82 
       foreign key (pre_enrollment_id) 
       references pre_enrollments (id);

    alter table enrollments 
       add constraint FK8kf1u1857xgo56xbfmnif2c51 
       foreign key (student_id) 
       references students (id);

    alter table log_activite 
       add constraint FK2kcymumbh5xl7y9bou7eyqbc4 
       foreign key (user_id) 
       references user_accounts (id);

    alter table montant 
       add constraint FK4uvjcy8tg77lpxolpw7qiub2e 
       foreign key (classe_room_id) 
       references classes (id);

    alter table paiements 
       add constraint FKcyoeh0yi4m2vmv0db850wm4wb 
       foreign key (enrollment_id) 
       references enrollments (id);

    alter table paiements 
       add constraint FK33u9np98xyqwpqfdjhi6odwu 
       foreign key (montant_id) 
       references montant (id);

    alter table paiements 
       add constraint FKq91r8kn2oy5vnookavk4os7og 
       foreign key (student_id) 
       references students (id);

    alter table parents 
       add constraint FKnei1hbyyiplts3rcp60ce9plt 
       foreign key (person_id) 
       references persons (id);

    alter table payment_installments 
       add constraint FK7mnsw51428mbrfkoowdn80xil 
       foreign key (plan_id) 
       references tuition_payment_plans (id);

    alter table pre_enrollment_documents 
       add constraint FKsft3agup2vc26lxe5k4xisngq 
       foreign key (pre_enrollment_id) 
       references pre_enrollments (id);

    alter table pre_enrollment_fee_payments 
       add constraint FK9vjpqf182y4lo3hn3asl8mb76 
       foreign key (pre_enrollment_id) 
       references pre_enrollments (id);

    alter table pre_enrollment_guardians 
       add constraint FKplrq5u7p7m50v00gu0fcd81gf 
       foreign key (pre_enrollment_id) 
       references pre_enrollments (id);

    alter table pre_enrollments 
       add constraint FKn3169138dcpej6hdr2xlbtp8m 
       foreign key (academic_year_id) 
       references academic_year (id);

    alter table refresh_tokens 
       add constraint FK4ma0jg35fmwwfv2wrr2npjvu8 
       foreign key (user_id) 
       references user_accounts (id);

    alter table role_permissions 
       add constraint FKegdk29eiy7mdtefy5c7eirr6e 
       foreign key (permission_id) 
       references permissions (id);

    alter table role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles (id);

    alter table sequence 
       add constraint FKpeoge95mbusyfqe6eyxf6ofk5 
       foreign key (trimestre_id) 
       references trimestre (id);

    alter table staff_assignments 
       add constraint FKrr6ul0uy3s98gx81l8j5sfhae 
       foreign key (staff_member_id) 
       references staff_members (id);

    alter table staff_members 
       add constraint FK4f2bu8c705vujut7kbsg7u4am 
       foreign key (person_id) 
       references persons (id);

    alter table student_guardians 
       add constraint FK2t9tc6xy5covs6wqevyt842r3 
       foreign key (guardian_person_id) 
       references persons (id);

    alter table student_guardians 
       add constraint FKf3bj5ksuok1k0lbenj0wch3uf 
       foreign key (student_id) 
       references students (id);

    alter table student_parents 
       add constraint FKh8yi0uy8nbnfqhtsnw3kv158v 
       foreign key (parent_id) 
       references parents (id);

    alter table student_parents 
       add constraint FKeamoo6ghyb022owmr29yqha3v 
       foreign key (student_id) 
       references students (id);

    alter table student_progress_report 
       add constraint FKiw9uwq8s29r9dha03rx181d46 
       foreign key (student_id) 
       references students (id);

    alter table students 
       add constraint FK7bj6li9e77inyp4eu1d2l0mhl 
       foreign key (person_id) 
       references persons (id);

    alter table trimestre 
       add constraint FK703i20lo5le5rxy0pvhlncgth 
       foreign key (academic_year_id) 
       references academic_year (id);

    alter table tuition_payment_plans 
       add constraint FK1huspesvh6hk85loonjojuojq 
       foreign key (enrollment_id) 
       references enrollments (id);

    alter table user_accounts 
       add constraint FKgk3yo8u7qaj123dbtrjfthxrl 
       foreign key (person_id) 
       references persons (id);

    alter table user_roles 
       add constraint FKh8ciramu9cc9q3qcqiv4ue8a6 
       foreign key (role_id) 
       references roles (id);

    alter table user_roles 
       add constraint FKnb9ceyh529oqh9n3aiw68twme 
       foreign key (user_id) 
       references user_accounts (id);
