package com.school.gestionuser.domain.port.in.identity;

import com.school.gestionuser.domain.model.identity.User;

public interface CreateUserUseCase {
    User createUser(String username, String passwordHash, String personId);
}