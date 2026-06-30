package com.school.platform.shared.application.service;

import java.util.List;

import com.school.platform.identityaccess.application.dto.UsersDTO;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;

public interface UserService {
  UsersDTO save(RegisterRequest dto);
  UsersDTO findById(Long id);
  List<UsersDTO> findAll();
  UsersDTO update(Long id, RegisterRequest dto);
  void delete(Long id);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}