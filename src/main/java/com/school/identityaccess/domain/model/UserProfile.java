package com.school.identityaccess.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles", uniqueConstraints = @UniqueConstraint(name = "uk_user_profiles_user_profile", columnNames = {"user_account_id", "profile_id"}))
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    protected UserProfile() {
    }

    UserProfile(UserAccount userAccount, Profile profile) {
        this.userAccount = userAccount;
        this.profile = profile;
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public Profile getProfile() {
        return profile;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
