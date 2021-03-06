package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.UUID;

@Controller
public class FacilityTypeApprovedProductController extends BaseController {

  private static final Logger LOGGER =
        LoggerFactory.getLogger(FacilityTypeApprovedProductController.class);

  @Autowired
  private FacilityTypeApprovedProductRepository repository;

  /**
   * Allows creating new facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProduct A facilityTypeApprovedProduct bound to the request body
   * @return ResponseEntity containing the created facilityTypeApprovedProduct
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts", method = RequestMethod.POST)
  public ResponseEntity<?> createFacility(
        @RequestBody FacilityTypeApprovedProduct facilityTypeApprovedProduct) {
    LOGGER.debug("Creating new facilityTypeApprovedProduct");
    // Ignore provided id
    facilityTypeApprovedProduct.setId(null);
    repository.save(facilityTypeApprovedProduct);
    return new ResponseEntity<>(facilityTypeApprovedProduct, HttpStatus.CREATED);
  }

  /**
   * Get all newFacilityTypeApprovedProducts.
   *
   * @return FacilityTypeApprovedProduct.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts", method = RequestMethod.GET)
  public ResponseEntity<?> getAllFacilityTypeApprovedProducts() {
    Iterable<FacilityTypeApprovedProduct> facilityTypeApprovedProducts = repository.findAll();
    if (facilityTypeApprovedProducts == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(facilityTypeApprovedProducts, HttpStatus.OK);
    }
  }

  /**
   * Get list of full supply FacilityTypeApprovedProduct.
   *
   * @param facility facility id
   * @param program  program id
   * @return list of full supply FacilityTypeApprovedProduct
   */
  @RequestMapping(
      value = "/facilityTypeApprovedProducts/search",
      method = RequestMethod.GET
  )
  public ResponseEntity<?> getFullSupply(@RequestParam(value = "facility") UUID facility,
                                         @RequestParam(value = "program") UUID program) {
    Collection<FacilityTypeApprovedProduct> facilityTypeApprovedProducts =
        repository.searchFullSupply(facility, program);

    if (CollectionUtils.isEmpty(facilityTypeApprovedProducts)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(facilityTypeApprovedProducts, HttpStatus.OK);
    }
  }


  /**
   * Allows updating facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProduct A facilityTypeApprovedProduct bound to the request body
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct
   *                                      which we want to update
   * @return ResponseEntity containing the updated facilityTypeApprovedProduct
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateFacilityTypeApprovedProduct(
        @RequestBody FacilityTypeApprovedProduct facilityTypeApprovedProduct,
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    LOGGER.debug("Updating facilityTypeApprovedProduct");
    repository.save(facilityTypeApprovedProduct);
    return new ResponseEntity<>(facilityTypeApprovedProduct, HttpStatus.OK);
  }

  /**
   * Get chosen facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct which we want to get
   * @return FacilityTypeApprovedProduct.
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getFacilityTypeApprovedProduct(
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
          repository.findOne(facilityTypeApprovedProductId);
    if (facilityTypeApprovedProduct == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(facilityTypeApprovedProduct, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting facilityTypeApprovedProduct.
   *
   * @param facilityTypeApprovedProductId UUID of facilityTypeApprovedProduct
   *                                      which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilityTypeApprovedProducts/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteFacilityTypeApprovedProduct(
        @PathVariable("id") UUID facilityTypeApprovedProductId) {
    FacilityTypeApprovedProduct facilityTypeApprovedProduct =
          repository.findOne(facilityTypeApprovedProductId);
    if (facilityTypeApprovedProduct == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      repository.delete(facilityTypeApprovedProduct);
      return new ResponseEntity<FacilityTypeApprovedProduct>(HttpStatus.NO_CONTENT);
    }
  }
}
