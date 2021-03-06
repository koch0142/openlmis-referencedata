package org.openlmis.referencedata.domain;

import static java.util.Collections.singleton;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.openlmis.referencedata.exception.RightTypeException;
import org.openlmis.referencedata.exception.RoleAssignmentException;

import java.util.Objects;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("fulfillment")
@NoArgsConstructor
public class FulfillmentRoleAssignment extends RoleAssignment {

  @ManyToOne
  @JoinColumn(name = "warehouseid")
  @Getter
  private Facility warehouse;

  private FulfillmentRoleAssignment(Role role) throws RightTypeException {
    super(role);
  }

  /**
   * Default constructor. Must always have a role and a facility, which must be of type
   * 'warehouse'.
   *
   * @param role      the role being assigned
   * @param warehouse the warehouse where the role applies
   * @throws RightTypeException      if role passed in has rights which are not an acceptable right
   *                                 type
   * @throws RoleAssignmentException if facility passed in is not of type 'warehouse'
   */
  public FulfillmentRoleAssignment(Role role, Facility warehouse)
      throws RightTypeException, RoleAssignmentException {
    super(role);

    if (!warehouse.getType().getCode().equalsIgnoreCase("warehouse")) {
      throw new RoleAssignmentException("referencedata.error.facility-type-must-be-warehouse");
    }

    this.warehouse = warehouse;
  }

  @Override
  protected Set<RightType> getAcceptableRightTypes() {
    return singleton(RightType.ORDER_FULFILLMENT);
  }

  @Override
  /**
   * Check if this role assignment has a right based on specified criteria. For fulfillment, check
   * also that the warehouse matches.
   */
  public boolean hasRight(RightQuery rightQuery) {
    boolean roleMatches = role.contains(rightQuery.getRight());
    boolean warehouseMatches = warehouse.equals(rightQuery.getWarehouse());

    return roleMatches && warehouseMatches;
  }

  @Override
  public void assignTo(User user) {
    super.assignTo(user);
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setRoleId(role.getId());
    exporter.setWarehouseCode(warehouse.getCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FulfillmentRoleAssignment)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    FulfillmentRoleAssignment that = (FulfillmentRoleAssignment) obj;
    return Objects.equals(role, that.role)
        && Objects.equals(user, that.user)
        && Objects.equals(warehouse, that.warehouse);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), warehouse);
  }

  public interface Exporter extends RoleAssignment.Exporter {
    void setWarehouseCode(String warehouseCode);
  }
}
