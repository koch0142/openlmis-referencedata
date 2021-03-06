package org.openlmis.referencedata.repository.custom.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;

import java.util.List;

public class FacilityRepositoryImpl implements FacilityRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;


  /**
   * This method is supposed to retrieve all Facilities with facilityCode similar to
   * code parameter or facilityName similar to name parameter.
   * To find all wanted Facilities we use criteria query and like operator.
   *
   * @param code Part of wanted code.
   * @param name Part of wanted name.
   * @return List of Facilities with wanted code or name.
   */
  public List<Facility> findFacilitiesByCodeOrName(String code, String name) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Facility> query = builder.createQuery(Facility.class);
    Root<Facility> root = query.from(Facility.class);
    Predicate predicate = builder.conjunction();
    if (code != null) {
      Predicate codeOrNamePredicate = builder.or(
          builder.like(
              root.get("code"), "%" + code + "%"),
          builder.like(
              root.get("name"), "%" + name + "%"));
      predicate = builder.and(
          predicate, codeOrNamePredicate);
    }
    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }

}
