package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleException;

import java.util.Set;

public class RoleTest {

  private String roleName = "role";

  private String right1Name = "right1";
  private String right2Name = "right2";

  @Test
  public void shouldGroupRightsOfSameType() throws RightTypeException, RoleException {
    //given
    Right right1 = Right.newRight(right1Name, RightType.ORDER_FULFILLMENT);
    Right right2 = Right.newRight(right2Name, RightType.ORDER_FULFILLMENT);

    //when
    Role role = Role.newRole(roleName, right1, right2);

    //then
    Set<Right> rights = role.getRights();
    assertThat(rights.size(), is(2));
    assertTrue(rights.contains(right1));
    assertTrue(rights.contains(right2));
  }

  @Test(expected = RightTypeException.class)
  public void shouldNotGroupRightsOfDifferentTypes() throws RightTypeException, RoleException {
    //given
    Right right1 = Right.newRight(right1Name, RightType.ORDER_FULFILLMENT);
    Right right2 = Right.newRight(right2Name, RightType.SUPERVISION);

    //when
    Role.newRole(roleName, right1, right2);
  }

  @Test
  public void shouldBeAbleToAddRightsOfSameTypeToExistingRole() throws RightTypeException,
      RoleException {
    //given
    Role role = Role.newRole(roleName, Right.newRight(right1Name, RightType.SUPERVISION));

    //when
    Right additionalRight = Right.newRight(right2Name, RightType.SUPERVISION);
    role.add(additionalRight);

    //then
    Set<Right> rights = role.getRights();
    assertThat(rights.size(), is(2));
    assertTrue(rights.contains(additionalRight));
  }

  @Test(expected = RightTypeException.class)
  public void shouldNotBeAbleToAddRightsOfDifferentTypeToExistingRole() throws RightTypeException,
      RoleException {
    //given
    Role role = Role.newRole(roleName, Right.newRight(right1Name, RightType.SUPERVISION));

    //when
    Right rightOfDifferentType = Right.newRight(right2Name, RightType.ORDER_FULFILLMENT);
    role.add(rightOfDifferentType);
  }

  @Test
  public void shouldIndicateIfItContainsARight() throws RightTypeException, RoleException {
    //given
    Right right1 = Right.newRight(right1Name, RightType.SUPERVISION);
    Right right2 = Right.newRight(right2Name, RightType.SUPERVISION);
    right1.attach(right2);

    Role role = Role.newRole(roleName, right1);

    //when
    boolean containsRight1 = role.contains(right1);
    boolean containsRight2 = role.contains(right2);

    //then
    assertTrue(containsRight1);
    assertTrue(containsRight2);
  }
}
