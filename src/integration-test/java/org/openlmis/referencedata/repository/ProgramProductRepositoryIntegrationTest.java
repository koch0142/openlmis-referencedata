package org.openlmis.referencedata.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.GlobalProduct;
import org.openlmis.referencedata.domain.ProductCategory;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramProduct;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ProgramProductRepositoryIntegrationTest
        extends BaseCrudRepositoryIntegrationTest<ProgramProduct> {

  @Autowired
  private ProgramProductRepository programProductRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProductCategoryRepository productCategoryRepository;

  @Autowired
  private OrderableProductRepository orderableProductRepository;

  private List<ProgramProduct> programProducts;

  ProgramProductRepository getRepository() {
    return this.programProductRepository;
  }

  ProgramProduct generateInstance() {
    Program program = generateProgram();
    GlobalProduct globalProduct = orderableProductRepository.save(new GlobalProduct());
    ProductCategory productCategory = ProductCategory.createNew(Code.code("testcat"));
    productCategoryRepository.save(productCategory);
    ProgramProduct programProduct = ProgramProduct.createNew(program,
        productCategory,
        globalProduct);
    return programProduct;
  }

  @Before
  public void setUp() {
    programProducts = new ArrayList<>();
    for (int programProductNumber = 0; programProductNumber < 5; programProductNumber++) {
      programProducts.add(programProductRepository.save(generateInstance()));
    }
  }

  @Test
  public void searchProgramProductsByAllParameters() {
    ProgramProduct programProduct = cloneProgramProduct(programProducts.get(0));
    List<ProgramProduct> receivedProgramProducts =
            programProductRepository.searchProgramProducts(
                    programProduct.getProgram());

    Assert.assertEquals(2, receivedProgramProducts.size());
    for (ProgramProduct receivedProgramProduct : receivedProgramProducts) {
      Assert.assertEquals(
              programProduct.getProgram().getId(),
              receivedProgramProduct.getProgram().getId());
    }
  }

  @Test
  public void searchProgramProductsByProgram() {
    ProgramProduct programProduct = cloneProgramProduct(programProducts.get(0));
    List<ProgramProduct> receivedProgramProducts =
            programProductRepository.searchProgramProducts(
                    programProduct.getProgram());

    Assert.assertEquals(2, receivedProgramProducts.size());
    for (ProgramProduct receivedProgramProduct : receivedProgramProducts) {
      Assert.assertEquals(
              programProduct.getProgram().getId(),
              receivedProgramProduct.getProgram().getId());
    }
  }

  @Test
  public void searchProgramProductsByAllParametersNull() {
    List<ProgramProduct> receivedProgramProducts =
            programProductRepository.searchProgramProducts(null);

    Assert.assertEquals(programProducts.size(), receivedProgramProducts.size());
  }

  private ProgramProduct cloneProgramProduct(ProgramProduct programProduct) {
    ProgramProduct clonedProgramProduct = ProgramProduct.createNew(programProduct.getProgram(),
        programProduct.getProductCategory(),
        programProduct.getProduct());
    programProductRepository.save(clonedProgramProduct);
    return clonedProgramProduct;
  }

  private Program generateProgram() {
    Program program = new Program("code" + this.getNextInstanceNumber());
    program.setPeriodsSkippable(false);
    programRepository.save(program);
    return program;
  }
}
