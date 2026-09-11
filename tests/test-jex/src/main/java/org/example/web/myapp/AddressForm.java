package org.example.web.myapp;

import io.avaje.http.api.FormParam;
import io.avaje.http.api.FormPrefix;

public class AddressForm {

  @FormParam("street")
  public String street;

  @FormParam("city")
  public String city;

  @FormParam("zip")
  private String postalCode;

  @FormPrefix("contact")
  public ContactForm contact;

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getPostalCode() {
    return postalCode;
  }
}
