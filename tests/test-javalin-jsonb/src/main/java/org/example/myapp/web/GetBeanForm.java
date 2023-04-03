package org.example.myapp.web;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.avaje.http.api.Header;
import io.avaje.http.api.Ignore;
import io.avaje.http.api.QueryParam;
import io.avaje.jsonb.Json;

@Json
@Valid
public class GetBeanForm {

  @NotNull
  @Size(min = 2, max = 150)
  private String name;

  @Email
  @Size(max = 100)
  private String email;

  private List<String> addresses;

  @Header private String head;

  @QueryParam private Set<ServerType> type;

  @Json.Ignore @Ignore private String ignored;

  public String getIgnored() {
    return ignored;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public GetBeanForm(String name, String email) {
    this.name = name;
    this.email = email;
  }

  @Override
  public String toString() {
    return "HelloForm{" + "name='" + name + '\'' + ", email='" + email + '\'' + '}';
  }

  public List<String> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<String> addresses) {
    this.addresses = addresses;
  }

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public Set<ServerType> getType() {
    return type;
  }

  public void setType(Set<ServerType> type) {
    this.type = type;
  }
}
