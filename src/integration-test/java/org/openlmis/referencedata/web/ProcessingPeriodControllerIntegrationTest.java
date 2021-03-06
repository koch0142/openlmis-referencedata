package org.openlmis.referencedata.web;

import com.google.common.collect.Sets;
import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.ProcessingSchedule;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.RequisitionGroupProgramSchedule;
import org.openlmis.referencedata.dto.ProcessingPeriodDto;
import org.openlmis.referencedata.exception.RequisitionGroupProgramScheduleException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.service.ProcessingPeriodService;
import org.openlmis.referencedata.validate.ProcessingPeriodValidator;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.validation.Errors;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;


@SuppressWarnings({"PMD.TooManyMethods"})
public class ProcessingPeriodControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/processingPeriods";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String SEARCH_BY_UUID_AND_DATE_URL = RESOURCE_URL + "/searchByUUIDAndDate";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String DIFFERENCE_URL = RESOURCE_URL + "/{id}/difference";
  private static final String PROGRAM = "programId";
  private static final String FACILITY = "facilityId";
  private static final String PROCESSING_SCHEDULE = "processingScheduleId";
  private static final String START_DATE = "startDate";
  private static final String ACCESS_TOKEN = "access_token";

  @MockBean
  private ProcessingPeriodRepository periodRepository;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private FacilityRepository facilityRepository;

  @MockBean
  private ProcessingScheduleRepository scheduleRepository;

  @MockBean
  private ProcessingPeriodService periodService;

  @MockBean(name = "beforeSavePeriodValidator")
  private ProcessingPeriodValidator validator;

  private ProcessingPeriod firstPeriod;
  private ProcessingPeriod secondPeriod;
  private ProcessingSchedule schedule;
  private RequisitionGroupProgramSchedule requisitionGroupProgramSchedule;

  private UUID firstPeriodId;
  private UUID programId;
  private UUID facilityId;
  private UUID scheduleId;

  private static Integer currentInstanceNumber = 0;

  @Before
  public void setUp() {
    schedule = generateSchedule();
    firstPeriod = ProcessingPeriod.newPeriod("P1", schedule,
          LocalDate.of(2016, 1, 1), LocalDate.of(2016, 2, 1));
    secondPeriod = ProcessingPeriod.newPeriod("P2", schedule,
          LocalDate.of(2016, 2, 2), LocalDate.of(2016, 3, 2));
    requisitionGroupProgramSchedule = generateRequisitionGroupProgramSchedule();
    firstPeriodId = UUID.randomUUID();
    programId = UUID.randomUUID();
    facilityId = UUID.randomUUID();
    scheduleId = UUID.randomUUID();
  }

  @Test
  public void shouldPostPeriodWithoutGap() {
    ProcessingPeriodDto dto = new ProcessingPeriodDto();
    firstPeriod.export(dto);

    ProcessingPeriodDto savedFirstPeriod = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(firstPeriod)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ProcessingPeriodDto.class);

    assertEquals(dto, savedFirstPeriod);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    ProcessingPeriodDto dto2 = new ProcessingPeriodDto();
    secondPeriod.export(dto2);

    ProcessingPeriodDto savedSecondPeriod = restAssured.given()
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(secondPeriod)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ProcessingPeriodDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(dto2, savedSecondPeriod);
  }

  @Test
  public void shouldReturnBadRequestIfThereAreValidationErrors() {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        Errors errors = (Errors) args[1];
        errors.reject("testReject");
        return null;
      }
    }).when(validator).validate(anyObject(), any(Errors.class));

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(secondPeriod)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDisplayTotalDifference() {

    given(periodRepository.findOne(firstPeriodId)).willReturn(firstPeriod);

    String response = restAssured.given()
        .pathParam("id", firstPeriodId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(DIFFERENCE_URL)
        .then()
        .statusCode(200)
        .extract().asString();

    assertTrue(response.contains("Period lasts 1 months and 1 days"));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindPeriodsByProgramAndFacility()
        throws RequisitionGroupProgramScheduleException {

    given(programRepository.findOne(programId))
          .willReturn(requisitionGroupProgramSchedule.getProgram());
    given(facilityRepository.findOne(facilityId))
          .willReturn(requisitionGroupProgramSchedule.getDropOffFacility());

    given(periodService.filterPeriods(requisitionGroupProgramSchedule.getProgram(),
          requisitionGroupProgramSchedule.getDropOffFacility()))
          .willReturn(Arrays.asList(firstPeriod, secondPeriod));

    ProcessingPeriodDto[] response = restAssured.given()
        .queryParam(PROGRAM, programId)
        .queryParam(FACILITY, facilityId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(ProcessingPeriodDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(2, response.length);
  }

  @Test
  public void shouldFindPeriodsByScheduleAndDate() {

    given(scheduleRepository.findOne(scheduleId)).willReturn(schedule);
    given(periodService.searchPeriods(schedule, secondPeriod.getStartDate()))
          .willReturn(Arrays.asList(secondPeriod, firstPeriod));

    ProcessingPeriodDto[] response = restAssured.given()
          .queryParam(PROCESSING_SCHEDULE, scheduleId)
          .queryParam(START_DATE, secondPeriod.getStartDate().toString())
          .queryParam(ACCESS_TOKEN, getToken())
          .when()
          .get(SEARCH_BY_UUID_AND_DATE_URL)
          .then()
          .statusCode(200)
          .extract().as(ProcessingPeriodDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(2, response.length);
    for ( ProcessingPeriodDto period : response ) {
      assertEquals(
            period.getProcessingSchedule().getId(),
            firstPeriod.getProcessingSchedule().getId());
    }
  }

  @Test
  public void shouldDeletePeriod() {

    given(periodRepository.findOne(firstPeriodId)).willReturn(firstPeriod);

    restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", firstPeriodId)
          .when()
          .delete(ID_URL)
          .then()
          .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutPeriod() {
    firstPeriod.setDescription("OpenLMIS");
    ProcessingPeriodDto dto = new ProcessingPeriodDto();
    firstPeriod.export(dto);

    given(periodRepository.findOne(firstPeriodId)).willReturn(firstPeriod);

    ProcessingPeriodDto response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", firstPeriodId)
          .body(firstPeriod)
          .when()
          .put(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(ProcessingPeriodDto.class);

    assertEquals(response.getDescription(), "OpenLMIS");
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllPeriods() {

    Set<ProcessingPeriod> storedPeriods = Sets.newHashSet(firstPeriod, secondPeriod);
    given(periodRepository.findAll()).willReturn(storedPeriods);

    ProcessingPeriodDto[] response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .when()
          .get(RESOURCE_URL)
          .then()
          .statusCode(200)
          .extract().as(ProcessingPeriodDto[].class);

    List<ProcessingPeriodDto> periods = Arrays.asList(response);
    assertEquals(periods.size(), 2);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenPeriod() {
    ProcessingPeriodDto dto = new ProcessingPeriodDto();
    firstPeriod.export(dto);
    given(periodRepository.findOne(firstPeriodId)).willReturn(firstPeriod);

    ProcessingPeriodDto response = restAssured.given()
          .queryParam(ACCESS_TOKEN, getToken())
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .pathParam("id", firstPeriodId)
          .when()
          .get(ID_URL)
          .then()
          .statusCode(200)
          .extract().as(ProcessingPeriodDto.class);

    assertEquals(dto, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private ProcessingSchedule generateSchedule() {
    ProcessingSchedule schedule = new ProcessingSchedule();
    schedule.setCode("S" + generateInstanceNumber());
    schedule.setName("schedule");
    schedule.setDescription("Test schedule");
    return schedule;
  }

  private RequisitionGroupProgramSchedule generateRequisitionGroupProgramSchedule() {
    requisitionGroupProgramSchedule = new RequisitionGroupProgramSchedule();
    Program program = generateProgram();
    Facility facility = generateFacility();
    requisitionGroupProgramSchedule.setProgram(program);
    requisitionGroupProgramSchedule.setDropOffFacility(facility);
    requisitionGroupProgramSchedule.setProcessingSchedule(schedule);
    return requisitionGroupProgramSchedule;
  }

  private Program generateProgram() {
    Program program = new Program("PROG" + generateInstanceNumber());
    program.setName("name");
    return program;
  }

  private Facility generateFacility() {
    Facility facility = new Facility("F" + generateInstanceNumber());
    FacilityType facilityType = generateFacilityType();
    GeographicZone geographicZone = generateGeographicZone();

    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("facilityName");
    facility.setDescription("Test facility");
    facility.setActive(true);
    facility.setEnabled(true);
    return facility;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FT" + generateInstanceNumber());
    return facilityType;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel level = new GeographicLevel();
    level.setCode("GL" + generateInstanceNumber());
    level.setLevelNumber(1);
    return level;
  }

  private GeographicZone generateGeographicZone() {
    GeographicZone geographicZone = new GeographicZone();
    GeographicLevel level = generateGeographicLevel();
    geographicZone.setLevel(level);
    geographicZone.setCode("GZ" + generateInstanceNumber());
    return geographicZone;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
