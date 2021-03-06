package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.RequisitionGroupProgramScheduleRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RequisitionGroupProgramScheduleControllerIntegrationTest
    extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/requisitionGroupProgramSchedules";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");

  @MockBean
  private RequisitionGroupProgramScheduleRepository repository;
  
  private RequisitionGroup requisitionGroup;
  private ProcessingSchedule schedule;
  private Program program;
  
  private RequisitionGroupProgramSchedule reqGroupProgSchedule;
  private UUID requisitionGroupProgramScheduleId;

  /**
   * Constructor for tests.
   */
  public RequisitionGroupProgramScheduleControllerIntegrationTest() {

    requisitionGroup = new RequisitionGroup("RG1", "Requisition Group 1",
        SupervisoryNode.newSupervisoryNode("SN1", new Facility("F1")));

    schedule = new ProcessingSchedule("scheduleCode", "scheduleName");

    program = new Program("programCode");
    program.setPeriodsSkippable(true);

    reqGroupProgSchedule = RequisitionGroupProgramSchedule
        .newRequisitionGroupProgramSchedule(requisitionGroup, program, schedule, false);
    
    requisitionGroupProgramScheduleId = UUID.randomUUID();
  }

  @Test
  public void shouldDeleteRequisitionGroupProgramSchedule() {

    given(repository.findOne(requisitionGroupProgramScheduleId)).willReturn(reqGroupProgSchedule);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupProgramScheduleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentRequisitionGroupProgramSchedule() {

    given(repository.findOne(requisitionGroupProgramScheduleId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupProgramScheduleId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPostRequisitionGroupProgramSchedule() {

    RequisitionGroupProgramSchedule response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(reqGroupProgSchedule)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(RequisitionGroupProgramSchedule.class);

    assertEquals(reqGroupProgSchedule, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllRequisitionGroupProgramSchedules() {

    List<RequisitionGroupProgramSchedule> storedRequisitionGroupProgramSchedules = Arrays.asList(
        reqGroupProgSchedule, RequisitionGroupProgramSchedule
            .newRequisitionGroupProgramSchedule(requisitionGroup, program, schedule, true));
    given(repository.findAll()).willReturn(storedRequisitionGroupProgramSchedules);

    RequisitionGroupProgramSchedule[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupProgramSchedule[].class);

    assertEquals(storedRequisitionGroupProgramSchedules.size(), response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetRequisitionGroupProgramSchedule() {

    given(repository.findOne(requisitionGroupProgramScheduleId)).willReturn(reqGroupProgSchedule);

    RequisitionGroupProgramSchedule response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupProgramScheduleId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupProgramSchedule.class);

    assertEquals(reqGroupProgSchedule, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentRequisitionGroupProgramSchedule() {

    given(repository.findOne(requisitionGroupProgramScheduleId)).willReturn(null);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupProgramScheduleId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutRequisitionGroupProgramSchedule() {

    reqGroupProgSchedule.setDirectDelivery(true);
    given(repository.findOne(requisitionGroupProgramScheduleId)).willReturn(reqGroupProgSchedule);

    RequisitionGroupProgramSchedule response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", requisitionGroupProgramScheduleId)
        .body(reqGroupProgSchedule)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupProgramSchedule.class);

    assertEquals(reqGroupProgSchedule, response);
    assertTrue(response.isDirectDelivery());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewRequisitionGroupProgramScheduleIfDoesNotExist() {

    reqGroupProgSchedule.setDirectDelivery(true);
    given(repository.findOne(requisitionGroupProgramScheduleId)).willReturn(null);

    RequisitionGroupProgramSchedule response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", ID)
        .body(reqGroupProgSchedule)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(RequisitionGroupProgramSchedule.class);

    assertEquals(reqGroupProgSchedule, response);
    assertTrue(response.isDirectDelivery());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
