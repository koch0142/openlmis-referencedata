package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.dto.RequisitionGroupProgramScheduleDto;
import org.openlmis.referencedata.exception.InvalidIdException;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.openlmis.referencedata.service.RequisitionGroupProgramScheduleService;
import org.openlmis.referencedata.util.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class RequisitionGroupProgramScheduleController extends BaseController {

  private static final Logger LOGGER =
        LoggerFactory.getLogger(RequisitionGroupProgramScheduleController.class);

  @Autowired
  private RequisitionGroupProgramScheduleService service;

  @Autowired
  private RequisitionGroupProgramScheduleRepository repository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Allows creating new requisitionGroupProgramSchedule.
   * If the id is specified, it will be ignored.
   *
   * @param requisition A requisitionGroupProgramSchedule bound to the request body
   * @return ResponseEntity containing the created requisitionGroupProgramSchedule
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules", method = RequestMethod.POST)
  public ResponseEntity<?> createRequisitionGroupProgramSchedule(
        @RequestBody RequisitionGroupProgramSchedule requisition) {
    LOGGER.debug("Creating new requisitionGroupProgramSchedule");
    requisition.setId(null);
    repository.save(requisition);
    LOGGER.debug("Created new requisitionGroupProgramSchedule with id: " + requisition.getId());
    return new ResponseEntity<>(requisition, HttpStatus.CREATED);
  }

  /**
   * Get all requisitionGroupProgramSchedules.
   *
   * @return RequisitionGroupProgramSchedules.
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules", method = RequestMethod.GET)
  public ResponseEntity<?> getAllRequisitionGroupProgramSchedule() {
    Iterable<RequisitionGroupProgramSchedule> requisitions = repository.findAll();
    return new ResponseEntity<>(requisitions, HttpStatus.OK);
  }

  /**
   * Get chosen requisitionGroupProgramSchedule.
   *
   * @param requisitionId UUID of requisitionGroupProgramSchedule whose we want to get
   * @return RequisitionGroupProgramSchedule.
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getRequisitionGroupProgramSchedule(
        @PathVariable("id") UUID requisitionId) {
    RequisitionGroupProgramSchedule requisition = repository.findOne(requisitionId);
    if (requisition == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(requisition, HttpStatus.OK);
    }
  }

  /**
   * Allows updating requisitionGroupProgramSchedule.
   *
   * @param reqGroupProgSchedule A requisitionGroupProgramSchedule
   *                                        bound to the request body
   * @param requisitionId UUID of requisitionGroupProgramSchedule
   *                                          which we want to update
   * @return ResponseEntity containing the updated requisitionGroup
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateRequisitionGroupProgramSchedule(
        @RequestBody RequisitionGroupProgramScheduleDto reqGroupProgSchedule,
        @PathVariable("id") UUID requisitionId) {

    RequisitionGroupProgramSchedule reqGroupProgScheduleToUpdate;

    try {

      reqGroupProgScheduleToUpdate = RequisitionGroupProgramSchedule
          .newRequisitionGroupProgramSchedule(reqGroupProgSchedule);

      reqGroupProgScheduleToUpdate.setId(requisitionId);

      repository.save(reqGroupProgScheduleToUpdate);

      LOGGER.debug("Saved requisitionGroupProgramSchedule with id: "
            + reqGroupProgScheduleToUpdate.getId());
      return new ResponseEntity<RequisitionGroupProgramSchedule>(
            reqGroupProgScheduleToUpdate, HttpStatus.OK);
    } catch (DataIntegrityViolationException ex) {
      ErrorResponse errorResponse =
            new ErrorResponse("An error occurred while saving requisitionGroupProgramSchedule"
                  + " with id: " + requisitionId, ex.getMessage());
      LOGGER.error(errorResponse.getMessage(), ex);
      return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Allows deleting requisitionGroupProgramSchedule.
   *
   * @param requisitionId UUID of requisitionGroupProgramSchedule whose we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteRequisitionGroupProgramSchedule(
        @PathVariable("id") UUID requisitionId) {
    RequisitionGroupProgramSchedule requisition = repository.findOne(requisitionId);
    if (requisition == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      repository.delete(requisition);
      return new ResponseEntity<RequisitionGroupProgramSchedule>(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Returns chosen RequisitionGroupProgramSchedule.
   * @param programId program of searched RequisitionGroupProgramSchedule.
   * @param facilityId facility of searched RequisitionGroupProgramSchedule.
   * @return RequisitionGroupProgramSchedule.
   */
  @RequestMapping(value = "/requisitionGroupProgramSchedules/search",
        method = RequestMethod.GET)
  public ResponseEntity<?> searchByProgramAndFacility(
        @RequestParam(value = "programId", required = true) UUID programId,
        @RequestParam(value = "facilityId", required = true) UUID facilityId)
        throws InvalidIdException, RequisitionGroupProgramScheduleException {

    if (programId == null) {
      throw new InvalidIdException("Program id must be provided.");
    }

    if (facilityId == null) {
      throw new InvalidIdException("Facility id must be provided.");
    }

    Program program = programRepository.findOne(programId);
    Facility facility = facilityRepository.findOne(facilityId);

    RequisitionGroupProgramSchedule requisitionGroupProgramSchedule = null;

    if (program != null && facility != null) {
      requisitionGroupProgramSchedule =
            service.searchRequisitionGroupProgramSchedule(program, facility);
    }

    return ResponseEntity.ok(exportToDto(requisitionGroupProgramSchedule));
  }

  private RequisitionGroupProgramScheduleDto exportToDto(
        RequisitionGroupProgramSchedule requisitionGroupProgramSchedule) {
    RequisitionGroupProgramScheduleDto dto = null;
    if (requisitionGroupProgramSchedule != null) {
      dto = new RequisitionGroupProgramScheduleDto();
      requisitionGroupProgramSchedule.export(dto);
    }
    return dto;
  }
}
