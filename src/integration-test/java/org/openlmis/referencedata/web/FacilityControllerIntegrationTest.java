package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.SupplyLine;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.service.SupplyLineService;
import org.springframework.boot.test.mock.mockito.MockBean;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.TooManyMethods")
public class FacilityControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/facilities";
  private static final String SUPPLYING_URL = RESOURCE_URL + "/supplying";

  @MockBean
  private SupplyLineService supplyLineService;

  @MockBean
  private ProgramRepository programRepository;

  @MockBean
  private SupervisoryNodeRepository supervisoryNodeRepository;

  private Integer currentInstanceNumber;
  private UUID programId;
  private UUID supervisoryNodeId;

  @Before
  public void setUp() {
    currentInstanceNumber = 0;
  }

  @Test
  public void shouldReturnSupplyingDepots() {
    int searchedFacilitiesAmt = 3;

    Program searchedProgram = generateProgram();
    SupervisoryNode searchedSupervisoryNode = generateSupervisoryNode();

    List<SupplyLine> searchedSupplyLines = new ArrayList<>();
    for (int i = 0; i < searchedFacilitiesAmt; i++) {
      SupplyLine supplyLine = generateSupplyLine();
      supplyLine.setProgram(searchedProgram);
      supplyLine.setSupervisoryNode(searchedSupervisoryNode);

      searchedSupplyLines.add(supplyLine);
    }
    
    given(programRepository.findOne(programId)).willReturn(searchedProgram);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(searchedSupervisoryNode);
    given(supplyLineService.searchSupplyLines(searchedProgram, searchedSupervisoryNode))
        .willReturn(searchedSupplyLines);

    Facility[] response = restAssured.given()
        .queryParam("programId", programId)
        .queryParam("supervisoryNodeId", supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(200)
        .extract().as(Facility[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(searchedFacilitiesAmt, response.length);

    SupplyLine additionalSupplyLine = generateSupplyLine();
    Collection<Facility> searchedFacilities = searchedSupplyLines
        .stream().map(SupplyLine::getSupplyingFacility).collect(Collectors.toList());
    for (Facility facility : response) {
      assertTrue(searchedFacilities.contains(facility));
      assertNotEquals(facility, additionalSupplyLine.getSupplyingFacility());
    }
  }

  @Test
  public void shouldReturnBadRequestWhenSearchingForSupplyingDepotsWithNotExistingSupervisorNode() {

    Program searchedProgram = generateProgram();
    supervisoryNodeId = UUID.randomUUID();

    given(programRepository.findOne(programId)).willReturn(searchedProgram);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(null);


    restAssured.given()
        .queryParam("programId", programId)
        .queryParam("supervisoryNodeId", supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenSearchingForSupplyingDepotsWithNotExistingProgram() {

    SupervisoryNode searchedSupervisoryNode = generateSupervisoryNode();
    programId = UUID.randomUUID();

    given(programRepository.findOne(programId)).willReturn(null);
    given(supervisoryNodeRepository.findOne(supervisoryNodeId)).willReturn(searchedSupervisoryNode);

    restAssured.given()
        .queryParam("programId", programId)
        .queryParam("supervisoryNodeId", supervisoryNodeId)
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SUPPLYING_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private SupplyLine generateSupplyLine() {
    SupplyLine supplyLine = new SupplyLine();
    supplyLine.setProgram(generateProgram());
    supplyLine.setSupervisoryNode(generateSupervisoryNode());
    supplyLine.setSupplyingFacility(generateFacility());
    return supplyLine;
  }

  private SupervisoryNode generateSupervisoryNode() {
    SupervisoryNode supervisoryNode = new SupervisoryNode();
    supervisoryNodeId = UUID.randomUUID();
    supervisoryNode.setId(supervisoryNodeId);
    supervisoryNode.setCode("SupervisoryNode " + generateInstanceNumber());
    supervisoryNode.setFacility(generateFacility());
    return supervisoryNode;
  }

  private Program generateProgram() {
    Program program = new Program("Program " + generateInstanceNumber());
    programId = UUID.randomUUID();
    program.setId(programId);
    program.setPeriodsSkippable(false);
    return program;
  }

  private Facility generateFacility() {
    Integer instanceNumber = generateInstanceNumber();
    GeographicLevel geographicLevel = generateGeographicLevel();
    GeographicZone geographicZone = generateGeographicZone(geographicLevel);
    FacilityType facilityType = generateFacilityType();
    Facility facility = new Facility("FacilityCode " + instanceNumber);
    facility.setType(facilityType);
    facility.setGeographicZone(geographicZone);
    facility.setName("FacilityName " + instanceNumber);
    facility.setDescription("FacilityDescription " + instanceNumber);
    facility.setEnabled(true);
    facility.setActive(true);
    return facility;
  }

  private GeographicLevel generateGeographicLevel() {
    GeographicLevel geographicLevel = new GeographicLevel();
    geographicLevel.setCode("GeographicLevel " + generateInstanceNumber());
    geographicLevel.setLevelNumber(1);
    return geographicLevel;
  }

  private GeographicZone generateGeographicZone(GeographicLevel geographicLevel) {
    GeographicZone geographicZone = new GeographicZone();
    geographicZone.setCode("GeographicZone " + generateInstanceNumber());
    geographicZone.setLevel(geographicLevel);
    return geographicZone;
  }

  private FacilityType generateFacilityType() {
    FacilityType facilityType = new FacilityType();
    facilityType.setCode("FacilityType " + generateInstanceNumber());
    return facilityType;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }
}
