package com.school.gestionuser.domain.service;

import com.school.gestionuser.domain.model.identity.User;

public class SecurityDomainService {
    public boolean isEnabledAndUnlocked(User user) {
        return user != null && user.isEnabled() && user.isAccountNonLocked();
    }
}