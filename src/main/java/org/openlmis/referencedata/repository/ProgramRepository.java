package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Program;

import java.util.UUID;

public interface ProgramRepository extends ReferenceDataRepository<Program, UUID> {
  // Add custom Program related members here. See UserRepository.java for examples.

  @Override
  <S extends Program> S save(S entity);

  @Override
  <S extends Program> Iterable<S> save(Iterable<S> entities);

  <S extends Program> S findByCode(String code);
}
