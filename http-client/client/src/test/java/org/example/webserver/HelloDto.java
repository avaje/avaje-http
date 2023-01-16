package org.example.webserver;

import java.time.Instant;
import java.util.UUID;

public class HelloDto {

  public int id;
  public String name;
  public String otherParam;
  private UUID gid;

  private Instant whenAction;

  public HelloDto(int id, String name, String otherParam) {
    this.id = id;
    this.name = name;
    this.otherParam = otherParam;
  }

  /**
   * Jackson constructor.
   */
  public HelloDto() {
  }

  public UUID getGid() {
    return gid;
  }

  public void setGid(UUID gid) {
    this.gid = gid;
  }

  public Instant getWhenAction() {
    return whenAction;
  }

  public void setWhenAction(Instant whenAction) {
    this.whenAction = whenAction;
  }

  @Override
  public String toString() {
    return "id:" + id + " name:" + name + " other:" + otherParam;
  }

}
