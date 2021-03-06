package org.openlmis.referencedata.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FacilityOperatorControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/facilityOperators";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private FacilityOperatorRepository facilityOperatorRepository;

  private FacilityOperator facilityOperator;
  private UUID facilityOperatorId;

  private Integer currentInstanceNumber = 0;

  @Before
  public void setUp() {
    currentInstanceNumber = generateInstanceNumber();
    facilityOperator = generateFacilityOperator();
  }

  @Test
  public void shouldPostFacilityOperator() {

    FacilityOperator response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(facilityOperator)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(FacilityOperator.class);

    assertEquals(facilityOperator, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllFacilityOperators() {

    List<FacilityOperator> facilityOperators = Arrays.asList(facilityOperator,
        generateFacilityOperator());

    given(facilityOperatorRepository.findAll()).willReturn(facilityOperators);

    FacilityOperator[] response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityOperator[].class);

    assertEquals(2, response.length);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPutFacilityOperator() {

    facilityOperator.setName("NewNameUpdate");

    given(facilityOperatorRepository.findOne(facilityOperatorId)).willReturn(facilityOperator);

    FacilityOperator response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityOperatorId)
        .body(facilityOperator)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityOperator.class);

    assertEquals(facilityOperator, response);
    assertEquals("NewNameUpdate", response.getName());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
  
  @Test
  public void shouldGetFacilityOperator() {

    given(facilityOperatorRepository.findOne(facilityOperatorId)).willReturn(facilityOperator);

    FacilityOperator response = restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityOperatorId)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityOperator.class);

    assertEquals(facilityOperator, response);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteFacilityOperator() {

    given(facilityOperatorRepository.findOne(facilityOperatorId)).willReturn(facilityOperator);

    restAssured
        .given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", facilityOperatorId)
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private FacilityOperator generateFacilityOperator() {
    FacilityOperator facilityOperator = new FacilityOperator();
    facilityOperatorId = UUID.randomUUID();
    facilityOperator.setId(facilityOperatorId);
    facilityOperator.setName("FacilityOperatorName" + currentInstanceNumber);
    facilityOperator.setCode("code" + currentInstanceNumber);
    facilityOperator.setDescription("description" + currentInstanceNumber);
    facilityOperator.setDisplayOrder(currentInstanceNumber);
    return facilityOperator;
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber++;
    return currentInstanceNumber;
  }
}
