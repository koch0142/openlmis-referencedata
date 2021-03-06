package org.openlmis.referencedata.validate;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.RequisitionGroupRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.CODE;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.CODE_CANNOT_BE_DUPLICATED;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.CODE_IS_TOO_LONG;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.DESCRIPTION;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.DESCRIPTION_IS_TOO_LONG;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.FACILITY_CAN_NOT_BE_NULL;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.FACILITY_MUST_EXIST;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.FACILITY_MUST_HAVE_ID;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.MEMBER_FACILITIES;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.NAME;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.NAME_IS_TOO_LONG;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.SUPERVISORY_NODE;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.SUPERVISORY_NODE_IS_REQUIRED;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.SUPERVISORY_NODE_MUST_EXIST;
import static org.openlmis.referencedata.validate.RequisitionGroupValidator.SUPERVISORY_NODE_MUST_HAVE_ID;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods"})
public class RequisitionGroupValidatorTest extends BaseValidatorTest {

  @Mock
  private SupervisoryNodeRepository supervisoryNodes;

  @Mock
  private RequisitionGroupRepository requisitionGroups;

  @Mock
  private FacilityRepository facilities;

  @InjectMocks
  private Validator validator = new RequisitionGroupValidator();

  private SupervisoryNode supervisoryNode;
  private RequisitionGroup requisitionGroup;
  private Errors errors;

  @Before
  public void setUp() throws Exception {
    Facility facility = new Facility("TestFacilityCode");
    facility.setId(UUID.randomUUID());

    supervisoryNode = new SupervisoryNode();
    supervisoryNode.setId(UUID.randomUUID());

    requisitionGroup = new RequisitionGroup();
    requisitionGroup.setId(UUID.randomUUID());
    requisitionGroup.setCode(RandomStringUtils.randomAlphanumeric(50));
    requisitionGroup.setName(RandomStringUtils.randomAlphanumeric(50));
    requisitionGroup.setDescription(RandomStringUtils.randomAlphanumeric(250));
    requisitionGroup.setSupervisoryNode(supervisoryNode);
    requisitionGroup.setMemberFacilities(Lists.newArrayList(facility));

    errors = new BeanPropertyBindingResult(requisitionGroup, "requisitionGroup");

    doReturn(mock(SupervisoryNode.class))
        .when(supervisoryNodes)
        .findOne(supervisoryNode.getId());

    doReturn(mock(Facility.class))
        .when(facilities)
        .findOne(facility.getId());
  }

  @Test
  public void shouldNotFindErrors() throws Exception {
    validator.validate(requisitionGroup, errors);

    assertThat(errors.getErrorCount(), is(equalTo(0)));
  }

  @Test
  public void shouldRejectIfSupervisoryNodeIsEmpty() throws Exception {
    requisitionGroup.setSupervisoryNode(null);

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, SUPERVISORY_NODE, SUPERVISORY_NODE_IS_REQUIRED);
  }

  @Test
  public void shouldRejectIfRequisitionGroupCodeIsDuplicated() throws Exception {
    doReturn(mock(RequisitionGroup.class))
        .when(requisitionGroups)
        .findByCode(requisitionGroup.getCode());

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, CODE, CODE_CANNOT_BE_DUPLICATED);
  }

  @Test
  public void shouldRejectIfSupervisoryHasNoId() throws Exception {
    requisitionGroup.getSupervisoryNode().setId(null);

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, SUPERVISORY_NODE, SUPERVISORY_NODE_MUST_HAVE_ID);
  }

  @Test
  public void shouldRejectIfSupervisoryNodeCanNotBeFound() throws Exception {
    doReturn(null)
        .when(supervisoryNodes)
        .findOne(any(UUID.class));

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, SUPERVISORY_NODE, SUPERVISORY_NODE_MUST_EXIST);
  }

  @Test
  public void shouldRejectIfFacilityIsNull() throws Exception {
    requisitionGroup.getMemberFacilities().add(null);

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES, FACILITY_CAN_NOT_BE_NULL);
  }

  @Test
  public void shouldRejectIfFacilityHasNoId() throws Exception {
    requisitionGroup.getMemberFacilities().add(new Facility("TestFacilityCode2"));

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES, FACILITY_MUST_HAVE_ID);
  }

  @Test
  public void shouldRejectIfFacilityCanNotBeFound() throws Exception {
    Facility facility = new Facility("TestFacilityCode2");
    facility.setId(UUID.randomUUID());

    requisitionGroup.getMemberFacilities().add(facility);

    validator.validate(requisitionGroup, errors);

    assertErrorMessage(errors, MEMBER_FACILITIES, FACILITY_MUST_EXIST);
  }

  @Test
  public void shouldNotThrowExceptionIfMemberFacilitiesIsNullOrEmpty() throws Exception {
    requisitionGroup.setMemberFacilities(null);

    validator.validate(requisitionGroup, errors);
    assertThat(errors.hasFieldErrors(MEMBER_FACILITIES), is(false));
  }

  @Test
  public void shouldRejectIfCodeIsTooLong() throws Exception {
    requisitionGroup.setCode(RandomStringUtils.randomAlphanumeric(51));

    validator.validate(requisitionGroup, errors);
    assertErrorMessage(errors, CODE, CODE_IS_TOO_LONG);
  }

  @Test
  public void shouldRejectIfNameIsTooLong() throws Exception {
    requisitionGroup.setName(RandomStringUtils.randomAlphanumeric(51));

    validator.validate(requisitionGroup, errors);
    assertErrorMessage(errors, NAME, NAME_IS_TOO_LONG);
  }

  @Test
  public void shouldRejectIfDescriptionIsTooLong() throws Exception {
    requisitionGroup.setDescription(RandomStringUtils.randomAlphanumeric(251));

    validator.validate(requisitionGroup, errors);
    assertErrorMessage(errors, DESCRIPTION, DESCRIPTION_IS_TOO_LONG);
  }

  @Test
  public void shouldNotRejectIfCodeIsDuplicatedAndIdsAreSame() throws Exception {
    RequisitionGroup old = new RequisitionGroup();
    old.setId(requisitionGroup.getId());
    old.setCode(requisitionGroup.getCode());
    old.setName(RandomStringUtils.randomAlphanumeric(50));
    old.setDescription(RandomStringUtils.randomAlphanumeric(250));
    old.setSupervisoryNode(supervisoryNode);

    doReturn(old)
        .when(requisitionGroups)
        .findByCode(requisitionGroup.getCode());

    validator.validate(requisitionGroup, errors);
    assertThat(errors.hasFieldErrors(CODE), is(false));
  }

  @Test
  public void shouldRejectIfCodeIsDuplicatedAndIdsAreDifferent() throws Exception {
    RequisitionGroup old = new RequisitionGroup();
    old.setId(UUID.randomUUID());
    old.setCode(requisitionGroup.getCode());
    old.setName(RandomStringUtils.randomAlphanumeric(50));
    old.setDescription(RandomStringUtils.randomAlphanumeric(250));
    old.setSupervisoryNode(supervisoryNode);

    doReturn(old)
        .when(requisitionGroups)
        .findByCode(requisitionGroup.getCode());

    validator.validate(requisitionGroup, errors);
    assertErrorMessage(errors, CODE, CODE_CANNOT_BE_DUPLICATED);
  }
}
