package com.school.platform.shared.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IGenericService<D, E, ID>{
  D create(D dto);
  Optional<D>findById(ID id);
  List<D> findAll();
  Page<D> findAll(Pageable pageable);
  D update(ID id, D dto);
  void delete(ID id);
  boolean existsById(ID id);

}