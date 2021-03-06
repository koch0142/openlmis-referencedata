package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;

import java.util.Set;

public class SupervisionRoleAssignmentTest {

  private Right right = Right.newRight("right", SUPERVISION);
  private Program program = new Program("em");
  private SupervisoryNode node = SupervisoryNode.newSupervisoryNode("SN1", new Facility("F1"));
  private Role role = Role.newRole("role", right);
  private SupervisionRoleAssignment homeFacilityRoleAssignment =
      new SupervisionRoleAssignment(role, program);
  private SupervisionRoleAssignment supervisedRoleAssignment =
      new SupervisionRoleAssignment(role, program, node);
  private User user = new UserBuilder("testuser", "Test", "User", "test@test.com").createUser();

  public SupervisionRoleAssignmentTest() throws RightTypeException, RoleException {
  }

  @Test
  public void shouldHaveRightWhenRightAndProgramAndSupervisoryNodeMatch()
      throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, node);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenProgramDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, new Program("test"), node);
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenNodeDoesNotMatch() throws RightTypeException {

    //when
    RightQuery rightQuery = new RightQuery(right, program, new SupervisoryNode());
    boolean hasRight = supervisedRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }

  @Test
  public void shouldAssignHomeFacilityProgramWhenUserAssignedWithNoNode() {

    //when
    homeFacilityRoleAssignment.assignTo(user);
    Set<Program> programs = user.getHomeFacilityPrograms();

    //then
    assertThat(programs.size(), is(1));
    assertTrue(programs.contains(program));
  }

  @Test
  public void shouldAssignSupervisedProgramWhenUserAssignedWithNode() {

    //when
    supervisedRoleAssignment.assignTo(user);
    Set<Program> programs = user.getSupervisedPrograms();

    //then
    assertThat(programs.size(), is(1));
    assertTrue(programs.contains(program));
  }
}
