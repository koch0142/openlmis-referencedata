package org.openlmis.referencedata.domain;

import static java.util.Collections.singleton;
import static org.openlmis.referencedata.domain.RightType.SUPERVISION;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.openlmis.referencedata.exception.RightTypeException;

import java.util.Objects;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("supervision")
@NoArgsConstructor
public class SupervisionRoleAssignment extends RoleAssignment {

  @ManyToOne
  @JoinColumn(name = "programid")
  @Getter
  private Program program;

  @ManyToOne
  @JoinColumn(name = "supervisorynodeid")
  @Getter
  private SupervisoryNode supervisoryNode;

  private SupervisionRoleAssignment(Role role) throws RightTypeException {
    super(role);
  }

  /**
   * Constructor for home facility supervision. Must always have a role and a program.
   *
   * @param role    the role being assigned
   * @param program the program where the role applies
   * @throws RightTypeException if role passed in has rights which are not an acceptable right type
   */
  public SupervisionRoleAssignment(Role role, Program program) throws RightTypeException {
    super(role);
    this.program = program;
  }

  /**
   * Constructor for supervisory supervision. Must always have a role, a program and a supervisory
   * node.
   *
   * @param role            the role being assigned
   * @param program         the program where the role applies
   * @param supervisoryNode the supervisory node where the role applies
   * @throws RightTypeException if role passed in has rights which are not an acceptable right type
   */
  public SupervisionRoleAssignment(Role role, Program program, SupervisoryNode supervisoryNode)
      throws RightTypeException {
    super(role);
    this.program = program;
    this.supervisoryNode = supervisoryNode;
  }

  @Override
  protected Set<RightType> getAcceptableRightTypes() {
    return singleton(SUPERVISION);
  }

  @Override
  /**
   * Check if this role assignment has a right based on specified criteria. For supervision, 
   * check also that program matches and supervisory node matches (if present).
   */
  public boolean hasRight(RightQuery rightQuery) {
    boolean roleMatches = role.contains(rightQuery.getRight());
    boolean programMatches = program.equals(rightQuery.getProgram());

    boolean nodePresentAndMatches = supervisoryNode != null
        && supervisoryNode.equals(rightQuery.getSupervisoryNode());
    boolean nodeAbsentAndMatches = supervisoryNode == null
        && rightQuery.getSupervisoryNode() == null;

    boolean nodeMatches = nodePresentAndMatches || nodeAbsentAndMatches;

    return roleMatches && programMatches && nodeMatches;
  }

  @Override
  /**
   * Assign this role assignment to the specified user. For supervision, will also add programs 
   * and supervised facilities to the user.
   */
  public void assignTo(User user) {
    super.assignTo(user);
    if (supervisoryNode == null) {
      user.addHomeFacilityProgram(program);
    } else {
      user.addSupervisedProgram(program);
      Set<Facility> supervisedFacilities = supervisoryNode.getAllSupervisedFacilities();
      user.addSupervisedFacilities(supervisedFacilities);
    }
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setRoleId(role.getId());
    exporter.setProgramCode(program.getCode().toString());
    if (supervisoryNode != null) {
      exporter.setSupervisoryNodeCode(supervisoryNode.getCode());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SupervisionRoleAssignment)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    SupervisionRoleAssignment that = (SupervisionRoleAssignment) obj;
    return Objects.equals(role, that.role)
        && Objects.equals(user, that.user)
        && Objects.equals(program, that.program)
        && Objects.equals(supervisoryNode, that.supervisoryNode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), program, supervisoryNode);
  }

  public interface Exporter extends RoleAssignment.Exporter {
    void setProgramCode(String programCode);

    void setSupervisoryNodeCode(String supervisoryNodeCode);
  }
}