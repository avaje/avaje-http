package org.example.myapp.web;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.avaje.http.api.Header;
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

  @QueryParam private ServerType type;

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

  public ServerType getType() {
    return type;
  }

  public void setType(ServerType type) {
    this.type = type;
  }
}
