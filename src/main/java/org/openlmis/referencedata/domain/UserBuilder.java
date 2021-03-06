package org.openlmis.referencedata.domain;

import java.util.Objects;
import java.util.UUID;

public class UserBuilder {

  private UUID id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String timezone;
  private Facility homeFacility;
  private boolean active;
  private boolean verified;
  private boolean loginRestricted;

  private UserBuilder() {
    this.homeFacility = null;
    this.active = false;
    this.verified = false;
  }

  /**
   * Constructor.
   */
  public UserBuilder(String username, String firstName, String lastName, String email) {
    this();
    this.username = Objects.requireNonNull(username);
    this.firstName = Objects.requireNonNull(firstName);
    this.lastName = Objects.requireNonNull(lastName);
    this.email = Objects.requireNonNull(email);
  }

  public UserBuilder setId(UUID id) {
    this.id = id;
    return this;
  }

  public UserBuilder setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }

  public UserBuilder setHomeFacility(Facility homeFacility) {
    this.homeFacility = homeFacility;
    return this;
  }

  public UserBuilder setActive(boolean active) {
    this.active = active;
    return this;
  }

  public UserBuilder setVerified(boolean verified) {
    this.verified = verified;
    return this;
  }

  public UserBuilder setLoginRestricted(boolean loginRestricted) {
    this.loginRestricted = loginRestricted;
    return this;
  }

  public User createUser() {
    return new User(id, username, firstName, lastName, email, timezone, homeFacility, active,
        verified, loginRestricted);
  }
}