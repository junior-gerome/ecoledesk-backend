package com.school.platform.settings.domain.model;

import com.school.platform.settings.application.dto.AppPreferencesDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_preferences")
public class AppPreferences {
    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(nullable = false, length = 160)
    private String schoolName;

    @Column(length = 50)
    private String schoolCode;

    @Column(nullable = false, length = 150)
    private String schoolEmail;

    @Column(length = 40)
    private String schoolPhone;

    @Column(length = 200)
    private String schoolWebsite;

    @Column(length = 255)
    private String schoolAddress;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(nullable = false, length = 80)
    private String timezone;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 20)
    private String dateFormat;

    @Column(nullable = false, length = 5)
    private String timeFormat;

    @Column(nullable = false, length = 20)
    private String theme;

    @Column(nullable = false, length = 20)
    private String startOfWeek;

    @Column(nullable = false)
    private Integer pageSize;

    @Column(nullable = false)
    private boolean notifyEmail;

    @Column(nullable = false)
    private boolean notifySms;

    @Column(nullable = false)
    private boolean notifyAbsence;

    @Column(nullable = false)
    private boolean notifyGrades;

    @Column(nullable = false)
    private boolean requireTwoFactor;

    @Column(nullable = false)
    private Integer sessionTimeoutMinutes;

    @Column(nullable = false)
    private Integer passwordMinLength;

    public static AppPreferences from(AppPreferencesDTO dto) {
        AppPreferences entity = new AppPreferences();
        entity.apply(dto);
        return entity;
    }

    public void apply(AppPreferencesDTO dto) {
        this.schoolName = dto.schoolName();
        this.schoolCode = dto.schoolCode();
        this.schoolEmail = dto.schoolEmail();
        this.schoolPhone = dto.schoolPhone();
        this.schoolWebsite = dto.schoolWebsite();
        this.schoolAddress = dto.schoolAddress();
        this.language = dto.language();
        this.timezone = dto.timezone();
        this.currency = dto.currency();
        this.dateFormat = dto.dateFormat();
        this.timeFormat = dto.timeFormat();
        this.theme = dto.theme();
        this.startOfWeek = dto.startOfWeek();
        this.pageSize = dto.pageSize();
        this.notifyEmail = dto.notifyEmail();
        this.notifySms = dto.notifySms();
        this.notifyAbsence = dto.notifyAbsence();
        this.notifyGrades = dto.notifyGrades();
        this.requireTwoFactor = dto.requireTwoFactor();
        this.sessionTimeoutMinutes = dto.sessionTimeoutMinutes();
        this.passwordMinLength = dto.passwordMinLength();
    }

    public AppPreferencesDTO toDto() {
        return new AppPreferencesDTO(
                schoolName,
                schoolCode,
                schoolEmail,
                schoolPhone,
                schoolWebsite,
                schoolAddress,
                language,
                timezone,
                currency,
                dateFormat,
                timeFormat,
                theme,
                startOfWeek,
                pageSize,
                notifyEmail,
                notifySms,
                notifyAbsence,
                notifyGrades,
                requireTwoFactor,
                sessionTimeoutMinutes,
                passwordMinLength
        );
    }
}
