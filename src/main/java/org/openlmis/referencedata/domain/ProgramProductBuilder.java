package org.openlmis.referencedata.domain;

import org.openlmis.referencedata.repository.ProgramRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Builder of {@link ProgramProduct}'s intended for use in deserialization.  This is a standard
 * builder pattern, however it requires that {@link #setProgramRepository(ProgramRepository)} is
 * called with a {@link ProgramRepository} so that it may lookup a Program's UUID and convert
 * it to {@link Program} in order to build a {@link ProgramProduct}.
 */
public class ProgramProductBuilder {
  private ProgramRepository programRepository;

  private UUID programId;
  private Integer dosesPerMonth;
  private boolean active;
  private String productCategory;
  private boolean fullSupply;
  private int displayOrder;
  private int maxMonthsOfStock;

  private ProgramProductBuilder() {
    this.dosesPerMonth = null;
    this.active = true;
    this.productCategory = "Other";
    this.fullSupply = false;
    this.displayOrder = 0;
    this.maxMonthsOfStock = 1;
  }

  /**
   * Creates a new builder with the given program id.
   * @param programId a persistent program id that the
   * {@link #setProgramRepository(ProgramRepository)} will find.
   */
  public ProgramProductBuilder(UUID programId) {
    this();
    this.programId = Objects.requireNonNull(programId);
  }

  public ProgramProductBuilder setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  public ProgramProductBuilder setDosesPerMonth(Integer dosesPerMonth) {
    this.dosesPerMonth = dosesPerMonth;
    return this;
  }

  public ProgramProductBuilder setActive(boolean active) {
    this.active = active;
    return this;
  }

  public ProgramProductBuilder setProductCategory(String productCategory) {
    this.productCategory = productCategory;
    return this;
  }

  public ProgramProductBuilder setFullSupply(boolean fullSupply) {
    this.fullSupply = fullSupply;
    return this;
  }

  public ProgramProductBuilder setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
    return this;
  }

  public ProgramProductBuilder setMaxMonthsOfStock(int maxMonthsOfStock) {
    this.maxMonthsOfStock = maxMonthsOfStock;
    return this;
  }

  public final void setProgramRepository(ProgramRepository repository) {
    this.programRepository = repository;
  }


  /**
   * Builds a new (non-persisted) {@link ProgramProduct}.  This will build a program product that
   * is ready for being persisted (or updating a pre-persisted entity), using the UUID's given in
   * this builder by resolving them using the provided repository.
   * @param product the product for which we're building this ProgramProduct.
   * @return a new ProgramProduct ready for persisting.
   * @throws NullPointerException if {@link #setProgramRepository(ProgramRepository)} wasn't
   *      called previously with a non-null repository.
   */
  public ProgramProduct createProgramProduct(OrderableProduct product) {
    Objects.requireNonNull(programRepository, "Program Repository needed to be injected prior to "
        + "creating program product");
    Objects.requireNonNull(product, "Product can't be null when building a program product");

    Objects.requireNonNull(productCategory);

    Program storedProgram = programRepository.findOne(programId);
    return ProgramProduct.createNew(storedProgram,
      productCategory,
      product,
      dosesPerMonth,
      active,
      fullSupply,
      displayOrder,
      maxMonthsOfStock);
  }
}