package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.util.UUID;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity {

  @Id
  @GeneratedValue(generator = "uuid-gen")
  @GenericGenerator(name = "uuid-gen",
      strategy = "org.openlmis.referencedata.util.ConditionalUuidGenerator")
  @Type(type = "pg-uuid")
  @Getter
  @Setter
  protected UUID id;
}
