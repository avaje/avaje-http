package org.example.web.myapp;

import io.avaje.http.api.FormParam;
import io.avaje.http.api.FormPrefix;

public class PersonForm {

  @FormParam("name")
  public String name;

  @FormPrefix("invoice")
  public AddressForm invoice;

  @FormPrefix("shipping")
  public AddressForm shipping;
}
