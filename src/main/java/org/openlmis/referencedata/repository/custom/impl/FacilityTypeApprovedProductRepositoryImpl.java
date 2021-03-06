package org.openlmis.referencedata.repository.custom.impl;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.openlmis.referencedata.repository.custom.FacilityTypeApprovedProductRepositoryCustom;

import java.util.Collection;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.google.common.base.Preconditions.checkNotNull;

public class FacilityTypeApprovedProductRepositoryImpl
    implements FacilityTypeApprovedProductRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Collection<FacilityTypeApprovedProduct> searchFullSupply(UUID facilityId, UUID programId) {
    checkNotNull(facilityId);
    checkNotNull(programId);

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<FacilityTypeApprovedProduct> query = builder.createQuery(
        FacilityTypeApprovedProduct.class
    );

    Root<FacilityTypeApprovedProduct> ftap = query.from(FacilityTypeApprovedProduct.class);
    Root<Facility> facility = query.from(Facility.class);

    Join<Facility, FacilityType> fft = facility.join("type");

    Join<FacilityTypeApprovedProduct, FacilityType> ft = ftap.join("facilityType");
    Join<FacilityTypeApprovedProduct, ProgramProduct> pp = ftap.join("programProduct");

    Join<ProgramProduct, Program> program = pp.join("program");

    Predicate conjunction = builder.conjunction();
    conjunction = builder.and(conjunction, builder.equal(fft.get("id"), ft.get("id")));
    conjunction = builder.and(conjunction, builder.equal(program.get("id"), programId));
    conjunction = builder.and(conjunction, builder.equal(facility.get("id"), facilityId));
    conjunction = builder.and(conjunction, builder.isTrue(pp.get("fullSupply")));
    conjunction = builder.and(conjunction, builder.isTrue(pp.get("active")));

    query.select(ftap);
    query.where(conjunction);

    Join<ProgramProduct, ProductCategory> category = pp.join("productCategory");
    Join<ProgramProduct, OrderableProduct> product = pp.join("product");

    query.orderBy(
        builder.asc(category.get("orderedDisplayValue").get("displayOrder")),
        builder.asc(category.get("orderedDisplayValue").get("displayName")),
        builder.asc(product.get("productCode"))
    );

    return entityManager.createQuery(query).getResultList();
  }

}
